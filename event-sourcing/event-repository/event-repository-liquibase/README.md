# Instructions for creating the database

1. Install Postgres 9.4 or later
2. Create a user called eventstore
3. Create a database called eventstore
4. Run with the following command:
        mvn resources:resources liquibase:update -Dliquibase.url=jdbc:postgresql://localhost:5432/assignmenteventstore -Dliquibase.username=assignment -Dliquibase.password=assignment -Dliquibase.logLevel=info
    Or
        java -jar event-repository-jpa-liquibase-0.1.0-SNAPSHOT.jar --url=jdbc:postgresql://localhost:5432/assignmenteventstore --defaultSchemaName=<schema> --username=assignment --password=assignment --logLevel=info update

All tables can be dropped by running:
        mvn resources:resources liquibase:dropAll -Dliquibase.url=jdbc:postgresql://localhost:5432/assignmenteventstore -Dliquibase.username=assignment -Dliquibase.password=assignment -Dliquibase.logLevel=info
    Or
        java -jar event-repository-jpa-liquibase-0.1.0-SNAPSHOT.jar --url=jdbc:postgresql://localhost:5432/assignmenteventstore --username=assignment --password=assignment --logLevel=info dropAll
   