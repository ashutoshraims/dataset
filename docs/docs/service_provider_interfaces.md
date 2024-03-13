# Service Provider Interfaces

The Dataset service has a few Service Provider Interfaces that can be implemented.

| Interface              | Required/Optional       | Path                                                                        |
| ---------------------- | ----------------------- | ------------------------------------------------------------------------    |
| DatasetDmsService      | Optional to implement   | `dataset-core/src/main/java/.../service/DatasetDmsService`                  |
| DatasetRegistryService | Optional to implement   | `dataset-core/src/main/java/.../provider/interfaces/DatasetRegistryService` |
| IDatasetDmsServiceMap  | Required to implement   | `dataset-core/src/main/java/.../provider/interfaces/IDatasetDmsServiceMap`  |

