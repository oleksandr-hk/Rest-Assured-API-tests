#!/bin/bash

cd ../..

required_coverage=30

coverage=$(jq -r '
  (.coverageOperationMap.counter.full / .coverageOperationMap.counter.all) * 100
' swagger-coverage-results.json)

coverage=${coverage%.*} #

if [ "$coverage" -lt $required_coverage ]; then
  echo "Not enough API coverage! Current API code Coverage: ${coverage}%"
  echo "Desired API coverage: ${required_coverage}%"
  exit 1
fi
