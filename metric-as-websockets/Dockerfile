FROM golang:1.14.4-buster
RUN mkdir /app
ADD . /app
WORKDIR /app
# Add this go mod download command to pull in any dependencies
RUN go mod download
# Our project will now successfully build with the necessary go libraries included.
RUN go build -o main ./cmd/server/server.go
# Our start command which kicks off
# our newly created binary executable
ENV KAFKA_BROKER kafka.cloud.elite-se.xyz:31090
ENV KAFKA_GROUP websocket-metrics
ENV KAFKA_TOPIC cell-profit
EXPOSE 80
CMD /app/main ${KAFKA_BROKER} ${KAFKA_GROUP} ${KAFKA_TOPIC} 80
