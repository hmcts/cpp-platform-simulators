#!/usr/bin/env bash

# Script that runs, liquibase, deploys wars and runs integration tests
CONTEXT_NAME=simulators

FRAMEWORK_LIBRARIES_VERSION=17.101.2
FRAMEWORK_VERSION=17.101.6
EVENT_STORE_VERSION=17.101.5

DOCKER_CONTAINER_REGISTRY_HOST_NAME=crmdvrepo01

#fail script on error
set -e

[ -z "$CPP_DOCKER_DIR" ] && echo "Please export CPP_DOCKER_DIR environment variable pointing to cpp-developers-docker repo (https://github.com/hmcts/cpp-developers-docker) checked out locally" && exit 1
WILDFLY_DEPLOYMENT_DIR="$CPP_DOCKER_DIR/containers/wildfly/deployments"

source $CPP_DOCKER_DIR/docker-utility-functions.sh
source $CPP_DOCKER_DIR/build-scripts/integration-test-scipt-functions.sh

#first deploy any other normal context such as subscription on docker so the wildfly container gets created. then only run this script else you will get error as below
# container not found or path not found
buildDeployAndTest() {
  loginToDockerContainerRegistry
  mvn clean install
  undeployWarsFromDocker
  buildAndStartContainers
  rm -rf $WILDFLY_DEPLOYMENT_DIR/*.deployed
  rm -rf $WILDFLY_DEPLOYMENT_DIR/*.war
  find . -iname "*.war"  -exec cp {} $WILDFLY_DEPLOYMENT_DIR \;
  docker cp $CPP_DOCKER_DIR/containers/wildfly/deployments containers-cpp-wildfly-1:/opt/jboss/wildfly/standalone
}

buildDeployAndTest
