# TNT API Service Aggregation for Fedex 

The assignmnt is made using Spring Boot.

### How to run the application:

- Make sure you can access the API by one of the following two ways:

    - run a local container using docker with that command: 
    *docker run -d -p 8080:8080 --name TNT-services xyzassessment/backend-services*
    
    - change the *tnt.aggregation.api-url* key in *application.properties* to reach the desired host
- Run the application:
    - This require the jar to be first built using one of the following way:
        - Using maven: *mvn clean install* from the project directory
        - From the IDE
    - Directly run the jar: *java -jar <PROJECT_PATH>/target/tntaggregation-0.0.1-SNAPSHOT*.jar.     - Using maven: 
        - For windows: *./mvnw spring-boot:run*
        - For Mac/Linux: mvn spring-boot:run
    - Or from an IDE
- The application is reachable at the URL:  *http://localhost:8081/aggregation*
- Additionally, you can adjust the log level if desired from the *application.properties* by modifiyng the key *logging.level.root* to DEBUG or other

