#!/bin/bash

if helm list | grep -q kafka; then
    helm upgrade kafka incubator/kafka -f values.yml
else
    helm install kafka incubator/kafka -f values.yml
fi