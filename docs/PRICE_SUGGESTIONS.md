# Book price suggestions

This Java 21 feature supplies advisory prices to operators. Catalog prices, cart
discounts and payment calculations continue to use their existing business rules.
Suggestions never write to the catalog. There is no automatic price approval.

## API

`POST /api/pricing/suggestions/train` accepts a JSON array of at least 10 samples:

```json
[
  {"features":{"pageCount":100,"publicationAgeYears":0},"price":30.00},
  {"features":{"pageCount":200,"publicationAgeYears":1},"price":38.00}
]
```

The two rows above illustrate the shape only; supply at least 10 actual historical
observations in the same currency. Success returns HTTP 204 and replaces the saved
model. This endpoint is intended for the existing local operator application;
authentication is not implemented in this repository.

`POST /api/pricing/suggestions` accepts:

```json
{"pageCount":350,"publicationAgeYears":1}
```

Response shape:

```json
{"suggestedPrice":53.00}
```

The response value depends on training data. Missing models, insufficient samples,
invalid features, and negative/non-finite predictions are rejected. Input validation
returns HTTP 400. Page count must be positive, age must be nonnegative, and sample
prices must be finite and nonnegative. Suggestions round to two places using HALF_UP.

## Model and storage

The application use case depends on `BookPricePredictor` and `PriceModelRepository`
ports. Infrastructure implements cubic polynomial ridge regression and JSON storage.
Training runs only on the training endpoint. Prediction loads a saved model without
retraining. Training data is supplied by the caller and is not retained; keep the
original historical dataset externally for reproducibility.

Features are ordered as page count then publication age in years. Both are centered
and scaled using training means and population standard deviations; constant columns
use scale 1. The normalized cubic basis is:

```text
1, x, y, x², xy, y², x³, x²y, xy², y³
```

Training solves averaged normal equations with ridge strength 0.000001, excluding
the intercept. Partial pivoting rejects pivots below 0.000000000001. All state is
local, and model lists are immutable. This is a small fixed-feature baseline;
normal equations are less numerically robust than QR/SVD for difficult datasets.

`bookstore.data-directory` configures storage (default `data`). `price-model.json`
contains `schemaVersion: 1`, two-element `means` and `scales`, and ten `coefficients`
in the basis order above. Unsupported versions or invalid model values are rejected.
Writes use a temporary file and atomic replacement when supported; otherwise they
fall back to replacement. Readers never create a model. Concurrent writers follow
last successful replacement wins; deploy as a single local process.

## Validation and limits

JUnit tests check a held-out synthetic linear price relationship, constant features,
repeat training, invalid inputs, HTTP routing, and model persistence. Synthetic error
checks prove implementation behavior, not accuracy on real book prices. Before using
suggestions operationally, hold out real historical observations and compare MAE
against a simple baseline. Cubic extrapolation beyond training examples may be poor.
No production accuracy claim or automatic price change is made.
