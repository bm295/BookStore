# Book demand classification

Estimate whether a book will sell quickly in the next 30 days using the units sold in the previous 30 days and the price at prediction time. Use a single currency and a consistent labeling policy. For example, label a historical sample FAST_SELLING if at least 20 units sold in its following 30 days, and SLOW_SELLING otherwise. The caller supplies the labels. Do not include future sales in the input features.

Run BookStore using Java 25 and `mvn spring-boot:run`. POST the following JSON to `/api/books/demand/train`:

```json
[
  {"features":{"unitsSoldLast30Days":2,"price":20},"label":"SLOW_SELLING"},
  {"features":{"unitsSoldLast30Days":25,"price":22},"label":"FAST_SELLING"}
]
```

Training returns HTTP 204. The two synthetic observations demonstrate the request format. POST `{"unitsSoldLast30Days":23,"price":22}` to `/api/books/demand/predict` to receive `{"label":"FAST_SELLING"}` with that model. Missing inputs, invalid values, a training set without both labels, or prediction before training return HTTP 400.

The model divides each feature by its maximum training value (minimum scale 1), then learns the mean point for each class. Prediction picks the nearest mean, breaking ties toward SLOW_SELLING. The model is held in memory for the lifetime of the application. A successful training request replaces it.

Evaluate on later historical periods held out from training. Compare precision and recall for FAST_SELLING with a simple baseline before using predictions for stock decisions.
