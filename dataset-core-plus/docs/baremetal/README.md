# Service Configuration for Baremetal

## Table of Contents <a name="TOC"></a>

* [Environment variables](#Environment-variables)
    * [Common properties for all environments](#Common-properties-for-all-environments)
    * [Properties set in Partition service](#properties-set-in-partition-service)
    * [For OSM Postgres](#For-OSM-Postgres)
* [DMS providers](#dms-providers)
* [Keycloak configuration](#keycloak-configuration)
* [Running E2E Tests](#running-e2e-tests)
* [License](#license)

## Environment variables

Defined in default application property file but possible to override:

| name                              | value                                        | description                         | sensitive? | source        |
|-----------------------------------|----------------------------------------------|-------------------------------------|------------|---------------|
| `MANAGEMENT_ENDPOINTS_WEB_BASE`   | ex `/`                                       | Web base for Actuator               | no         | -             |
| `MANAGEMENT_SERVER_PORT`          | ex `8081`                                    | Port for Actuator                   | no         | -             |


### Common properties for all environments

| name                        | value                                         | description                                                                           | sensitive? | source                              |
|-----------------------------|-----------------------------------------------|---------------------------------------------------------------------------------------|------------|-------------------------------------|
| `LOG_PREFIX`                | `dataset`                                     | Logging prefix                                                                        | no         | -                                   |
| `SERVER_SERVLET_CONTEXPATH` | `/api/storage/v2/`                            | Servlet context path                                                                  | no         | -                                   |
| `AUTHORIZE_API`             | ex `https://entitlements.com/entitlements/v1` | Entitlements API endpoint                                                             | no         | output of infrastructure deployment |
| `PARTITION_API`             | ex `http://localhost:8081/api/partition/v1`   | Partition service endpoint                                                            | no         | -                                   |
| `STORAGE_API`               | ex `http://storage/api/legal/v1`              | Storage API endpoint                                                                  | no         | output of infrastructure deployment |
| `SCHEMA_API`                | ex `http://schema/api/legal/v1`               | Schema API endpoint                                                                   | no         | output of infrastructure deployment |
| `DMS_API_BASE`              | ex `http://localhost:8081/api/file/v2/files`  | *Only for local usage.* Allows to override DMS service base url value from Datastore. | no         | -                                   |

These variables define service behavior.

| name                     | value                | description                                                                 | sensitive? | source |
|--------------------------|----------------------|-----------------------------------------------------------------------------|------------|--------|
| `PARTITION_AUTH_ENABLED` | ex `true` or `false` | Disable or enable auth token provisioning for requests to Partition service | no         | -      |

### Properties set in Partition service:

Note that properties can be set in Partition as `sensitive` in that case in property `value` should
be present **not
value itself**, but **ENV variable name**. This variable should be present in environment of service
that need that
variable.

Example:

```
    "elasticsearch.port": {
      "sensitive": false, <- value not sensitive 
      "value": "9243"  <- will be used as is.
    },
      "elasticsearch.password": {
      "sensitive": true, <- value is sensitive 
      "value": "ELASTIC_SEARCH_PASSWORD_OSDU" <- service consumer should have env variable ELASTIC_SEARCH_PASSWORD_OSDU with elastic search password
    }
```

### For OSM Postgres

As a quick shortcut, this example snippet can be used by DevOps DBA:

```

CREATE TABLE public."DmsServiceProperties"(
id text COLLATE pg_catalog."default" NOT NULL,
pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
data jsonb NOT NULL,
CONSTRAINT DmsServiceProperties_id UNIQUE (id)
);
CREATE INDEX DmsServiceProperties_datagin ON public."DmsServiceProperties" USING GIN (data);

```

There must be a table `DmsServiceProperties` in default schema, with DMS configuration, Example:

| name                              | apiKey | dmsServiceBaseUrl                                            | isStagingLocationSupported | isStorageAllowed |
|-----------------------------------|--------|--------------------------------------------------------------|----------------------------|------------------|
| `name=dataset--File.*`            |        | `https://osdu-anthos.osdu.club/api/file/v2/files`            | `true`                     | `true`           |
| `name=dataset--FileCollection.*`  |        | `https://osdu-anthos.osdu.club/api/file/v2/file-collections` | `true`                     | `true`           |
| `name=dataset--ConnectedSource.*` |        | `https://osdu-anthos.osdu.club/api/eds/v1/`                  | `true`                     | `true`           |

You can use the `INSERT` script below to bootstrap the data with valid records:

```roomsql
INSERT INTO public."DmsServiceProperties"(id, data)
	VALUES 
	('dataset--File.*', 
	'{
	  "apiKey": "",
	  "datasetKind": "dataset--File.*",
	  "isStorageAllowed": true,
	  "dmsServiceBaseUrl": "https://osdu-anthos.osdu.club/api/file/v2/files",
	  "isStagingLocationSupported": true
	}'),
	
	('dataset--File.*', 
	'{
	  "apiKey": "",
	  "datasetKind": "ConnectedSource.*",
	  "isStorageAllowed": true,
	  "dmsServiceBaseUrl": "https://osdu-anthos.osdu.club/api/eds/v1/",
	  "isStagingLocationSupported": true
	}'),
	
	('dataset--FileCollection.*', 
	'{
	  "apiKey": "",
	  "datasetKind": "dataset--FileCollection.*",
	  "isStorageAllowed": true,
	  "dmsServiceBaseUrl": "https://osdu-anthos.osdu.club/api/file/v2/file-collections",
	  "isStagingLocationSupported": true
	}');
```

**prefix:** `osm.postgres`
It can be overridden by:

- through the Spring Boot property `osm.postgres.partition-properties-prefix`
- environment variable `OSM_POSTGRES_PARTITION_PROPERTIES_PREFIX`

**Propertyset:**

| Property                         | Description |
|----------------------------------|-------------|
| osm.postgres.datasource.url      | server URL  |
| osm.postgres.datasource.username | username    |
| osm.postgres.datasource.password | password    |

<details><summary>Example of a definition for a single tenant</summary>

```

curl -L -X PATCH 'https://api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "osm.postgres.datasource.url": {
      "sensitive": false,
      "value": "jdbc:postgresql://127.0.0.1:5432/postgres"
    },
    "osm.postgres.datasource.username": {
      "sensitive": false,
      "value": "postgres"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
      "value": "<POSTGRES_PASSWORD_ENV_VARIABLE_NAME>" <- (Not actual value, just name of env variable)
    }
  }
}'

```

</details>

## DMS providers

- File service is responsible for handling FILE and FILE_COLLECTION dataset types
- EDS service is responsible for redirecting handling CONNECTED_SOURCE dataset type
  to external DMS providers

### EDS retrieval instructions handling flow

Prerequisites on "external" OSDU environment:

1. Legal service | Create legaltag
2. Dataset service | getStorageInstructions
3. Signed url | Upload file
4. Dataset service | registerDataset
5. Dataset service | getRetrievalInstructions
6. Signed url | Download file to check file was uploaded

Summary: test file was uploaded on "external" OSDU environment
and dataset was registered with `external-dataset-registry-id`

Main flow on "local" OSDU environment:

7. Legal service | Create legaltag
8. Secret service | Create Scopes secrets
9. Secret service | Create Client secrets
10. Storage service | Create ConnectedSourceRegistryEntry (contains SecuritySchemes)
11. Storage service | Create ConnectedSourceDataJob (contains Registry ID, external url)
12. Storage service | Create ConnectedSource.Generic (contains DatasetProperties mapping)
13. Dataset service | getRetrievalInstructions
14. Signed url | Download file on "local" OSDU environment

Summary: test file created on "external" OSDU environment was downloaded on "local" OSDU environment
using EDS service

![Screenshot](./pics/dataset.png)

## Keycloak configuration

[Keycloak service accounts setup](https://www.keycloak.org/docs/latest/server_admin/#_service_accounts)

Configure Clients. One Client per OSDU service. Set them “confidential”.

![Screenshot](./pics/client.png)

Each Client has embedded Service Account (SA) option. Enable SAs for Clients, make “Authorization
enabled”:

![Screenshot](./pics/sa.png)

Add `partition-and-entitlements` scope to `Default Client Scopes` and generate Keys.

Give `client-id` and `client-secret` to services, which should be authorized within the platform.

## Running E2E Tests

This section describes how to run cloud OSDU E2E tests (testing/dataset-test-baremetal).

You will need to have the following environment variables defined.

| name                                           | value                                                                | description                                                                                                                                                     | sensitive?                                        | source                              |
|------------------------------------------------|----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|-------------------------------------|
| `GROUP_ID`                                     | ex `osdu-gc.go3-nrg.projects.epam.com`                               | -                                                                                                                                                               | no                                                | -                                   |
| `STORAGE_BASE_URL`                             | ex `https://os-storage-jvmvia5dea-uc.a.run.app/api/storage/v2/`      | Storage API endpoint                                                                                                                                            | no                                                | output of infrastructure deployment |
| `LEGAL_BASE_URL`                               | ex `https://os-legal-jvmvia5dea-uc.a.run.app/api/legal/v1/`          | Legal API endpoint                                                                                                                                              | no                                                | output of infrastructure deployment |
| `LEGAL_HOST`                                   | ex `https://os-legal-jvmvia5dea-uc.a.run.app/api/legal/v1/`          | Legal API endpoint                                                                                                                                              | no                                                | output of infrastructure deployment |
| `DATASET_BASE_URL`                             | ex `http://localhost:8080/api/dataset/v1/`                           | Dataset API endpoint                                                                                                                                            | no                                                | output of infrastructure deployment |
| `SCHEMA_API`                                   | ex `https://os-schema-jvmvia5dea-uc.a.run.app/api/schema-service/v1` | Schema API endpoint                                                                                                                                             | no                                                | output of infrastructure deployment |
| `PROVIDER_KEY`                                 | `ANTHOS`                                                             | required for response verification                                                                                                                              | no                                                | -                                   |
| `TENANT_NAME`                                  | `opendes`                                                            | Tenant name                                                                                                                                                     | no                                                | -                                   |
| `KIND_SUBTYPE`                                 | `DatasetTest`                                                        | Kind subtype that will be used in int tests, schema creation automated , result kind will be `TENANT_NAME::wks-test:dataset--FileCollection.KIND_SUBTYPE:1.0.0` | no                                                | -                                   |
| `LEGAL_TAG`                                    | `public-usa-dataset-1`                                               | Legal tag name, if tag with that name doesn't exist then it will be created during preparing step                                                               | no                                                | -                                   |
| `BAREMETAL_STORAGE_PERSISTENT_AREA`            | ex `osdu-anthos-osdu-persistent-area`                                | persistent area bucket                                                                                                                                          | no                                                | output of infrastructure deployment |
| `TEST_OPENID_PROVIDER_CLIENT_ID`               | `********`                                                           | Client Id for `$INTEGRATION_TESTER`                                                                                                                             | yes                                               | --                                  |
| `TEST_OPENID_PROVIDER_CLIENT_SECRET`           | `********`                                                           |                                                                                                                                                                 | Client secret for `$INTEGRATION_TESTER`           | --                                  |
| `TEST_NO_ACCESS_OPENID_PROVIDER_CLIENT_ID`     | `********`                                                           | Client Id for `$NO_ACCESS_INTEGRATION_TESTER`                                                                                                                   | yes                                               | --                                  |
| `TEST_NO_ACCESS_OPENID_PROVIDER_CLIENT_SECRET` | `********`                                                           |                                                                                                                                                                 | Client secret for `$NO_ACCESS_INTEGRATION_TESTER` | --                                  |
| `TEST_OPENID_PROVIDER_URL`                     | `https://keycloak.com/auth/realms/osdu`                              | OpenID provider url                                                                                                                                             | yes                                               | --                                  |
| `TEST_MINIO_SECRET_KEY`                        | `********`                                                           | MinIO secret key                                                                                                                                                | yes                                               | --                                  |
| `TEST_MINIO_ACCESS_KEY`                        | `********`                                                           | MinIO access key                                                                                                                                                | yes                                               | --                                  |
| `TEST_MINIO_URL`                               | `https://minio.com`                                                  | Endpoint of MinIO used by File service                                                                                                                          | no                                                | --                                  |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER        |
|---------------------------| 
| users                     | 
| service.entitlements.user |
| service.storage.admin     |
| service.legal.user        |
| service.search.user       |
| service.delivery.viewer   |
| service.dataset.viewers   |
| service.dataset.editors   |

**Access configuration for minio test accounts**

| TEST_MINIO_ACCESS_KEY                                                |
|----------------------------------------------------------------------|
| access to the staging and persistent bucket used by the File service |

Execute following command to build code and run all the integration tests:

 ```bash
 # Note: this assumes that the environment variables for integration tests as outlined
 #       above are already exported in your environment.
 # build + install integration test core
 $ (cd testing/dataset-test-core/ && mvn clean install)
 ```

 ```bash
 # build + run Google Cloud integration tests.
 $ (cd testing/dataset-test-baremetal/ && mvn clean test)
 ```

## License

Copyright 2020-2023 Google LLC

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