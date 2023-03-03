### Tracker Service

Tracker is a price tracking application that allows users to subscribe to product pages and receive updates when the price changes.

#### Prerequisites

To run this service, you'll need:
* JDK 11 or later
* sbt
* Docker
* Docker Compose

#### Dependencies
The project is built using the following technologies:

* Scala programming language
* Cats and Cats Effect libraries for functional programming
* HTTP4S for serving HTTP requests
* Doobie for database access
* Circe for JSON serialization/deserialization
* fs2 for streaming

#### Running the service

Clone the repository
```bash
git clone https://github.com/slmzig/amazon-tracker.git
```
start postgresSql
```bash
docker compose -f docker-compose.dev.yml up
```
run application
```bash
sbt run
```

#### Usage

Once the project is running, you can interact with it using HTTP requests. Here are some example requests:
Subscribe to a product

```bash
$ curl -X POST http://localhost:8080/subscribe -H "Content-Type: application/json" -d '{"url": "https://www.amazon.com/dp/B01M7VJDCY"}'

```

Unsubscribe from a product
```bash
$ curl -X DELETE http://localhost:8080/unsubscribe/<subscriptionId>

```
Get price changes for a product
```bash
$ curl http://localhost:8080/subscriptions/<subscriptionId>
```
