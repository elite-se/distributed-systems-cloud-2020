package server

import (
	"context"
	"fmt"
)

type Cell struct {
	x int
	y int
}

type DropoffEvent struct {
	cell   Cell
	metric float64
}

type DropoffEventBroker interface {
	register(cell Cell, infoChan chan float64) int
	unregister(handle int)
	startBroking(ctx context.Context)
}

type handle struct {
	id       int
	infoChan chan float64
}

type dropoffEventBrokerImpl struct {
	handles      map[Cell][]handle
	nextHandleId int
	inputChan    chan DropoffEvent
}

func NewDropoffEventBroker(inputChan chan DropoffEvent) DropoffEventBroker {
	return dropoffEventBrokerImpl{
		handles:      make(map[Cell][]handle),
		nextHandleId: 0,
		inputChan:    inputChan,
	}
}

func (broker dropoffEventBrokerImpl) startBroking(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return
		case msg := <-broker.inputChan:
			fmt.Printf("Broker: Cell (%v, %v), Value %v\n", msg.cell.x, msg.cell.y, msg.metric)
			key := msg.cell
			handles := broker.handles[key]
			if handles != nil {
				for _, handle := range handles {
					select {
					case handle.infoChan <- msg.metric:
						continue
					default:
						continue
					}
				}
			}
		}
	}
}

func (broker dropoffEventBrokerImpl) register(cell Cell, infoChan chan float64) int {
	handles := broker.handles[cell]
	if handles == nil {
		handles = make([]handle, 1)
	}
	handleId := broker.nextHandleId
	broker.nextHandleId++
	handles = append(handles, handle{
		id:       handleId,
		infoChan: infoChan,
	})
	broker.handles[cell] = handles
	return handleId
}

func (broker dropoffEventBrokerImpl) unregister(handleId int) {
	for outerIdx, handles := range broker.handles {
		for idx, handle := range handles {
			if handle.id == handleId {
				handles[idx] = handles[len(handles)-1]
				broker.handles[outerIdx] = handles[:len(handles)-1]
			}
		}
	}
}
