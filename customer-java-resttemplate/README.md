# Customer

* Language : **java**
* Frameworks : **Spring boot 2 - RestTemplate**
* Dependency management : **maven**

## Functionality

Customer resource server : (get, put)


## Middlewares

You will need docker and docker-compose to install and run the project. https://docs.docker.com/install/

To launch Redis, ELK, etc.
```
docker-compose up --no-start
docker-compose start
```

## Build
```
mvn clean install
```

With integration tests
```
mvn clean install -P docker
```


## Tests
```
mvn clean test  
```

## Run 
```
mvn spring-boot:run
```

To overload spring-boot active profiles, run with
```
mvn spring-boot:run -Dspring-boot.run.profiles=jsonlog
```
The `jsonlog` spring profile is for tests with ELK stack



