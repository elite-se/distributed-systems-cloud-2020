package server

import (
	"context"
	"net/http"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

func ListenForPrometheusRequests(ctx context.Context, address string) {
	http.Handle("/metrics", promhttp.Handler())

	server := &http.Server{Addr: address}
	go server.ListenAndServe()
	<-ctx.Done()
	_ = server.Shutdown(ctx)
}

func RegisterProcessedMessage() {
	msgProcessed.Inc()
}

func RegisterConnectionOpened() {
	connectionsOpen.Inc()
	connectionsTotal.Inc()
}

func RegisterConnectionClosed() {
	connectionsOpen.Dec()
}

func SetCacheCount(val int) {
	cacheCount.Set(float64(val))
}

var (
	msgProcessed = promauto.NewCounter(prometheus.CounterOpts{
		Name: "metric_as_webservice_processed_msg_total",
		Help: "The total number of processed messages received from kafka",
	})
	connectionsOpen = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "metric_as_webservice_connections_current",
		Help: "The current number of open websocket connections",
	})
	connectionsTotal = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "metric_as_webservice_connections_total",
		Help: "The total number of websocket connections",
	})
	cacheCount = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "metric_as_webservice_cache_count_current",
		Help: "The current number of cached cell metric values",
	})
)
