#!/usr/bin/env bash
set -euo pipefail

required_coverage=30
report_file="swagger-coverage-results.json"

coverage=$(jq -r '
  (.coverageOperationMap.counter.full / .coverageOperationMap.counter.all) * 100
' "$report_file")

echo "Current coverage as per report: ${coverage}"

coverage=${coverage%.*}

if (( coverage < required_coverage )); then
  echo "::error::Not enough API coverage!"
  echo "Current API coverage: ${coverage}%"
  echo "Required API coverage: ${required_coverage}%"
  exit 1
fi
