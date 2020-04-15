# loteria-rest
A small practice application to query spanish Lottery results

It is a tiny Spring Boot microservice calling an external source to retrieve results and matching with specified participations.

Request example:
*PUT* to path */*
[
  {
    "amount": "34",
    "number": "96090"
  }
]

[
  {
    "amount": 204,
    "participation": {
      "number": "96090",
      "amount": 34
    }
  }
]
