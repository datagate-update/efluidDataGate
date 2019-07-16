#!/bin/bash

## BASIC BUILD OF DOCKER INSTANCE IN STANDALONE MODE

## MVN build
docker run -it --rm \
    -v ~/.m2:/root/.m2 \
    -v $(pwd):/usr/src/mymaven \
    -w /usr/src/mymaven maven:3.5-jdk-10 /bin/bash \
    -c "mvn --batch-mode -pl '!datagate-app-tests' install -DskipTests; cp /usr/src/mymaven/datagate-app/src/docker/build-desktop/standalone-with-h2/* /usr/src/mymaven/datagate-app/target"

## Docker build
docker build -t datagate:latest-h2 ./datagate-app/target

## Completed
echo "datagate-gest standalone build completed"
