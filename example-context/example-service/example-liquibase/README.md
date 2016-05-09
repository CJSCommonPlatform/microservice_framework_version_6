# Instructions for creating the database

1. Install Postgres 9.4 or later
2. Create a user called viewstore
3. Create a database called viewstore
4. Run with the following command:
    mvn resources:resources liquibase:update -Dliquibase.url=jdbc:postgresql://localhost:5432/viewstore -Dliquibase.username=viewstore -Dliquibase.password=password -Dliquibase.logLevel=info
   Or
    java -jar event-repository-jpa-liquibase-<version>.jar --url=jdbc:postgresql://localhost:5432/viewstore --defaultSchemaName=<schema> --username=viewstore --password=password --logLevel=info update

All tables can be dropped by running:

java -jar event-repository-jpa-liquibase-<version>.jar --url=jdbc:postgresql://localhost:5432/viewstore --username=viewstore --password=password --logLevel=info dropAll
