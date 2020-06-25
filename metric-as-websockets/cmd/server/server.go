package main

import (
	"context"
	"fmt"
	"github.com/elite-se/distributed-systems-cloud-2020/metric-as-websocket/internal/app/server"
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
	topic := os.Args[3]
	var wsPort string
	if len(os.Args) > 4 {
		wsPort = os.Args[4]
	} else {
		wsPort = "8080"
	}
	go func() {
		server.ServeMetricNotifications(ctx, broker, group, topic, ":"+wsPort)
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
