FROM golang:1.14.4-buster
RUN mkdir /app
ADD . /app
WORKDIR /app
# Add this go mod download command to pull in any dependencies
RUN go mod download
# Our project will now successfully build with the necessary go libraries included.
RUN go build -o main ./cmd/server/server.go

FROM alpine:latest

ENV KAFKA_BROKER kafka.cloud.elite-se.xyz:31090
ENV KAFKA_GROUP websocket-metrics
ENV KAFKA_TOPIC cell-profit
EXPOSE 80

RUN mkdir /app
WORKDIR /app

COPY --from=0 /app/main .

# Our start command which kicks off
# our newly created binary executable

CMD /app/main ${KAFKA_BROKER} ${KAFKA_GROUP} ${KAFKA_TOPIC} 80
