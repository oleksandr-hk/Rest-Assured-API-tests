#!/bin/bash

# Configure image name
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1: -api} # launch param
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# Build image based on dockerfile
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

#Run docker container
docker run --rm \
  -v "$TEST_OUTPUT_DIR"/logs:/app/logs \
  -v "$TEST_OUTPUT_DIR"/results:/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR"/report:/app/target/site \
  -e TEST_PROFILE="${TEST_PROFILE}" \
  -e APIBASEURL=http://192.168.0.203:4111 \
  -e UIBASEURL=http://192.168.0.203:3000 \
$IMAGE_NAME

# Echo summary
echo ">>> Tests completed"
echo "Log file: $TEST_OUTPUT_DIR/logs/run.log"
echo "Test results: $TEST_OUTPUT_DIR/results"
echo "Final report: $TEST_OUTPUT_DIR/report"