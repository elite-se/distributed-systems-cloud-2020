#!/bin/bash

helm repo add stable https://kubernetes-charts.storage.googleapis.com/
helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
helm repo add haproxytech https://haproxytech.github.io/helm-charts
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update