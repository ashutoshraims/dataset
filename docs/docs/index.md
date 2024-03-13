# OSDU Dataset Service

## Introduction

The OSDU Dataset service provides internal and external API endpoints to allow and application or user fetch storage/retrieval instructions for various types of datasets.  (ex. File Datasets)

The Dataset service requires that various DMS services are registered per dataset schema type.  For example the schema subType '`dataset--File.*`' can be mapped to the FileDMS service's endpoint

## System interactions

The File service defines the following workflows:

* Dataset Storage Instructions
* Dataset Retrieval Instructions
* Dataset Registry Registration
* Dataset Registry Retrieval

### Dataset Storage Instructions

The dataset storage instructions workflow is defined for the `/v1/getStorageInstructions` API endpoint.  The following diagram illustrates the workflow.

![OSDU Dataset Service getStorageInstructions](/img/getStorageInstructions.png)

### Dataset Retrieval Instructions

The dataset retrieval instructions workflow is defined for the `/v1/getRetrievalInstructions` API endpoint.  The following diagram illustrates the workflow.

![OSDU Dataset Service getRetrievalInstructions](/img/getRetrievalInstructions.png)

### Dataset Registry Registration

The dataset registry registration workflow is defined for the `/v1/registerDataset` API endpoint. The following diagram illustrates the workflow.

![OSDU Dataset Service registerDatasetRegistry](/img/registerDatasetRegistry.png)

### Dataset Registry Retrieval

The dataset registry retrieval workflow is defined for the `/v1/getDatasetRegistry` API endpoints (GET/POST). The following diagram illustrates the workflow.

![OSDU Dataset Service getDatasetRegistry](/img/getDatasetRegistry.png)