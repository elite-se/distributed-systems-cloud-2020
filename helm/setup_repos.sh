#!/bin/bash

helm repo add stable https://kubernetes-charts.storage.googleapis.com/
helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
helm repo update