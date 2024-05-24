# Employee Application

This application allows to create, update and delete employees. It also propagates them to kafka

## Build and run it: 
For starting postgres and kafka locally run
`docker-compose up -d`

To run the application run 
`mvn spring-boot:run`

Try the API over swagger by opening:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Authentication
Currently a basic Authentication with username "user" and password "password" is used.
The Only endpoints that need to be authenticated are: 
put, post and delete for /employee
get-Requests don't need authentication

## Testing 
Run Test with 
`mvn test`

This requires a running Database and Kafka, not only for the E2E-Tests, 
also for the integration tests!
Not every line of code is tested to full extend.
Testing focuses on the most relevant tests without testing libraries and basic configurations

## Further notes about the Implementation
- No changes to employee can be done without successfully sending the kafka event. (Done with Transaction-Annotation)
- Discussable: Where to put the DTO-Mapping -> I decided for mapping in the service
- The E2E test for testing the happy-path of the Kafka events is not stable with the local Kafka. Using an embedded Kafka for tests instead could solve this problem. 