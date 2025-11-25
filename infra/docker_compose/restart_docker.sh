#!/bin/bash

echo ">>> Stop Docker Compose"
docker compose down

echo ">>> Docker pull all browser images"

#file with browsers configs
json_file="./config/browsers.json"

#check if jq installed
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

#extracting all browser versions from browsers.json
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")


#iterate through each image and do git pul
for image in $images; do
  echo "Pulling $image.."
  docker pull "$image"
done

echo ">>> Launch Docker compose"
docker compose up -d