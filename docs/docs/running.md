# Running Dataset Service 

## Running Dataset Service Locally

The Dataset Service is a Maven multi-module project with each cloud implemention placed in its submodule.

!!! warning "Possibly outdated"

    Running these services locally are quite difficult and this information may be outdated. [AdminCLI](https://osdu.pages.opengroup.org/ui/admincli/) has some functionality to make this a little [easier](https://osdu.pages.opengroup.org/ui/admincli/aws/#partial-steps-to-setup-a-hybrid-development-environment).

#***REMOVED*** Implementation

1. Navigate to the module of the cloud of interest, for example, ```dataset-aws```. Configure ```application.properties``` and optionally ```logback-spring.xml```. Intead of changing these files in the source, you can also provide external files at run time.

2. Navigate to the root of the dataset registry project, build and run unit tests in command line:

```bash
    mvn clean package
```

3. Install Redis

    You can follow the [tutorial to install Redis locally](https://koukia.ca/installing-redis-on-windows-using-docker-containers-7737d2ebc25e) or install [docker for windows](https://docs.docker.com/docker-for-windows/install/). Make sure you are running docker with linux containers as Redis does not have a Windows image.

### Pull redis image on docker
```bash
    docker pull redis
```
### Run redis on docker
```bash
    docker run --name some-redis -d redis
```

    Install windows Redis client by following the instructions [here](https://github.com/MicrosoftArchive/redis/releases). Use default port 6379.

4. Set environment variables:

**AWS**: AWS service account credentials are read from the environment variables in order to
authenticate AWS requests. The following variables can be set as either system environment
variables or user environment variables. User values will take precedence if both are set.

1. `AWS_ACCESS_KEY_ID=<YOURAWSACCESSKEYID>`
2. `AWS_SECRET_KEY=<YOURAWSSECRETKEY>`

Note that these values can be found in the IAM stack's export values in the AWS console. To
deploy resources to the AWS console, see the deployment section below.

1. Run dataset service in command line:

```bash
    # Running AWS:
    java -jar provider\dataset-aws\target\dataset-aws-0.0.1-SNAPSHOT-spring-boot.jar
```

2. Access the service:

    The port and path for the service endpoint can be configured in ```application.properties``` in the provider folder as following. If not specified, then  the web container (ex. Tomcat) default is used:

```bash
    server.servlet.contextPath=/api/dataset/v1/
    server.port=8080
```

3. Build and test in IntelliJ:
    1. Import the maven project from the root of this project.
    2. Create a ```JAR Application``` in ```Run/Debug Configurations``` with the ```Path to JAR``` set to the target jar file.
    3. To run unit tests, creat a ```JUnit``` configuration in ```Run/Debug Configurations```, specify, for example:

```text
    Test kind: All in a package
    Search for tests: In whole project
```

### Additional Information

Additional documentation for the AWS implementation of `os-dataset` can be found [here](https://community.opengroup.org/osdu/platform/system/dataset/-/blob/master/provider/dataset-aws/README.md) and [here](https://community.opengroup.org/osdu/platform/system/dataset/-/blob/master/devops/aws/chart/README.md)

#### Azure

All documentation for the Azure implementation of `os-dataset` can be found [here](https://community.opengroup.org/osdu/platform/system/dataset/-/blob/master/provider/dataset-azure/README.md)

###***REMOVED***

All documentation for the Google Cloud implementation of `os-dataset` can be found [here](https://community.opengroup.org/osdu/platform/system/dataset/-/blob/master/provider/dataset-gc/README.md)

## Running integration tests

Integration tests are located in a separate project for each cloud in the ```testing``` directory under the project root directory.

##***REMOVED***

Instructions for running the AWS integration tests can be found [here](https://community.opengroup.org/osdu/platform/system/dataset/-/blob/master/provider/dataset-aws/README.md)

##***REMOVED***

All documentation for the Google Cloud implementation of `os-dataset` can be found [here](https://community.opengroup.org/osdu/platform/system/dataset/-/blob/master/provider/dataset-gc/README.md)