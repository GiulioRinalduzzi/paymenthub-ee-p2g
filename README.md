# paymenthub-ee-p2g

The two sides of the Payment Hub EE bill-payment flow, in one repository. Both are deployable
Spring Boot applications with their own Docker image.

[![License](https://img.shields.io/badge/License-MPL--2.0-blue.svg)](LICENSE)

| module | what it is | image |
|--------|------------|-------|
| [`p2g/`](p2g) | the **payer** side: the Person-to-Government service, where a payer looks up a bill, pays it and checks the state of that payment (former `ph-ee-bill-pay`) | `openmf/paymenthub-ee-p2g` |
| [`crm/`](crm) | the **biller** side: answers what a customer owes and accepts the payment (former `ph-ee-connector-crm`) | `openmf/paymenthub-ee-crm` |

They live together because they are only useful together. The deployed
`payment_notification` process has exactly two service tasks: `billPay`, served by `crm`, and
`billPayResponse`, served by `p2g`. Splitting them across repositories means every change to the
flow is two PRs that have to land in step.

## p2g — the payer side

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
- Zeebe workers: `discover-biller`, `payerRtpRequest`, `billerRtpResponse`, `billFetchResponse`,
  `billPayResponse`, `sendError`.

Listens on 8080. Its REST endpoints are Spring MVC controllers and all its Camel routes are
`direct:` ones, so `camel.server-port` (5000) is configured but never bound.

## crm — the biller side

- **Bill lookup** — `GET /bills/{billId}` returns the amount due and the customer it belongs to.
- **Payment notification** — `POST /paymentNotifications`, the message that says a payment went
  through.
- There is also a `POST /billTransferRequests` declared in `BillRtpRespApi`, but no controller
  implements it, so the path is not served. The request-to-pay answer goes through the
  `billRTPResp` worker instead.
- Zeebe workers: `fetch-bill`, `billPay`, `billRtpAck`, `billRTPResp`.
- Handles the request-to-pay answer, including the case where the bill was already paid, which the
  workflow has to tell apart from a real failure.

Listens on 8080.

## How it fits into Payment Hub EE

Payment Hub EE runs each payment as a Zeebe (Camunda) workflow. A request arriving on one of the
`p2g` REST endpoints starts a workflow (the BPMN names are configured under `bpmn.flows`), and the
Zeebe workers of both modules then pick up the steps that need to talk to the outside world: the
biller, the payer's FSP, or the operations app. Answers that come back later, such as the payer
approving a request to pay, arrive on the REST endpoints and are published as messages to the
workflow that is waiting for them.

Neither module keeps a database of its own.

## Tech stack

- Java 21
- Spring Boot 3.4
- Apache Camel 4 (routes for bill inquiry, biller fetch, payer response and payment notification)
- Zeebe / Camunda workers (via the Zeebe Java client)
- Gradle build, with checkstyle, spotless and Error Prone per module
- Depends on `paymenthub-ee-bom` (for versions) and `paymenthub-ee-core`

## Build and run

    ./gradlew clean build              # builds both modules with all their gates
    ./gradlew -p crm build             # one module only
    ./gradlew :p2g:bootRun             # runs one of them locally

Use `-p <module> build` for a single module, not `:<module>:build`: Error Prone only turns itself
on when the task name is exactly `build` or `check`, so `:crm:build` compiles without it and can
pass here while failing in CI.

Each module ships its own Dockerfile, built **from the repository root** so the jar path stays
module-qualified:

    docker build -f p2g/Dockerfile -t paymenthub-ee-p2g .
    docker build -f crm/Dockerfile -t paymenthub-ee-crm .

Both expect a Zeebe broker at `zeebe.broker.contactpoint` (`127.0.0.1:26500` by default), and
`p2g` also expects the operations app at `operations.url`.

## Code style

    ./gradlew spotlessApply            # applies the formatting to both modules
    ./gradlew :crm:checkstyleMain      # checkstyle for one module

Each module keeps its own `config/` directory: the two checkstyle configurations are not identical,
so they cannot be merged into a single one at the repository root.

## Branches

- `dev` is the active development branch — all PRs should target `dev`.
- `main` holds released versions.

## Contributing

See [contributing.md](contributing.md), our [Code of Conduct](CODE_OF_CONDUCT.md) and the [security policy](security.md).
