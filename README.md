# loteria-rest
A small practice application to query spanish Lottery results

It is a tiny Spring Boot microservice calling an external source to retrieve data and match it against specified participation.

Swagger URL for quick testing: http://localhost:8080/swagger-ui.html#/app/calculatePrizesUsingPUT

Request example:
*PUT* to path */*
```
[
  {
    "amount": "34",     <-- amount in € acquired of number 96090
    "number": "96090"
  },
  {
    "amount": "20",     <-- amount in € acquired of number 40016 
    "number": "40016"
  }
]
```
Response:
```
[
  {
    "amount": 204,       <-- Prize obtained
    "participation": {
      "number": "96090", <-- Winning number
      "amount": 34       <-- amount participated
    }
  }
]
```