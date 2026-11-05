# Historical price data preparation

`POST /api/pricing/suggestions/prepare` accepts a JSON array of historical book observations:

```json
[
  {"pageCount":100,"publicationAgeYears":0,"price":20.0},
  {"pageCount":200,"publicationAgeYears":3,"price":40.0},
  {"pageCount":null,"publicationAgeYears":null,"price":30.0},
  {"pageCount":300,"publicationAgeYears":4,"price":null}
]
```

The response contains `samples` in the shape accepted by the existing `/train` endpoint and a `report`. The report counts input/output rows, rows dropped for missing price, feature imputations, and per-column present/missing counts, minimum, maximum, mean and median **before** cleaning. The feature median used for imputation is computed only from rows with known prices, and half-integers are rounded up to retain integer features. Rows with missing price are dropped, not assigned invented target values. Zero age and zero price are valid; page count must be positive. Invalid non-missing values return HTTP 400. Empty input, no known price, or a feature missing in all eligible rows also returns HTTP 400.

Preparation is advisory: it neither changes source data nor trains or saves a model. Review the report and send the returned samples to `/train` separately; its existing minimum of 10 samples still applies. For model evaluation, split data first and calculate imputation values on training data alone; preparing the entire dataset before splitting can leak information. Reports and samples can contain sensitive business data and are not persisted by this endpoint.
