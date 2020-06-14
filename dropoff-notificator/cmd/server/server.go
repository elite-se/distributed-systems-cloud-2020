package main

import (
	"context"
	"fmt"
	"github.com/elite-se/distributed-systems-cloud-2020/dropoff-notificator/internal/app/server"
	"os"
	"os/signal"
	"syscall"
)

func main() {
	// Stuff for coordinating shutdown
	ctx, cancel := context.WithCancel(context.Background())
	doneChan := make(chan bool)
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)

	// start server
	broker := os.Args[1]
	group := os.Args[2]
	go func() {
		server.ServeDropoffNofitications(ctx, broker, group)
		doneChan <- true
	}()

	// handle server shutdown
	for {
		select {
		case <-sigChan:
			fmt.Println("Shutting down server …")
			cancel()
		case <-doneChan:
			return
		}
	}
}
