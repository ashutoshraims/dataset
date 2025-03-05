# Dataset registry service

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites


* JDK 17
* Lombok 1.18.26 or later
* Maven 3.8.0+

# Features of implementation
This is a universal solution created using EPAM OSM and OBM mappers technology.
It allows you to work with various implementations of KV stores and Blob stores.

## Limitations of the current version

In the current version, the mappers are equipped with several drivers to the stores:

- OSM (mapper for KV-data): Datastore; Postgres
- OBM (mapper to Blob stores):  Cloud Storage (GCS); MinIO

## Extensibility

To use any other store or message broker, implement a driver for it. With an extensible set of drivers, the solution is unrestrictedly universal and portable without modification to the main code.

Mappers support "multitenancy" with flexibility in how it is implemented.
They switch between datasources of different tenants due to the work of a bunch of classes that implement the following interfaces:

- Destination - takes a description of the current context, e.g., "data-partition-id = opendes"
- DestinationResolver – accepts Destination, finds the resource, connects, and returns Resolution
- DestinationResolution – contains a ready-made connection, the mapper uses it to get to data

## Mapper tuning mechanisms

#### Their algorithms are as follows:
- incoming Destination carries data-partition-id
- resolver accesses the Partition service and gets PartitionInfo
- from PartitionInfo resolver retrieves properties for the connection: URL, username, password etc.
- resolver creates a data source, connects to the resource, remembers the datasource
- resolver gives the datasource to the mapper in the Resolution object


# Configuration

## Service Configuration

### Baremetal Service Configuration:
[Baremetal service configuration ](docs/baremetal/README.md)

### Run Locally
Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.8.0+
Maven home: /usr/share/maven
Java version: 17, vendor: AdoptOpenJDK
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```

* Navigate to Dataset service root folder and run:

```bash
mvn clean install   
```

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/dataset-gc && mvn spring-boot:run
```
## Testing

 ### Running E2E Tests
 This section describes how to run cloud OSDU E2E tests.
 
 ### Baremetal test configuration:
 [Baremetal service configuration ](docs/baremetal/README.md)


## License



Copyright 2020-2023 EPAM Systems, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
