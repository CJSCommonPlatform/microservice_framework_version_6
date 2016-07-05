# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines 
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to 
[Semantic Versioning](http://semver.org/).

## [0.10.0] - 2016-07-05

### Added

- Support for generating messaging clients from RAML
- Added EVENT_API service component type
- Support for simple access control with a default implementation that allows all requests
- New ZonedDateTimes utility class for converting JSON strings to UTC date times
- New JsonEnvelopeBuilder test utility class for building envelopes
- Bill of materials (BOM) for easier framework module dependency management in other Maven projects

### Changed

- Object mapper configured to exclude nulls from JSON
- Ensure dates are stored and retrieved in UTC format
- Refactor DispatcherProvider into component parts
- Updated USER_ID constant in response to change by IDAM
- Updated COMMAND_API service component type to an http default input

## [0.9.0] - 2016-06-15

### Changed

- Messaging adapter generator now uses Java Poet for code generation instead of a template

### Fixed

- Fix RAML action mapping description to allow other description text surrounding the mapping
- Fix action mapper to support media types with different character sets

## [0.8.12] - 2016-06-13

### Added

- Support for [types](https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#type) on query parameters in the REST adapter generator
- Initial [action mapping](https://github.com/CJSCommonPlatform/microservice_framework/wiki/User-Guide#rest-adapter-generator) support for the REST adapter generator,
turned off by default

### Fixed

- Fix media type handling in the test utilities REST client

## [0.8.11] - 2016-06-07

### Changed

- Ensure Jackson always adds a timezone when marshalling timestamps
- Allow JAX-RS providers to be overridden in generated REST adapters

### Fixed
- Do not change port on REST requests to service components within the current service

## [0.8.10] - 2016-06-03

### Added

- Configuration option to control REST client port via a system property
- Add user id, session id and correlation id to REST headers for remote calls
- JSON schema validation error [logging](https://github.com/CJSCommonPlatform/microservice_framework/wiki/User-Guide#logging)

## [0.8.9] - 2016-05-31

### Changed

- Allow senders to be annotated at the field level

### Added

- Add ListToJsonArrayConverter

## [0.8.8] - 2016-05-31

### Added

- Requester injection for non-handler classes
- Skip generation flag for RAML plugin
- Metadata handling for REST client queries
- POST support for REST clients
- Persistence module and example for Deltaspike integration testing

## [0.8.7] - 2016-05-26

### Added

- CORS filter

## [0.8.6] - 2016-05-26

### Added

- Message trace [logging](https://github.com/CJSCommonPlatform/microservice_framework/wiki/User-Guide#logging)
- JEE integration test support
- REST client test-utils module
- Version now supplied in event metadata

## [0.8.5] - 2016-05-23

### Changed

- Revert to consistently use Jackson 2.6.4

## [0.8.4] - 2016-05-23

### Changed

- Use Jackson 2.7.4

## [0.8.3] - 2016-05-23

### Changed

- Relax event type validation to accept plural to ease context migration to new framework

## [0.8.2] - 2016-05-19

### Added

- Support for remote service invocation

## [0.8.1] - 2016-05-12

### Added

- Add annotation support for properties
- Add JSON schema validation support
- Support 404 response
- Optional query parameter support
- Support for endpoint exclusion in RAML rest generator
- Add aggregate service and helper functions

### Changed

- Convert REST adapter generator to use Java Poet
- Disable WRITE_DATES_AS_TIMESTAMPS in Jackson

### Fixed

- Fix generator duplicate class detection for multi-platform support

## [0.8.0] - 2016-04-22

### Added

- Initial release with basic dispatcher, handler and adapter generation
