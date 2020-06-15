package server

import (
	"context"
	"fmt"
	"net"
	"os"
)

// ListenForTCPConnects allows clients to connect via TCP to get informed about dropoffs in a specific cell
func ListenForTCPConnects(ctx context.Context, address string) {
	ln, err := net.Listen("tcp", address)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "Failed to bind TCP listener: %s\n", err)
		os.Exit(2)
	}

	connections := make(chan net.Conn)
	for {
		go acceptSingleConnection(ln, connections)
		select {
		case conn := <-connections:
			go handleConnection(ctx, conn)
		case <-ctx.Done():
			_ = ln.Close()
			return
		}
	}
}

func acceptSingleConnection(ln net.Listener, connections chan<- net.Conn) {
	for {
		if conn, err := ln.Accept(); err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "Failed to bind accept connection: %s\n", err)
		} else {
			connections <- conn
			return
		}
	}
}

func handleConnection(ctx context.Context, conn net.Conn) {
	// TODO do something
}
