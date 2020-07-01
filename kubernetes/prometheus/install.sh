#!/bin/bash

if helm list | grep -q prometheus; then
    helm upgrade prometheus stable/prometheus -f values.yml
else
    helm install prometheus stable/prometheus -f values.yml
fi