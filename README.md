# individuals-employments-api

This API provides individuals' employment information (PAYE only) from HM Revenue and Customs (HMRC). Employment data is only available for tax years commencing 2013-2014.

### Documentation
The documentation on [confluence](https://confluence.tools.tax.service.gov.uk/display/MDS/Development+space) includes:
- Configuration driven management of data and scopes
- Scope driven query strings for Integration Framework (IF)
- Caching strategy to alleviate load on backend systems

Please ensure you reference the OGD Data Item matrix to ensure the right data items are mapped and keep this document up to date if further data items are added.
(The matrix was last validated at V1.1, please ensure you update with any changes you make.)

### Running tests

Run all the tests with coverage report:

    sbt clean compile coverage test it:test component:test coverageReport

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

### Running locally for performance tests

     sbt run -Dconfig.resource=application.local.conf