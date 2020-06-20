package server

import (
	"context"
	"encoding/json"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
)

func ListenForWSConnects(ctx context.Context, address string, broker MetricMulticast) {
	upgrader := websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
	}
	upgrader.CheckOrigin = func(r *http.Request) bool { return true }

	http.HandleFunc("/", func(rw http.ResponseWriter, r *http.Request) {
		conn, err := upgrader.Upgrade(rw, r, nil)
		if err != nil {
			log.Println(err)
			return
		}
		handleWSConnection(ctx, conn, broker)
	})
	_ = http.ListenAndServe(address, nil)
}

func handleWSConnection(ctx context.Context, conn *websocket.Conn, broker MetricMulticast) {
	defer conn.Close()

	// send info whenever there is one
	eventsChan := make(chan MetricEvent, 3)
	defer close(eventsChan)
	handle := broker.register(eventsChan)
	defer broker.unregister(handle)
	for {
		select {
		case event := <-eventsChan:
			bytes, err := json.Marshal(event)
			if err != nil {
				log.Println(err)
			} else {
				_ = conn.WriteMessage(websocket.TextMessage, bytes)
			}
		case <-ctx.Done():
			return
		}
	}
}
