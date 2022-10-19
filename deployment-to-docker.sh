#!/usr/bin/env bash
read -p "Enter container name of Keycloak: " containerName
containerId=$(docker ps -aqf "name=$containerName$")
echo "container id= $containerId"

mvn clean package && docker cp ./dist/. "$containerId":/opt/keycloak/providers
