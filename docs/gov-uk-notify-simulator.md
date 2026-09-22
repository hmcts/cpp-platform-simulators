# Gov.UK Notify simulator

Stubs the [Gov.UK Notify](https://www.notifications.service.gov.uk/) email API so services that send
email through it (e.g. `cp-gov-uk-notify-gateway`) can be exercised end-to-end in lower environments
without hitting the real service.

It is a faithful mimic, not a bespoke stub: every behaviour below also occurs against real Gov.UK
Notify, so the same request behaves the same with or without the simulator.

## Endpoints

| Method | Path | Transformer | Purpose |
|--------|------|-------------|---------|
| `POST` | `/v2/notifications/email` | `gov-uk-notify-send-email-response-transformer` | send an email |
| `GET`  | `/v2/notifications/{id}`  | `gov-uk-notify-get-status-response-transformer` | poll delivery status |

Served under the WAR context, i.e. `…/simulator/v2/notifications/…`.

## Request contract (mandatory)

Both endpoints enforce the same mandatory request contract as the real service, so the necessary
configuration is exercised in lower environments too — a missing/blank API key fails here rather than
slipping through and only breaking once the simulator is turned off higher up. Values may be dummy.

- **`Authorization: Bearer <token>`** must be present and non-empty. The Notify client builds this from
  the configured API key, so a missing/blank key (or a client that failed to send the header) is
  rejected with `403 AuthError` — exactly as real Gov.UK Notify does. The JWT signature is **not**
  verified (the simulator has no service secret and the key is a dummy), only its presence and shape.

## Behaviour

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
