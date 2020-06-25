# Distributed Systems Demo 2020

![Project summary](diagrams/project_overview.png)

## Demo Use Case

[New York City Taxi Dataset](https://www1.nyc.gov/site/tlc/about/tlc-trip-record-data.page)

![Taxi Data Mindmap](diagrams/taxi_data_mindmap.JPG)

**Basic idea**: 
- The cloud team hosts a small Kubernetes Cluster (hosted by Google Cloud) with basic monitoring services
- Microservices and other services can be deployed in the cluster (using configuration files)
- The participating teams exchange data to make the demonstration more interesting

**The Relationships in Detail**:

![TaxiProxyGo](https://user-images.githubusercontent.com/16650999/85385333-75f9de00-b542-11ea-94b2-7f47f38bd6d7.jpg)

## Teams

- Cloud Technologien in Practice
- Microservice Architectures in Practice
- Kafka
- GoLang
- AWS Load Balancer

## Setup

### Build docker containers

1. taxi-trip-converter: `docker build -f taxi-trip-converter/Dockerfile -t taxi-trip-converter .`
2. taxi-trip-metrics: `docker build -f taxi-trip-metrics/Dockerfile -t taxi-trip-metrics .`

### Start docker containers

1. taxi-trip-converter: `docker run --name taxi-trip-converter taxi-trip-converter .`
2. taxi-trip-metrics: `docker run --name taxi-trip-metrics taxi-trip-metrics .`
