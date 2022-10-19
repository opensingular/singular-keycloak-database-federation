#!/usr/bin/env bash
read -p "Enter absolute path of Keycloak folder: " pathKeycloak

mvn clean package && cp ./dist/* "$pathKeycloak"/providers