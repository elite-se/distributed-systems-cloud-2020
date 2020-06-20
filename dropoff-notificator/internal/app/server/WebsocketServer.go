package server

import (
	"context"
	"errors"
	"fmt"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
	"os"
	"strconv"
	"strings"
)

func ListenForWSConnects(ctx context.Context, address string, broker DropoffEventBroker) {
	upgrader := websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
	}
	upgrader.CheckOrigin = func(r *http.Request) bool { return true }

	http.HandleFunc("/", func(rw http.ResponseWriter, r *http.Request) {
		conn, err := upgrader.Upgrade(rw, r, nil)
		if err != nil {
			log.Println(err)
			return
		}
		handleWSConnection(ctx, conn, broker)
	})
	_ = http.ListenAndServe(address, nil)
}

func handleWSConnection(ctx context.Context, conn *websocket.Conn, broker DropoffEventBroker) {
	ctx, cancel := context.WithCancel(ctx)
	defer conn.Close()

	// listen for stuff from client
	requestChan := make(chan string)
	go func() {
		if err := readMessagesFromWSClient(ctx, conn, requestChan); err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "Failed to retrieve string from client: %s\n", err)
			cancel()
		}
	}()

	// wait for cell info
	cell := Cell{
		x: readIntFromWSClient(ctx, requestChan),
		y: readIntFromWSClient(ctx, requestChan),
	}

	// send info whenever there is one
	metricsChan := make(chan float64, 3)
	defer close(metricsChan)
	handle := broker.register(cell, metricsChan)
	defer broker.unregister(handle)
	for {
		select {
		case metric := <-metricsChan:
			log.Printf("WS: sending %v\n", metric)
			writer, _ := conn.NextWriter(websocket.TextMessage)
			_, _ = fmt.Fprintf(writer, "%v\n", metric)
			_ = writer.Close()
		case request := <-requestChan:
			if strings.ToLower(request) == "quit" {
				cancel()
			}
		case <-ctx.Done():
			return
		}
	}
}

func readIntFromWSClient(ctx context.Context, clientChan chan string) int {
	for {
		select {
		case line := <-clientChan:
			val, err := strconv.Atoi(line)
			if err == nil {
				return val
			}
		case <-ctx.Done():
			return -1
		}
	}
}

func readMessagesFromWSClient(ctx context.Context, conn *websocket.Conn, clientChan chan string) error {
	for {
		select {
		case <-ctx.Done():
			return nil
		default:
			messageType, p, err := conn.ReadMessage()
			if err != nil {
				return err
			}
			if messageType != websocket.TextMessage {
				return errors.New("expected a text message")
			}
			clientChan <- string(p)
		}
	}
}
