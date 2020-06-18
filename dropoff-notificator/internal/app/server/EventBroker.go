package server

import (
	"context"
	"github.com/confluentinc/confluent-kafka-go/kafka"
)

type DropoffEvent struct {
	msg string // TODO put real data here
}

type DropoffEventBroker interface {
	register(request string, infoChan chan DropoffEvent) int
	unregister(handle int)
	startBroking(ctx context.Context)
}

type handle struct {
	id       int
	infoChan chan DropoffEvent
}

type dropoffEventBrokerImpl struct {
	handles      map[string][]handle
	nextHandleId int
	inputChan    chan kafka.Message
}

func NewDropoffEventBroker(inputChan chan kafka.Message) DropoffEventBroker {
	return dropoffEventBrokerImpl{
		handles:      make(map[string][]handle),
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
			key := string(msg.Value)[:4] // TODO find real key
			handles := broker.handles[key]
			if handles != nil {
				for _, handle := range handles {
					select {
					case handle.infoChan <- DropoffEvent{msg: string(msg.Value)}:
						continue
					default:
						continue
					}
				}
			}
		}
	}
}

func (broker dropoffEventBrokerImpl) register(request string, infoChan chan DropoffEvent) int {
	handles := broker.handles[request]
	if handles == nil {
		handles = make([]handle, 1)
	}
	handleId := broker.nextHandleId
	broker.nextHandleId++
	handles = append(handles, handle{
		id:       handleId,
		infoChan: infoChan,
	})
	broker.handles[request] = handles
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
