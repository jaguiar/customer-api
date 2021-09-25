# About
This is a simple project with a resource server providing customer data management, an oauth2 authorization server and a client web application.


## The different versions of the customer-api (customer data management) resource server

* customer-java-resttemplate : Resource server for customer data - Java RestTemplate version
* customer-java-webflux : Resource server for customer data - Java webflux version
* customer-kotlin-webflux : Resource server for customer data - kotlin Webflux version
* customer-kotlin-coroutines : Resource server for customer data - kotlin webflux with **coroutines functional style** version
* customer-kotlin-coroutines-controllers : Resource server for customer data - kotlin webflux with **coroutines annotation style** version

# Prerequisite

You will need to install the following languages/tools to run the demo project

* Java JDK 11+
* Kotlin 1.4+
* Maven 3.6+
* Docker 18+
* Nodejs 16+

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

1. Launch the docker-compose in /customer-api 
   On Linux
    ```sh
    docker-compose up --no-start && docker-compose start
    ```
   On MacOS
   ```sh
   docker-compose -f docker-compose-mac.yml up --no-start && docker-compose -f docker-compose-mac.yml start
   ```

2. Launch the authorization server in the /authentication project (on port 8081)
    ```sh
    mvn spring-boot:run
    ```
3. Launch the resource server in /customer-api (you can choose coroutines, webflux, resttemplate). All versions run on port 4600
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

# Troubleshooting
## Kibana
If Kibana does not work, check the logs for the elasticsearch container
`docker logs elasticsearch` 
If you have this kind of exceptions:
```
"stacktrace": ["org.elasticsearch.bootstrap.StartupException: ElasticsearchException[failed to bind service]; nested: AccessDeniedException[/usr/share/elasticsearch/data/nodes];",
"at org.elasticsearch.bootstrap.Elasticsearch.init(Elasticsearch.java:163) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Elasticsearch.execute(Elasticsearch.java:150) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.cli.EnvironmentAwareCommand.execute(EnvironmentAwareCommand.java:75) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:116) ~[elasticsearch-cli-7.14.1.jar:7.14.1]",
"at org.elasticsearch.cli.Command.main(Command.java:79) ~[elasticsearch-cli-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:115) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:81) ~[elasticsearch-7.14.1.jar:7.14.1]",
"Caused by: org.elasticsearch.ElasticsearchException: failed to bind service",
"at org.elasticsearch.node.Node.<init>(Node.java:798) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.node.Node.<init>(Node.java:281) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Bootstrap$5.<init>(Bootstrap.java:219) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Bootstrap.setup(Bootstrap.java:219) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Bootstrap.init(Bootstrap.java:399) ~[elasticsearch-7.14.1.jar:7.14.1]",
"at org.elasticsearch.bootstrap.Elasticsearch.init(Elasticsearch.java:159) ~[elasticsearch-7.14.1.jar:7.14.1]"
```
just go to the project root folder:
`sudo chown -R 1000:1000 ./elasticsearch-data`
