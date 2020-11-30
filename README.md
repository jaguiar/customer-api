# About
This is a simple project with a resource server providing customer data management, an oauth2 authorization server and a client web application.

# Run
To test the example follow the steps below.

1. Launch the docker-compose in /customer-api : 
`docker-compose up --no-start && docker-compose start`
2. Launch the authorization server in the /authentication project (on port 8081)
`mvn spring-boot:run`
3. Launch the resource server in /customer-api (you can choose webflux or resttemplate) (on port 4600)
`mvn spring-boot:run` or `mvn spring-boot:run -Dspring-boot.run.profiles=jsonlog` if you want the logs in JSON format
4. Launch the client in /customer-api/dummy-oauth-client (on port 8089)
5. Go to castlemock http://localhost:8085/castlemock/ and log in with the user admin/admin. Then import the `project-customers-dummy.xml`

# Test
Launch the client app in a browser : http://localhost:8089
The user is john/123
