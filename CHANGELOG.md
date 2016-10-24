# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

## [0.32.0] - 2016-10-24

### Added
- JsonEnvelope.asJsonObject() for simple conversion of the entire envelope to a JsonObject
- EventStreamMatcher to match stream of JsonEnvelopes appended
- RestPoller. A polling REST client utility for integration tests
- Mechanism for taking snapshots of aggregates, disabled by default. To enable, add a dependency on
the `aggregate-snapshot-service` module. Requires the `aggregate-snapshot-repository-liquibase`
schema to be applied to the event store.
- JSON schema matching support in the JsonEnvelopeMatcher

### Changed
- JsonEnvelopeMetadataMatcher to allow matching with JSON paths
- Deprecated PollingRestClient in favour of the new RestPoller

### Fixed
- RestProcessor now allows metadata from payload to be merged with headers instead of being
overwritten

## [0.31.0] - 2016-10-24

- Failed release; do not use

## [0.30.0] - 2016-10-11

### Fixed
- Event buffer connection held open after exception in event listener
- RestClientProcessor tests clashing using common 8080 port
- JMS listeners for event topics did not use shared subscriptions. Fixing this allows multiple
instances to run. Each message will be consumed only once across a cluster.

### Added
- Hamcrest Matchers for asserting JsonEnvelopes contained within either Streams or Lists
- New Clock interface to replace DateTimeProvider
- EnumPicker in random generators

### Changed
- DateTimeProvider is now deprecated - use Clock instead
- JsonEnvelopeBuilder is now deprecated - use DefaultJsonEnvelope.envelope() instead
- Extend DefaultJsonEnvelope.Builder to accept JsonObject as part of the payload

### Removed
- Unused JdbcConnectionProvider utility

## [0.29.0] - 2016-09-30

### Fixed
- 'Response is closed' bug in PollingRequestClient

### Added
- ZonedDateTimes methods to convert between ZDT and SQL timestamps
- LocalDate conversion functions
- EnveloperFactory method to create a Enveloper with a list of Event classes registered
- EventStreamMockHelper that provides a method to verify and return the Stream argument from a call 
to the EventStream append method
- DateProvider to allow easy mocking of date creating for testing
- Random generators and TypeCheck class for type-based testing
- Metrics per action name

### Changed
- Event Store to include a 'date_created' timestamp field - added to the DB schema but not yet
enabled in code

## [0.28.0] - 2016-09-21

### Fixed
- EventBufferInterceptor container discovery

## [0.27.0] - 2016-09-20
### Added
- JsonSchemaValidationMatcher class to validate json content against a schema
- PollingRequestClient class as a new implementation of the HttpResponsePoller

### Changed
- Moved EventBufferInterceptor and EventFilterInterceptor to event-listener component
- Added withHeaders method to HttpResponsePoller

### Fixed
- BaseTransactionalTest to cleanup with rollback

### Deprecated
- HttpResponsePoller, replaced with PollingRestClient

## [0.26.0] - 2016-09-20
- Failed release; do not use

## [0.25.0] - 2016-09-12
### Added
- sender.sendAsAdmin() method

### Fixed
- NPE JsonSenderWrapper

## [0.24.0] - 2016-09-08

### Added
- Configurable JNDI Auditing blacklist regex of action names in AuditingService
- Expose component metrics via Rest and JMX
- TestJdbcConnectionProvider: for getting hard out of container connections
to the event and view stores
- DatabaseCleaner: allows easy clearing of database tables for integration tests


## [0.23.0] - 2016-09-01

### Added
- persistence-jdbc to the framework bom


## [0.22.0] - 2016-08-31

### Added
- Add JDBC Connection classes for easy access to event store and view store databases

### Fixed
- Fixed message producer client to use a system property to override the queue url

## [0.21.0] - 2016-08-30

### Added
- Add json object value matching to HttpResponsePoller utility
- Add a 'Request as Admin' request to the requester

### Fixed
- HttpResponsePoller conditional poll fixed

## [0.19.0] - 2016-08-26

### Added
- Dispatcher interceptor integration. Interceptors are chained between the adapter and the dispatcher 
according to a priority setting.  Interceptors implement the Interceptor interface and are registered 
at startup.  
Implemented interceptors: Event Buffer Interceptor, Access Control Interceptor
- Event Listener Filter
- Additional features to the HttpResponsePoller utility

### Fixed
- Remote Client unable to POST to REST end point with media type that is different to action name
- SenderProducer support for @FrameworkComponent annotated Senders


## [0.18.0] - 2016-08-22

### Added
- test-utils-persistence to the framework bom

### Fixed
- CORS response header mismatch
- JMS Message Producer test-util connection closing issue


## [0.17.0] - 2016-08-19

### Fixed
- Travis build file settings
- Javadoc build issues


## [0.16.0] - 2016-08-18

### Added
- MessageProducerClient in test-utils for easy integration testing of sending messages
- UuidStringMatcher: A Hamcrest matcher for asserting that a string is a valid UUID
- EnvelopeFactory: for creating a simple JsonEnvelope for testing

### Changed
- Updated to use DeltaSpike container managed persistence and transaction

### Removed
- Remove asynchronous and synchronous differences from Dispatcher


## [0.15.0] - 2016-08-11

### Added
- Initial implementation of event ordering buffer for event listeners
- Add a simple Audit Client
- Add a toDebugStringPrettyPrinted() method that returns the JsonEnvelope as JSON

### Changed
- Move the logic of JsonEnvelopeLoggerHelper to the toString() method of JsonEnvelope
- Move EnveloperFactory to test utils


## [0.14.0] - 2016-08-08

### Added
- Add a match all and do nothing EventMatcher to the EventSwitcher. This will ignore all other events.
Example:
```
match(event).with(
    when(SomethingAdded.class).apply(x -> id = x.getId()),
    otherwiseDoNothing());
```
- Added test utility classes for logger producer, test messaging client and other http utilities.

### Changed
- Removed schema validation for requests without a payload (actions without schema/example specified in raml and denoted with !!null).

Example (Two actions, the second has no payload):
```
application/vnd.people.modified-user+json:
    schema: !include json/schema/people.modified-user.json
    example: !include json/update-user.json
application/vnd.people.link-user+json: !!null
```

## [0.13.0] - 2016-07-28

### Added
- Service component passthrough test utility; test Command API, Command Controller, Query API and
Query Controller as passthrough services
- Support for listening to all events on a topic or queue

### Fixed
- Causation HTTP header not sent by the REST client

### Changed
- Removed limitation on messaging RAML resources to allow any queue or topic name

## [0.12.3] - 2016-07-19

### Fixed
- 403 response from REST calls caused 500 to be returned
- Stream order not guaranteed when retrieving events from the event store

## [0.12.0] - 2016-07-14

### Changed
- Use new parent POM and common POM projects

### Fixed
- Prevent access control triggering for remote handlers
- Ignore GET resources when generating JMS adapters
- Support for Event API components in RAML

### Removed
- Action mapper annotation now always required in REST RAML; it is no longer possible to disable the
action mapper system

## [0.11.0] - 2016-07-08

### Added
- Access control violations now return 403 forbidden response
- Support for Event API components in RAML

### Fixed
- Access control provider scanning and bean injection
- Event processor and JEE dependencies were incorrect
- Component module dependencies cleaned up and made consistent

## [0.10.1] - 2016-07-05

### Fixed

- Set framework BOM to POM packaging

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
