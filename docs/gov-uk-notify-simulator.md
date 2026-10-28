# Gov.UK Notify simulator

Stubs the [Gov.UK Notify](https://www.notifications.service.gov.uk/) email and precompiled-letter APIs
so services that send email or letters through it (e.g. `cp-gov-uk-notify-gateway`) can be exercised
end-to-end in lower environments without hitting the real service.

It is a faithful mimic, not a bespoke stub: every behaviour below also occurs against real Gov.UK
Notify, so the same request behaves the same with or without the simulator.

## Endpoints

| Method | Path | Transformer | Purpose |
|--------|------|-------------|---------|
| `POST` | `/v2/notifications/email` | `gov-uk-notify-send-email-response-transformer` | send an email |
| `POST` | `/v2/notifications/letter` | `gov-uk-notify-send-precompiled-letter-response-transformer` | send a precompiled (PDF) letter |
| `GET`  | `/v2/notifications/{id}`  | `gov-uk-notify-get-status-response-transformer` | poll delivery status (email or letter) |

Served under the WAR context, i.e. `…/simulator/v2/notifications/…`.

## Request contract (mandatory)

All endpoints enforce the same mandatory request contract as the real service, so the necessary
configuration is exercised in lower environments too — a missing/blank API key fails here rather than
slipping through and only breaking once the simulator is turned off higher up. Values may be dummy.

- **`Authorization: Bearer <token>`** must be present and non-empty. The Notify client builds this from
  the configured API key, so a missing/blank key (or a client that failed to send the header) is
  rejected with `403 AuthError` — exactly as real Gov.UK Notify does. The JWT signature is **not**
  verified (the simulator has no service secret and the key is a dummy), only its presence and shape.

## Email behaviour

Two independent levers decide the outcome (once the request contract above is satisfied):

1. **Send-time 400** — a request whose `personalisation` is missing the required `material_url`
   placeholder is rejected with a real Gov.UK Notify `400 BadRequestError`
   (`Missing personalisation: material_url`). `material_url` counts as present whether it is a plain
   string or the JSON object produced by `NotificationClient.prepareUpload(...)` for an attachment.
2. **Delivery status** — for an accepted send, the eventual polled status is driven by the **recipient
   address**, using Gov.UK Notify's own reserved simulator addresses. The chosen status is recorded
   against the minted notification id and replayed by the status poll.

| Caller scenario | Request to simulator | Simulator response | Consumer outcome |
|---|---|---|---|
| missing/blank API key (no `Authorization: Bearer`) | any | `403 AuthError` | auth failure (surfaces missing key config) |
| success (attachment / `material_url` present) | `material_url` set | `201` + id; poll → `delivered` | **SENT** |
| permanent failure (no attachment) | `material_url` absent | `400 BadRequestError` | **FAILED** (permanent) |
| recipient `perm-fail@simulator.notify` | `material_url` present | `201`; poll → `permanent-failure` | **FAILED** |
| recipient `temp-fail@simulator.notify` | `material_url` present | `201`; poll → `temporary-failure` | **FAILED** |
| any other recipient | `material_url` present | `201`; poll → `delivered` | **SENT** |

## Letter behaviour (precompiled PDF)

The request is what `NotificationClient.sendPrecompiledLetter*` sends: `reference`, base64 `content`
and an optional `postage`. It is checked as real Gov.UK Notify checks it:

| Request | Simulator response |
|---|---|
| no `reference` | `400 ValidationError` `reference is a required property` |
| no `content` | `400 ValidationError` `content is a required property` |
| `content` not a base64 PDF | `400 ValidationError` `Letter content is not a valid PDF` |
| `postage` other than `first` / `second` / `economy` | `400 ValidationError` `postage invalid. It must be either first, second or economy.` |
| otherwise | `201` `{id, reference, postage}` — postage `second` when none was asked for |

Each status poll then reports the next status of a precompiled letter's real lifecycle; the last status
is reported on every later poll. **Every** `GET` for the id moves it on, so a poll you make by hand while
debugging (e.g. with curl) uses up a status the service would otherwise have seen. Real Gov.UK Notify
decides a letter's outcome from the PDF itself, so the simulator reads it from the PDF too: the text
`simulator.notify:<status>` selects the outcome. It is matched in the raw PDF bytes, so it must be in an
uncompressed content stream — as in the PDF `ng-letter-sanity.sh` generates; most real PDFs compress their
text, so they always take the default lifecycle. This marker is the simulator's only letter lever and means
nothing to the real service.

| PDF contains | Polls report | Consumer outcome (`cp-gov-uk-notify-gateway`) |
|---|---|---|
| no marker | `pending-virus-check`, `accepted`, `received` | **SENT** |
| `simulator.notify:accepted` | `pending-virus-check`, `accepted` (stays) | stays in progress — polled every 12h until its retries run out |
| `simulator.notify:validation-failed` | `pending-virus-check`, `validation-failed` | resent 5 times (each send fails the same way), then **FAILED** `400` |
| `simulator.notify:virus-scan-failed` / `technical-failure` / `permanent-failure` | `pending-virus-check`, that status | **FAILED** |

The status poll answers a letter as `type: letter` with its `postage`. Template (non-precompiled)
letters are not simulated.

The send and its polls are correlated in a file under the pod's temp directory, so they must reach the same
simulator pod, and a restart loses them. A poll for an id the simulator does not know is answered as a
`delivered` **email** (real Gov.UK Notify answers `404`), which a letter's consumer treats as a failure: run
the simulator with one replica (`replicaCount: 1`, no autoscaling), as it is today.

## Using it from a service

Point the service's Gov.UK Notify base URL at the simulator (no trailing slash — the client appends
`/v2/notifications/…`). In-cluster, the simulator is reachable at the `platform-simulators` release's
service on port 80, under the `/simulator` context:

```
http://platform-simulators-wildfly-app/simulator
```

For `cp-gov-uk-notify-gateway` this is `CP_NG_GOVNOTIFY_BASE_URL`, set per environment in
`cpp-flux-config` (currently dev + ste).

## Notes / assumptions

- `material_url` is treated as the required placeholder because that is what the NG template requires;
  real Gov.UK Notify 400s for the same omission. If a caller with a different template (not requiring
  `material_url`) is ever routed here, this check needs revisiting.
- The reserved `*@simulator.notify` addresses and the missing-personalisation 400 are genuine Gov.UK
  Notify behaviours, so the same scenarios are valid against the real service too.
