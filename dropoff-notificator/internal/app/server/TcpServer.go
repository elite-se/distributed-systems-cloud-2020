package server

import (
	"bufio"
	"context"
	"fmt"
	"net"
	"os"
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
			_, _ = fmt.Fprintf(os.Stderr, "Failed to bind accept connection: %s\n", err)
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

	requestChan := make(chan string)
	go func() {
		if err := readLinesFromClient(ctx, rw.Reader, requestChan); err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "Failed to retrieve request string: %s\n", err)
			cancel()
		}
	}()

	// send info whenever there is one
	request := <-requestChan
	infoChan := make(chan DropoffEvent, 3)
	defer close(infoChan)
	handle := broker.register(request, infoChan)
	defer broker.unregister(handle)
	for {
		select {
		case info := <-infoChan:
			_, _ = fmt.Fprintf(rw, "%s\n", info)
		case request := <-requestChan:
			if strings.ToLower(request) == "quit" {
				cancel()
			} else {
				_, _ = rw.WriteString("Send \"quit\" to quit or shut the f*ck up\n")
			}
		case <-ctx.Done():
			return
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
			clientChan <- clientRequest
		}
	}
}
