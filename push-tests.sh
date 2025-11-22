#!/bin/bash

USERNAME="oleksandrhk"
TAG="latest"

#Add tag to local image
docker tag nbank-tests "${USERNAME}/nbank-tests:${TAG}"

#login
docker login -u ${USERNAME}

#push to hub
echo "${USERNAME}/nbank-tests:${TAG}"
docker push "${USERNAME}/nbank-tests:${TAG}"