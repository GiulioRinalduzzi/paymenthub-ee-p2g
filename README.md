# paymenthub-ee-p2g

The Payment Hub EE Person-to-Government (P2G) service: it lets a payer look up a bill, pay it,
and check the state of that payment, and it drives the matching workflow inside Payment Hub.

[![License](https://img.shields.io/badge/License-MPL--2.0-blue.svg)](LICENSE)

## What it does

- **Bill inquiry** — `GET /bills/{billId}` asks the biller for a bill and returns its details.
- **Bill transfer request** — `POST /billTransferRequests` starts the request-to-pay flow for a
  bill, so the payer can be asked to approve it.
- **Payer response** — `PUT /billTransferRequests` takes the payer's answer to that request and
  passes it back to the waiting workflow.
- **Payment notification** — `POST /paymentNotifications` tells the biller that a bill was paid.
- **Status** — `GET /transferRequests/{correlationId}` reports the state of a request, reading it
  from the operations app.
- **Biller lookup** — resolves a bill id to a biller, using the `billers.details` list in
  `application.yml`.
- Runs Zeebe (Camunda) workers for `discover-biller`, `payerRtpRequest`, `billerRtpResponse`,
  `billFetchResponse`, `billPayResponse` and `sendError`.

## How it fits into Payment Hub EE

Payment Hub EE runs each payment as a Zeebe (Camunda) workflow. This service is the entry point
for the P2G use case: a request arriving on one of its REST endpoints starts a workflow (the BPMN
names are configured under `bpmn.flows`), and its Zeebe workers then pick up the steps of that
workflow that need to talk to the outside world — the biller, the payer's FSP, or the operations
app. Answers that come back later, such as the payer approving a request to pay, arrive on the
REST endpoints above and are published as messages to the workflow that is waiting for them.

## Tech stack

- Java 21
- Spring Boot 3.4
- Apache Camel 4 (routes for bill inquiry, biller fetch, payer response and payment notification)
- Zeebe / Camunda workers (via the Zeebe Java client)
- Gradle build
- Depends on `paymenthub-ee-bom` (for versions) and `paymenthub-ee-core`

## Build and run

    ./gradlew clean build          # compiles and runs the checks
    ./gradlew bootRun              # runs the service locally
    docker build -t paymenthub-ee-p2g .

The service listens on port 5000 for the Camel REST routes and 8080 for Spring Boot and the
actuator endpoints. It expects a Zeebe broker at `zeebe.broker.contactpoint`
(`127.0.0.1:26500` by default) and the operations app at `operations.url`.

## Code style

    ./gradlew spotlessApply        # applies the formatting
    ./gradlew checkstyleMain       # runs the checkstyle rules

## Branches

- `dev` is the active development branch — all PRs should target `dev`.
- `main` holds released versions.

## Contributing

See [contributing.md](contributing.md), our [Code of Conduct](CODE_OF_CONDUCT.md) and the [security policy](security.md).
