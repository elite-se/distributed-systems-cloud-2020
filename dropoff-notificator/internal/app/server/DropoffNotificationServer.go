package server

import (
	"context"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"sync"
)

// ServeDropoffNofitications allows clients to connect via TCP to get informed about dropoffs in a specific cell
func ServeDropoffNofitications(ctx context.Context, broker string, group string) {
	var wg sync.WaitGroup
	wg.Add(1) // TODO adjust

	messageChan := make(chan kafka.Message, 10)

	go func() {
		defer wg.Done()
		receiveKafkaMessages(ctx, broker, group, messageChan)
	}()

	// TODO allow clients to connect and notify them

	wg.Wait()
}
