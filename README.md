# Card Application

## Prerequisites

- Java 17
- Apache Maven 3.9.6
- Microsoft SQL Server running on the local machine

## Create the local database if not exist

```sql
IF DB_ID(N'TESTDB') IS NULL
BEGIN
	CREATE DATABASE TESTDB;
END;
GO
```
The application expects SQL Server to listen on `localhost:1433`.

## Configure the application

The default database settings are:

- URL: `jdbc:sqlserver://localhost:1433;databaseName=TESTDB;encrypt=true;trustServerCertificate=true`
- Username: `sa`
- Password: your local SQL Server `sa` password

Override them with environment variables when needed:

Option 1 : Run script
```bash
export DB_URL='jdbc:sqlserver://localhost:1433;databaseName=TESTDB;encrypt=true;trustServerCertificate=true'
export DB_USERNAME='sa'
export DB_PASSWORD='your-password'
```

Option 2 : Update in application.yml
```text
spring:
  datasource:
    url: ${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=TESTDB;encrypt=true;trustServerCertificate=true}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:}
```

## Create table in database

Trigger the db script in path below before start server.
```text
/db_script/create_table.sql
```

## Dummy data (Optional)

If you need dummy data may trigger the db script in path below.

```text
/db_script/dummydata.sql
```

## Build

From the repository root:

```bash
mvn clean package
```

Skip tests during the build:

```bash
mvn clean package -DskipTests
```

## Start the server

Run the packaged application:

```bash
java -jar app-card/target/app-card-1.0.0.jar
```

The API starts on:

```text
http://localhost:8080
```

Stop the server with `Ctrl+C`.

## Logs

Get logs in 
```text
/logs/card.log
```

## Postman Collection

Get postman collection with response example in 
```text
/postman/Card Application API.postman_collection.json
```

## Valid Payment Status

```text
PENDING
AUTHORIZED
DECLINED
CAPTURED
REFUNDED
```

## Valid Currency

```text
AUD
BRL
CAD
CHF
CNY
CZK
DKK
GBP
HKD
HUF
IDR
ILS
INR
ISK
JPY
KRW
MXN
MYR
NOK
NZD
PHP
PLN
RON
SEK
SGD
THB
TRY
USD
ZAR
EUR
```
