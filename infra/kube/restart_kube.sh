#!/bin/bash
minikube start --driver=docker

kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

helm install nbank ./nbank-chart

kubectl get svc

kubectl get pods

kubectl logs deployment/backend

kubectl rollout status deployment/backend
kubectl rollout status deployment/frontend
kubectl rollout status deployment/selenoid
kubectl rollout status deployment/selenoid-ui

#port forward to local machine
kubectl port-forward svc/frontend 3000:80 > /dev/null 2>&1 &
kubectl port-forward svc/backend 4111:4111 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid 4444:4444 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid-ui 8080:8080 > /dev/null 2>&1 &
