package server

import (
	"context"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"sync"
)

// ServeDropoffNotifications allows clients to connect via TCP to get informed about dropoffs in a specific cell
func ServeDropoffNotifications(ctx context.Context, broker string, group string, listenAddress string) {
	var wg sync.WaitGroup
	wg.Add(3)

	messageChan := make(chan kafka.Message, 10)

	go func() {
		defer wg.Done()
		receiveKafkaMessages(ctx, broker, group, messageChan)
	}()

	eventBroker := NewDropoffEventBroker(messageChan)
	go func() {
		defer wg.Done()
		eventBroker.startBroking(ctx)
	}()

	go func() {
		defer wg.Done()
		ListenForTCPConnects(ctx, listenAddress, eventBroker)
	}()

	wg.Wait()
}
