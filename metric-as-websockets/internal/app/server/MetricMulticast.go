package server

import (
	"context"
	"log"
	"sync"
)

type Cell struct {
	X int
	Y int
}

type MetricEvent struct {
	Cell   Cell
	Metric float64
}

type MetricMulticast interface {
	register(outputChan chan MetricEvent) int
	unregister(handle int)
	startBroking(ctx context.Context)
	getCachedMetrics() *sync.Map
}

type metricMulticast struct {
	outputChans   map[int]chan MetricEvent
	nextHandleId  int
	inputChan     chan MetricEvent
	cachedMetrics sync.Map
}

func NewMetricMulticast(inputChan chan MetricEvent) MetricMulticast {
	return &metricMulticast{
		outputChans:   make(map[int]chan MetricEvent),
		nextHandleId:  0,
		inputChan:     inputChan,
		cachedMetrics: sync.Map{},
	}
}

func (broker *metricMulticast) startBroking(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return
		case msg := <-broker.inputChan:
			log.Printf("Processing event: Cell (%v, %v), Value %v\n", msg.Cell.X, msg.Cell.Y, msg.Metric)

			// cache value
			_, alreadyExisted := broker.cachedMetrics.LoadOrStore(msg.Cell, msg.Metric)
			if alreadyExisted {
				broker.cachedMetrics.Store(msg.Cell, msg.Metric)
			} else {
				RegisterNewCachedCell()
			}

			// distribute event
			for _, outputChan := range broker.outputChans {
				select {
				case outputChan <- msg:
					continue
				default:
					continue
				}
			}
			RegisterProcessedMessage()
		}
	}
}

func (broker *metricMulticast) register(outputChan chan MetricEvent) int {
	handleId := broker.nextHandleId
	broker.nextHandleId += 1
	broker.outputChans[handleId] = outputChan
	return handleId
}

func (broker *metricMulticast) unregister(handleId int) {
	delete(broker.outputChans, handleId)
}

func (broker *metricMulticast) getCachedMetrics() *sync.Map {
	return &broker.cachedMetrics
}
