# Microservice Framework

[![Build Status](https://travis-ci.org/CJSCommonPlatform/microservice_framework.svg?branch=master)](https://travis-ci.org/CJSCommonPlatform/microservice_framework) [![Coverage Status](https://coveralls.io/repos/github/CJSCommonPlatform/microservice_framework/badge.svg?branch=master)](https://coveralls.io/github/CJSCommonPlatform/microservice_framework?branch=master)

A Java framework to support applications utilising CQRS and Event Sourcing architectures.

## Modules

* adapters - Supports incoming requests from REST and JMS clients. Adapts requests to the internal framework representation (JSON envelope)
* clients - Support outgoing REST/HTTP requests to application components/external services
* common - Shared utility classes
* components - Base for application components
* core - Core framework logic
* domain - Framework domain classes
* event-sourcing - Event sourcing implementation
* example-context - Example applicating based on the framework
* messaging-core - Internal messaging representations
* messaging-jms - Communication with JMS queues/topics
* metrics - Support for health checks in application components
* persistence - Support for persistence layer, such as deltaspike.

## How to build and deploy locally without access to build repository

#### Project dependencies
Clone the following CJSCommonPlatform projects into the same directory level:
* [maven-super-pom](https://github.com/CJSCommonPlatform/maven-super-pom)
* [maven-parent-pom](https://github.com/CJSCommonPlatform/maven-parent-pom)
* [maven-common-bom](https://github.com/CJSCommonPlatform/maven-common-bom)
* [maven-common](https://github.com/CJSCommonPlatform/maven-common)
* [raml-maven](https://github.com/CJSCommonPlatform/raml-maven)
* [embedded-artemis](https://github.com/CJSCommonPlatform/embedded-artemis)
* [maven-framework-parent-pom](https://github.com/CJSCommonPlatform/maven-framework-parent-pom)
* [test-utils](https://github.com/CJSCommonPlatform/test-utils)
* [utilities](https://github.com/CJSCommonPlatform/utilities)
* [file-service](https://github.com/CJSCommonPlatform/file-service)
* [json-schema](https://github.com/CJSCommonPlatform/json-schema)
* [microservice_framework](https://github.com/CJSCommonPlatform/microservice_framework)

#### Run dependency installation script
Run the install-dependencies.sh script from the microservice_framework directory.  This will checkout 
and install the required versions of each project.

`./install-dependencies.sh`

#### Finally build and verify the Microservice Framework.

`mvn clean verify`
