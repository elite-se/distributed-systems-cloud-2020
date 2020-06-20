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
	topic := os.Args[3]
	var wsPort, tcpPort string
	if len(os.Args) > 4 {
		wsPort = os.Args[4]
		if len(os.Args) > 5 {
			tcpPort = os.Args[5]
		} else {
			tcpPort = "3000"
		}
	} else {
		wsPort = "8080"
	}
	go func() {
		server.ServeDropoffNotifications(ctx, broker, group, topic, ":"+wsPort, ":"+tcpPort)
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
