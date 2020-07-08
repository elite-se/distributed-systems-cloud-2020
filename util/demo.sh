#!/bin/bash

while [ true ]
do
    curl -I -s http://maw-map.cloud.elite-se.xyz:30967 | grep HTTP
    date
    echo "------"
    sleep 1
done