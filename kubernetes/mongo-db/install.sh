#!/bin/bash

if helm list | grep -q mongo-db; then
    helm upgrade mongo-db bitnami/mongodb -f values.yaml
else
    helm install mongo-db bitnami/mongodb -f values.yaml
fi