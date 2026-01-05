This project builds a simulators for cpp platform

The war file should be deployed to wildfly where the simulator is needed

# Local testing
* To test this component locally, build the project using mvn clean package or install and then execute below command to copy and deploy the war to local wildfly server running as a docker container
`docker cp  cpp-platform-simulators/target/cpp-platform-simulators-*.war containers-cpp-wildfly-1:/opt/jboss/wildfly/standalone/deployments`

* To verify that deployment is successful, hit any endpoint exposed by this component from browser and see if success response is returned
`http://localhost:8080/simulator/__admin/mappings

# CI and release
Release pipeline is manual (similar to library components) i.e. on commit it just triggers verify pipeline for validating PR changes but does not publish any artifacts

To make a release [ADO validation pipeline](https://dev.azure.com/hmcts-cpp/cpp-apps/_build?definitionId=327&_a=summary) must be triggered manually by specifying the branch as input

This component uses `pipelines/library-validation.yaml@cppAzureDevOpsTemplates` validation template in azure pipeline yaml which
* Builds war artefact by executing mvn clean install
* Publishes war artefact to maven artefactory
* Wildfly Docker image is built by using [Dockerfile](docker/Dockerfile_platformsimulators-service) and it gets tagged and published to ACR (similar to contexts)

# Deployment to environments
Docker image tag version management follows the same hierarchical structure as contexts do

platform_simulators_image_tag is (or can be) defined at multiple levels as below
* [ste env](cpp-aks-deploy/blob/simulators-java-17/helmsman_vars/ste.env)
* [dev env](cpp-aks-deploy/blob/simulators-java-17/helmsman_vars/dev.env)

Above tag can be overridden in below cpp.pipeline project file (similar to contexts)
* [cpp.pipeline/aks-pipeline.versions.yml](/cpp.pipeline/+/refs/heads/main/aks-pipeline.versions.yml)

