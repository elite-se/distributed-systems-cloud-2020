#!/bin/bash

if helm list | grep -q ha-proxy-ingress; then
    helm upgrade ha-proxy-ingress haproxytech/kubernetes-ingress --values values.yml
else
    helm install ha-proxy-ingress haproxytech/kubernetes-ingress -f values.yml
fi