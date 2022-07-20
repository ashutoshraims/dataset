# Helm Chart

## Introduction
The following document outlines how to deploy and update the service application onto an existing Kubernetes deployment using the [Helm](https://helm.sh) package manager.

## Prerequisites
The below software must be installed before continuing:
* [AWS CLI ^2.7.0](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
* [kubectl 1.21-1.22](https://kubernetes.io/docs/tasks/tools/)
* [Helm ^3.7.1](https://helm.sh/docs/intro/install/)

Additionally, an OSDU on AWS environment must be deployed.

## Installation/Updating
To install or update the service application by executing the following command in the devops/aws/chart folder:

```bash
helm upgrade [RELEASE_NAME] .
```

To observe the Kubernetes resources before deploying them using the command:
```bash
helm upgrade [RELEASE_NAME] . --dry-run --debug
```

To observe the history of the current release, use the following command:
```bash
helm history [RELEASE]
```

To revert to a previous release, use the following command:
```bash
helm rollback [RELEASE] [REVISION]
```

### Customizing the Deployment
It is possible to modify the default values specified in the **values.yaml** file using the --set option. The below variables can be modified by advanced users to customize the deployment configuration:

| Name | Example Value | Description | Type |
| ---  | ------------- | ----------- | ---- |
| `replicaCount` | `1` | The number of pod replicas to be deployed | int |
| `autoscaling.enabled` | `true` | Enables the pod autoscaler | Bool |
| `autoscaling.minReplicas` | `1` | Minimum number of pod replicas | int |
| `autoscaling.maxReplicas` | `100` | Maximum number of pod replicas | int |
| `autoscaling.targetCPUUtilizationPercentage` | `80` | CPU utilization target | int |

## Uninstalling the Chart
To uninstall the helm release:

```bash
helm uninstall [RELEASE] --keep-history
```
