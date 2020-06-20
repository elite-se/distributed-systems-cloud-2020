package server

import (
	"bufio"
	"context"
	"fmt"
	"net"
	"os"
	"strconv"
	"strings"
)

// ListenForTCPConnects allows clients to connect via TCP to get informed about dropoffs in a specific cell
func ListenForTCPConnects(ctx context.Context, address string, broker DropoffEventBroker) {
	tcpAddr, err := net.ResolveTCPAddr("tcp", address)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "Failed to build TCP address: %s\n", err)
		os.Exit(2)
	}

	ln, err := net.ListenTCP("tcp", tcpAddr)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "Failed to bind TCP listener: %s\n", err)
		os.Exit(2)
	}
	//noinspection GoUnhandledErrorResult
	defer ln.Close()

	connections := make(chan *net.TCPConn)
	for {
		go acceptSingleConnection(ln, connections)
		select {
		case conn := <-connections:
			go handleConnection(ctx, conn, broker)
		case <-ctx.Done():
			return
		}
	}
}

func acceptSingleConnection(ln *net.TCPListener, connections chan<- *net.TCPConn) {
	for {
		if conn, err := ln.AcceptTCP(); err != nil {
			if !strings.HasSuffix(err.Error(), "use of closed network connection") { // bad, I know
				_, _ = fmt.Fprintf(os.Stderr, "Failed to bind accept connection: %s\n", err)
			}
		} else {
			connections <- conn
			return
		}
	}
}

func handleConnection(ctx context.Context, conn *net.TCPConn, broker DropoffEventBroker) {
	ctx, cancel := context.WithCancel(ctx)
	defer conn.Close()
	rw := bufio.NewReadWriter(bufio.NewReader(conn), bufio.NewWriter(conn))

	// listen for stuff from client
	requestChan := make(chan string)
	go func() {
		if err := readLinesFromClient(ctx, rw.Reader, requestChan); err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "Failed to retrieve string from client: %s\n", err)
			cancel()
		}
	}()

	// wait for cell info
	cell := Cell{
		x: readIntFromClient(ctx, requestChan, rw.Writer),
		y: readIntFromClient(ctx, requestChan, rw.Writer),
	}

	// send info whenever there is one
	metricsChan := make(chan float64, 3)
	defer close(metricsChan)
	handle := broker.register(cell, metricsChan)
	defer broker.unregister(handle)
	for {
		select {
		case metric := <-metricsChan:
			fmt.Printf("TCP: sending %v\n", metric)
			_, _ = fmt.Fprintf(rw, "%v\n", metric)
			_ = rw.Flush()
		case request := <-requestChan:
			if strings.ToLower(request) == "quit" {
				cancel()
			} else {
				_, _ = rw.WriteString("Send \"quit\" to quit or shut the f*ck up!\n")
				_ = rw.Flush()
			}
		case <-ctx.Done():
			return
		}
	}
}

func readIntFromClient(ctx context.Context, clientChan chan string, w *bufio.Writer) int {
	for {
		select {
		case line := <-clientChan:
			val, err := strconv.Atoi(line)
			if err != nil {
				_, _ = w.WriteString("How hard can it be to send a proper int? Try again.\n")
				_ = w.Flush()
			} else {
				return val
			}
		case <-ctx.Done():
			return -1
		}
	}
}

func readLinesFromClient(ctx context.Context, reader *bufio.Reader, clientChan chan<- string) error {
	for {
		select {
		case <-ctx.Done():
			return nil
		default:
			clientRequest, err := reader.ReadString('\n')
			if err != nil {
				return err
			}
			clientChan <- strings.TrimSuffix(clientRequest, "\n")
		}
	}
}
