# Hexagonal SpringBoot Financial Application

Maven + Java 17 Hexagonal Spring-Boot application split in modules.

### application module

```
Java 17 
Spring WebFlux Reactive/Non-Blocking REST
```

### domain module

```
Java 17 
Ports and Interfaces that make use of Adapters
```

### launcher module

```
Java 17
Spring-Boot Bootstrap and Bean injection of domain impls
```

### persistence module

```
Java 17
Spring-Boot Data R2DB Reactive/Non-Blocking SQL
Liquibase Database SQL Migration/Versioning
Adapter implementation of persistence Port
Use Transactional and Optimist Lock to guarantee account balance and movement data
```

## Database

H2 database, will create a folder "h2db" when the application starts in the root folder of where the application is
running.

## Running Application

How to run the application.

#### Dependencies:

```
Docker
```

#### Running
One command run everything:
```
docker-compose up --build
```

**Dockerfile** use a multi-stage docker build process to build the docker with the spring-boot application jar, so you
don't need Java Environment on your machine.

The **docker-compose.yml** holds commands to automatic build the container of the app and create a shared volume in a
local folder "h2db" where will be persisted the H2DB data.

## Tests

Every module have his own tests. Assuming every responsibility was covered, i.e:

**application tests** validates the input/web behaviours like the right call of the domain service implementations with
right objects
and the error handling processed by the exception handlers to check if the right json/status-codes was right.

**persistence tests** unit tests validates the right function and behaviours of the persistence ports implementations.

**launcher tests** Integration tests that validates the behaviour of the application using h2:mem database.
Uses a _test execution order dependency_ anti-pattern so the order of tests and each test depends his antecessor to run.
Validate parallel transfers to secure the lock and transfers occurs correctly making the balance of a account be equal
to the
total of respective credits and debits in the extract.

#### Dependencies:

```
Java Environment
```

#### Running tests:

```
./mvnw clean install
```

## Documentation / Using the API

#### Swagger

You can try it out using swagger-ui:

```
localhost:8080/swagger-ui.html
```

### Criar uma conta corrente:

POST /api/v1/accounts

```JSON
{
  "name": "Account User Name",
  "limit": 100
}
```

### Realizar um depósito:

POST /api/v1/movements/{accountId}/debit

```JSON
{
  "amount": 12.0
}
```

### Realizar um saque (verificar se possui saldo ou limite)

POST /api/v1/movements/{accountId}/withdraw

```JSON
{
  "amount": 103.00
}
```

### Realizar uma transferência entre contas

POST api/v1/movements/{accountId}/transfer

```JSON
{
  "creditAccountId": "431acb9c-5940-4332-8d6b-92482fe03c4c",
  "amount": 1.0
}
```

### Consultar o saldo da conta

GET /api/v1/accounts/{accountId}/balance

### Consultar o extrato da conta por período

GET /api/v1/movements/{accountId}?start=2000-01-01&end=3000-01-01&page=0&pageSize=25

* The most common ISO Date Format {@code yyyy-MM-dd} &mdash; for example, "2000-10-31".

### Consultar o extrato da conta por tipo

**Tipo debito:**

GET /api/v1/movements/{accountId}/debit

**Tipo credito:**

GET /api/v1/movements/{accountId}/credito
