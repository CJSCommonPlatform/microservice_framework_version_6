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
