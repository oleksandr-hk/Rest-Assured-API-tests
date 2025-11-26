#!/bin/bash

#Running application containers (front-end/back-end/nginx/selenoid/selenoid-ui)
sh restart_docker.sh

#Executiong tests
cd ../../
sh run-tests.sh -p api,ui