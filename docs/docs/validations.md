# Validations

The Dataset service's current implementation performs a general check of the validity of the
authorization token and data partition ID before the service starts the core function of each service.

However, the Dataset service doesn't perform any verification whether a
dataset upload/download happened or whether the user registered a dataset after upload.

#### Server Url(full path vs relative path) configuration
- `api.server.fullUrl.enabled=true` It will generate full server url in the OpenAPI swagger
- `api.server.fullUrl.enabled=false` It will generate only the contextPath only
- default value is false (Currently only in Azure it is enabled)
[Reference]:(https://springdoc.org/faq.html#_how_is_server_url_generated) 

