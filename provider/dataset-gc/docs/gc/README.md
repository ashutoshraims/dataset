# Service Configuration for Google Cloud

## Table of Contents <a name="TOC"></a>

* [Environment variables](#Environment-variables)
  * [Common properties for all environments](#Common-properties-for-all-environments)
* [Datastore configuration](#Datastore-configuration)
* [DMS providers](#DMS-providers)
* [Google cloud service account configuration](#Google-cloud-service-account-configuration)
* [Running E2E Tests](#running-e2e-tests)
* [License](#license)

## Environment variables

Define the following environment variables.

Defined in default application property file but possible to override:

| name                              | value                                        | description                         | sensitive? | source        |
|-----------------------------------|----------------------------------------------|-------------------------------------|------------|---------------|
| `MANAGEMENT_ENDPOINTS_WEB_BASE`   | ex `/`                                       | Web base for Actuator               | no         | -             |
| `MANAGEMENT_SERVER_PORT`          | ex `8081`                                    | Port for Actuator                   | no         | -             |

### Common properties for all environments

| name                             | value                                         | description                                                                           | sensitive? | source                                                       |
|----------------------------------|-----------------------------------------------|---------------------------------------------------------------------------------------|------------|--------------------------------------------------------------|
| `LOG_PREFIX`                     | `dataset`                                     | Logging prefix                                                                        | no         | -                                                            |
| `SERVER_SERVLET_CONTEXPATH`      | `/api/storage/v2/`                            | Servlet context path                                                                  | no         | -                                                            |
| `AUTHORIZE_API`                  | ex `https://entitlements.com/entitlements/v1` | Entitlements API endpoint                                                             | no         | output of infrastructure deployment                          |
| `PARTITION_API`                  | ex `http://localhost:8081/api/partition/v1`   | Partition service endpoint                                                            | no         | -                                                            |
| `STORAGE_API`                    | ex `http://storage/api/legal/v1`              | Storage API endpoint                                                                  | no         | output of infrastructure deployment                          |
| `SCHEMA_API`                     | ex `http://schema/api/legal/v1`               | Schema API endpoint                                                                   | no         | output of infrastructure deployment                          |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json`      | Service account credentials, you only need this if running locally                    | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |
| `DMS_API_BASE`                   | ex `http://localhost:8081/api/file/v2/files`  | *Only for local usage.* Allows to override DMS service base url value from Datastore. | no         | -                                                            |
| `PARTITION_AUTH_ENABLED`         | ex `true` or `false`                          | Disable or enable auth token provisioning for requests to Partition service           | no         | -                                                            |

## Datastore configuration

There must be a kind `DmsServiceProperties` in default namespace, with DMS configuration,
Example:

| name                              | apiKey | dmsServiceBaseUrl                                                               | isStagingLocationSupported | isStorageAllowed |
|-----------------------------------|--------|---------------------------------------------------------------------------------|----------------------------|------------------|
| `name=dataset--File.*`            |        | `https://community.gc.gnrg-osdu.projects.epam.com/api/file/v2/files`            | `true`                     | `true`           |
| `name=dataset--FileCollection.*`  |        | `https://community.gc.gnrg-osdu.projects.epam.com/api/file/v2/file-collections` | `true`                     | `true`           |
| `name=dataset--ConnectedSource.*` |        | `https://community.gc.gnrg-osdu.projects.epam.com/api/eds/v1/`                  | `true`                     | `true`           |

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

![Screenshot](../baremetal/pics/dataset.png)

## Google cloud service account configuration

TBD

| Required roles |
| ---    |
| - |

## Running E2E Tests

This section describes how to run cloud OSDU E2E tests (testing/dataset-test-gc).

You will need to have the following environment variables defined.

| name                         | value                                                                | description                                                                                                                                                     | sensitive? | source                                                       |
|------------------------------|----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|--------------------------------------------------------------|
| `GROUP_ID`                   | ex `osdu-gc.go3-nrg.projects.epam.com`                               | -                                                                                                                                                               | no         | -                                                            |
| `STORAGE_BASE_URL`           | ex `https://os-storage-jvmvia5dea-uc.a.run.app/api/storage/v2/`      | Storage API endpoint                                                                                                                                            | no         | output of infrastructure deployment                          |
| `LEGAL_BASE_URL`             | ex `https://os-legal-jvmvia5dea-uc.a.run.app/api/legal/v1/`          | Legal API endpoint                                                                                                                                              | no         | output of infrastructure deployment                          |
| `DATASET_BASE_URL`           | ex `http://localhost:8080/api/dataset/v1/`                           | Dataset API endpoint                                                                                                                                            | no         | output of infrastructure deployment                          |
| `SCHEMA_API`                 | ex `https://os-schema-jvmvia5dea-uc.a.run.app/api/schema-service/v1` | Schema API endpoint                                                                                                                                             | no         | output of infrastructure deployment                          |
| `PROVIDER_KEY`               | `GCP`                                                                | required for response verification                                                                                                                              | no         | -                                                            |
| `INTEGRATION_TESTER`         | `ewogICJ0....` or `tmp/service-acc.json`                             | Service account for API calls, passed as a filename or JSON content, plain or Base64 encoded.  Note: this user must have entitlements configured already        | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |
| `GC_DEPLOY_FILE`             | `ewogICJ0....` or `tmp/service-acc.json`                             | Service account for test data tear down, passed as a filename or JSON content, plain or Base64 encoded. Must have cloud storage role configured                 | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |
| `TENANT_NAME`                | `opendes`                                                            | Tenant name                                                                                                                                                     | no         | -                                                            |
| `KIND_SUBTYPE`               | `DatasetTest`                                                        | Kind subtype that will be used in int tests, schema creation automated , result kind will be `TENANT_NAME::wks-test:dataset--FileCollection.KIND_SUBTYPE:1.0.0` | no         | -                                                            |
| `LEGAL_TAG`                  | `public-usa-dataset-1`                                               | Legal tag name, if tag with that name doesn't exist then it will be created during preparing step                                                               | no         | -                                                            |
| `GCLOUD_PROJECT`             | `osdu-cicd-epam`                                                     | Project id                                                                                                                                                      | no         | -                                                            |
| `GC_STORAGE_PERSISTENT_AREA` | ex `persistent-area`                                                 | persistent area bucket(will be concatenated with project id ex `osdu-cicd-epam-persistent-area`                                                                 | no         | output of infrastructure deployment                          |
| `LEGAL_HOST`                 | ex `https://os-legal-jvmvia5dea-uc.a.run.app/api/legal/v1/`          | Legal API endpoint                                                                                                                                              | no         | output of infrastructure deployment                          |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER |
| ---  |
| users<br/>service.entitlements.user<br/>service.storage.admin<br/>service.legal.user<br/>service.search.user<br/>service.delivery.viewer<br/>service.dataset.viewers<br/>service.dataset.editors |

**Cloud roles configuration for integration accounts**

| GC_DEPLOY_FILE|
| ---  |
| storage.admin access to the Google Cloud Storage |

Execute following command to build code and run all the integration tests:

 ```bash
 # Note: this assumes that the environment variables for integration tests as outlined
 #       above are already exported in your environment.
 # build + install integration test core
 $ (cd testing/dataset-test-core/ && mvn clean install)
 ```

 ```bash
 # build + run Google Cloud integration tests.
 $ (cd testing/dataset-test-gc/ && mvn clean test)
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