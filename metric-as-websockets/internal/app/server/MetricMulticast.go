package server

import (
	"context"
	"log"
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
}

type metricMulticast struct {
	outputChans  map[int]chan MetricEvent
	nextHandleId int
	inputChan    chan MetricEvent
}

func NewMetricMulticast(inputChan chan MetricEvent) MetricMulticast {
	return &metricMulticast{
		outputChans:  make(map[int]chan MetricEvent),
		nextHandleId: 0,
		inputChan:    inputChan,
	}
}

func (broker *metricMulticast) startBroking(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return
		case msg := <-broker.inputChan:
			log.Printf("Distributing event: Cell (%v, %v), Value %v\n", msg.Cell.X, msg.Cell.Y, msg.Metric)
			for _, outputChan := range broker.outputChans {
				select {
				case outputChan <- msg:
					continue
				default:
					continue
				}
			}
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
