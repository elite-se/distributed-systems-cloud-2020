package server

import (
	"context"
	"sync"
)

// ServeMetricNotifications allows clients to connect via WebSockets to get informed about metric changes
func ServeMetricNotifications(ctx context.Context, broker string, group string, topic string, addr string) {
	var wg sync.WaitGroup
	wg.Add(4)

	messageChan := make(chan MetricEvent, 10)

	go func() {
		defer wg.Done()
		receiveKafkaMessages(ctx, broker, group, topic, messageChan)
	}()

	eventBroker := NewMetricMulticast(messageChan)
	go func() {
		defer wg.Done()
		eventBroker.startBroking(ctx)
	}()

	go func() {
		defer wg.Done()
		ListenForWSConnects(ctx, addr, eventBroker)
	}()

	go func() {
		defer wg.Done()
		ListenForPrometheusRequests(ctx, addr)
	}()

	wg.Wait()
}
