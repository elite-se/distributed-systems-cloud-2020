package server

import (
	"context"
	"encoding/binary"
	"errors"
	"fmt"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"math"
	"os"
	"regexp"
	"strconv"
)

func receiveKafkaMessages(ctx context.Context, broker string, group string, topic string, messageChan chan<- MetricEvent) {
	c, err := kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":     broker,
		"broker.address.family": "v4",
		"group.id":              group,
		"session.timeout.ms":    6000,
		"auto.offset.reset":     "earliest"})
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "Failed to create consumer: %s\n", err)
		os.Exit(1)
	}
	//noinspection GoUnhandledErrorResult
	defer c.Close()

	err = c.Subscribe(topic, nil)

	for {
		select {
		case <-ctx.Done():
			return
		default:
			ev := c.Poll(100)
			if ev == nil {
				continue
			}

			switch e := ev.(type) {
			case *kafka.Message:
				event, err := parseEvent(e)
				if err != nil {
					fmt.Fprintf(os.Stderr, "Failed to parse message: %v\n", err)
				} else {
					messageChan <- event
				}
			case kafka.Error:
				fmt.Fprintf(os.Stderr, "%% Error: %v: %v\n", e.Code(), e)
			default:
				fmt.Printf("Ignored %v\n", e)
			}
		}
	}
}

func parseEvent(e *kafka.Message) (MetricEvent, error) {
	// cell
	cell, err := parseCell(string(e.Key))
	if err != nil {
		return MetricEvent{}, err
	}

	// metric value
	bits := binary.BigEndian.Uint64(e.Value)
	value := math.Float64frombits(bits)

	return MetricEvent{
		Cell:   cell,
		Metric: value,
	}, nil
}

func parseCell(cellString string) (Cell, error) {
	var cellMatcher = regexp.MustCompile(`^{"clat":([0-9]+),"clong":([0-9]+)}$`)
	cell := cellMatcher.FindStringSubmatch(cellString)
	if cell == nil {
		return Cell{}, errors.New("key was no proper cell")
	}
	x, err := strconv.Atoi(cell[1])
	if err != nil {
		return Cell{}, errors.New("invalid x coordinate")
	}
	y, err := strconv.Atoi(cell[2])
	if err != nil {
		return Cell{}, errors.New("invalid x coordinate")
	}
	return Cell{
		X: x,
		Y: y,
	}, nil
}
