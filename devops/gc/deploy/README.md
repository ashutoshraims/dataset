<!--- Deploy -->

# GC Dataset chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.23.12) with **Istio** (1.15)
> It is possible to use other versions, but it hasn't been tested

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

* **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)
* **Kubectl** (version: v1.23.12 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Configmap variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging level | string | "ERROR" | yes
**data.springProfilesActive** | Spring profile that activate default configuration for Google Cloud environment | string | "gcp" | yes
**data.redisDatasetHost** | The host for an external redis instance. If empty (by default), helm installs an internal redis instance | string | - | yes
**data.redisDatasetPort** | The port for an external redis instance | digit | 6379 | yes

### Deployment variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|---------|
**data.requestsCpu** | amount of requested CPU | string | "20m" | yes
**data.requestsMemory** | amount of requested memory| string | "400Mi" | yes
**data.limitsCpu** | CPU limit | string | "1" | yes
**data.limitsMemory** | memory limit | string | "1G" | yes
**data.serviceAccountName** | name of your service account | string | - | yes
**data.imagePullPolicy** | when to pull image | string | "IfNotPresent" | yes
**data.image** | service image | string | - | yes
**data.redisImage** | image for internal redis | string | `redis:7` | yes

### Config variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|---------|
**conf.configmap** | configmap to be used | string | "dataset-config" | yes
**conf.appName** | name of the app | string | "dataset" | yes
**conf.postgresSecretName** | secret for Postgres | string | "dataset-postgres-secret" | yes
**conf.datasetRedisSecretName** | secret for redis that contains redis password with REDIS_PASSWORD key | string | `dataset-redis-secret` | yes
**conf.onPremEnabled** | whether on-prem is enabled | boolean | false | yes
**conf.domain** | your domain | string | - | yes

### ISTIO variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | 10m | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | 200m | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | 100Mi | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | 256Mi | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install dataset-deploy  .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall dataset-deploy
```

[Move-to-Top](#introduction)
