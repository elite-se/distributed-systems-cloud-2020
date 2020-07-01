#!/bin/bash

if helm list | grep -q weave-scope; then
    helm upgrade weave-scope stable/weave-scope -f values.yml
else
    helm install weave-scope stable/weave-scope -f values.yml
fi