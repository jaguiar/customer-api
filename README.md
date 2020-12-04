# About
This is a simple project with a resource server providing customer data management, an oauth2 authorization server and a client web application.

# Build
Go to the respective folders of the projects and run
```sh
mvn clean install
```
or if you want to run integration tests with testcontainers
```sh
mvn clean install -P docker
``` 

# Run
To test the example follow the steps below.

1. Launch the docker-compose in /customer-api : 
    ```sh
    docker-compose up --no-start && docker-compose start
    ```
2. Launch the authorization server in the /authentication project (on port 8081)
    ```sh
    mvn spring-boot:run
    ```
3. Launch the resource server in /customer-api (you can choose coroutines, webflux, resttemplate. All versions run on port 4600
    ```sh
    mvn spring-boot:run
    ```
    or if you want the logs in JSON format
    ```sh
    mvn spring-boot:run -Dspring-boot.run.profiles=jsonlog
    ```

4. Launch the client in /customer-api/dummy-oauth-client (on port 8089)
    ```sh
    cd customer-api/dummy-oauth-client/src/main/resources
    npm start
    ```
5. Optional step. If for some reason it does not work, go to castlemock http://localhost:8085/castlemock/ and log in with the user admin/admin.
    Then import the `project-customers-dummy.xml`

# Test
Open your browser and go to http://localhost:8089

The user is `john/123`
