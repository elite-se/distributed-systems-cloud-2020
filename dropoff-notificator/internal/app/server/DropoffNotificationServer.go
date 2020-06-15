package server

import (
	"context"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"sync"
)

// ServeDropoffNotifications allows clients to connect via TCP to get informed about dropoffs in a specific cell
func ServeDropoffNotifications(ctx context.Context, broker string, group string, listenAddress string) {
	var wg sync.WaitGroup
	wg.Add(1) // TODO adjust

	messageChan := make(chan kafka.Message, 10)

	go func() {
		defer wg.Done()
		receiveKafkaMessages(ctx, broker, group, messageChan)
	}()

	go func() {
		defer wg.Done()
		ListenForTCPConnects(ctx, listenAddress)
	}()

	// TODO connect Kafka with TCP server

	wg.Wait()
}
