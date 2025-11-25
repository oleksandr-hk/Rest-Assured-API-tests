#!/bin/bash

#Running application containers (front-end/back-end/nginx/selenoid/selenoid-ui)
sh restart_docker.sh

#Executiong tests
sh ../../run-tests.sh -p api
sh ../../run-tests.sh -p ui