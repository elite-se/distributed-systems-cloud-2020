package server

import (
	"context"
	"sync"
)

// ServeDropoffNotifications allows clients to connect via TCP to get informed about dropoffs in a specific cell
func ServeDropoffNotifications(ctx context.Context, broker string, group string, topic string, wsAddr string, tcpAddr string) {
	var wg sync.WaitGroup
	wg.Add(4)

	messageChan := make(chan DropoffEvent, 10)

	go func() {
		defer wg.Done()
		receiveKafkaMessages(ctx, broker, group, topic, messageChan)
	}()

	eventBroker := NewDropoffEventBroker(messageChan)
	go func() {
		defer wg.Done()
		eventBroker.startBroking(ctx)
	}()

	go func() {
		defer wg.Done()
		ListenForTCPConnects(ctx, tcpAddr, eventBroker)
	}()

	go func() {
		defer wg.Done()
		ListenForWSConnects(ctx, wsAddr, eventBroker)
	}()

	wg.Wait()
}
