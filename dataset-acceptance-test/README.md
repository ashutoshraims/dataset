### Running E2E Tests

You will need to have the following environment variables defined.

| name                    | value                                            | description                                                        | sensitive? | source | required |
|-------------------------|--------------------------------------------------|--------------------------------------------------------------------|------------|--------|----------|
| `DOMAIN`                | ex `osdu`                                        | Domain name                                                        | no         | -      | yes      |
| `TENANT_NAME`           | ex `osdu`                                        | Shared Tenant name                                                 | no         | -      | yes      |
| `PROVIDER_KEY`          | ex `anthos`                                      | Provider Key                                                       | no         | -      | yes      |
| `KIND_SUBTYPE`          | ex `DatasetTest`                                 | Kind Subtype                                                       | no         | -      | yes      |
| `LEGAL_TAG`             | ex `public-usa-dataset-1`                        | Legal Tag name                                                     | no         | -      | yes      |
| `ENTITLEMENTS_BASE_URL` | ex `http://localhost:8080`                       | Entitlements service Base URL                                      | no         | -      | yes      |
| `STORAGE_BASE_URL`      | ex `http://localhost:8080/api/storage/v2/`       | Storage Service Base URL                                           | no         | -      | yes      |
| `DATASET_BASE_URL`      | ex `http://localhost:8080/api/dataset/v1/`       | Dataset Service Base URL                                           | no         | -      | yes      |
| `SCHEMA_BASE_URL`       | ex `http://localhost:8080/api/schema-service/v1` | Schema Service Base URL                                            | no         | -      | yes      |
| `LEGAL_BASE_URL`        | ex `http://localhost:8080/api/legal/v1/`         | Legal Service Base URL                                             | no         | -      | yes      |
| `EXECUTE_DELETE_TEST`   | ex `true` or `false`                             | Execute exhaustive tests including delete operation, default false | no         | -      | no       |

Authentication can be provided as OIDC config:

| name                                            | value                                      | description                   | sensitive? | source |
|-------------------------------------------------|--------------------------------------------|-------------------------------|------------|--------|
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_ID`     | `********`                                 | Privileged User Client Id     | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_SECRET` | `********`                                 | Privileged User Client secret | yes        | -      |
| `TEST_OPENID_PROVIDER_URL`                      | ex `https://keycloak.com/auth/realms/osdu` | OpenID provider url           | yes        | -      |

Or tokens can be used directly from env variables:

| name                    | value      | description           | sensitive? | source |
|-------------------------|------------|-----------------------|------------|--------|
| `PRIVILEGED_USER_TOKEN` | `********` | Privileged User Token | yes        | -      |


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


**Note**
- The acceptance tests currently do not support deletion of the generated dataset files in Storage, so the multiple runs of this test pipeline would let the files accumulate over time, which might need to be cleaned up.


Execute following command to build code and run all the integration tests:

 ```bash
 # Note: this assumes that the environment variables for integration tests as outlined
 #       above are already exported in your environment.
 # build + install integration test core
 $ (cd dataset-acceptance-test && mvn clean test)
 ```

## License

Copyright © Google LLC

Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
