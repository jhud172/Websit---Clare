# Free production-readiness checklist

Status date: 20 August 2026. This distinguishes completed repository work from external actions that need Clare, platform access, credentials, paid approval or legal sign-off.

## Completed in code

- [x] Flyway V1 schema plus compatibility migration; Hibernate validates rather than mutates production schema.
- [x] H2 migration integration coverage and PostgreSQL CI startup/migration smoke path.
- [x] Guarded `pg_dump` backup and checksum plus confirmation-gated `pg_restore` tooling and restore rehearsal runbook.
- [x] Interactive BCrypt admin hash generator that does not accept plaintext on the command line.
- [x] Trusted-proxy handling, secure/HttpOnly/SameSite cookie configuration, CSRF, session fixation protection, login throttling and bounded expiring public rate limits.
- [x] Nonce-capable CSP without `unsafe-inline`, HSTS on HTTPS, content-type, referrer and permissions headers.
- [x] Public minimal liveness/readiness health checks covering database and media configuration; non-health actuator routes are admin-only.
- [x] Reference-ID notification logging, database-first enquiry persistence, upload signature validation, cleanup on failed persistence and submission idempotency.
- [x] Private enquiry attachments require admin authentication and record ownership; public review media cannot retrieve other storage categories.
- [x] Dashboard search, service/status filters, sort order, archived visibility control, per-status counts, overdue follow-up flags, “not proceeding”, private attachment downloads and mobile layout.
- [x] Destructive review deletion confirmation and existing CSRF enforcement.
- [x] Aggregate service/package/source analytics and honest funnel conversion rates without IP, user-agent or profile records.
- [x] Disabled calendar/official-review provider abstractions, disabled-by-default Turnstile widget and server verifier, and report-only retention architecture.
- [x] CI dependency audit, JavaScript syntax, CSS reproducibility, Java tests/package, YAML/whitespace, PostgreSQL runtime, probes, route/header and axe checks.
- [x] Local PostgreSQL 17 backup-to-restore rehearsal proved the dump, checksum and restored-data verification path on 20 August 2026.

## Can still be completed for free

- [x] Run `npm ci`, the dependency audit, CSS build, JavaScript syntax check, clean Maven package and whitespace/YAML checks for this release candidate.
- [x] Smoke-run the packaged JAR, all principal routes, both health probes, the progressive enquiry flow and a pending-review submission at desktop/mobile widths.
- [x] Run the six-page Axe suite and focused keyboard/modal checks for this release candidate.
- [ ] Create a feature branch/pull request and let the checked-in GitHub workflow repeat the PostgreSQL 17, route, header and Axe gates before merge.
- [ ] Repeat the restore rehearsal from the selected real pre-release backup and record the operator, date and result.

## Requires Clare's permission

- [ ] Clare approves final copy, prices/effective date, deposit/payment/cancellation/rescheduling/travel wording and privacy/retention periods.
- [ ] Clare supplies or approves final photographs, testimonial evidence/permission, official partner badge rules and final social/profile links.
- [ ] A real production backup target, schedule, encryption/access policy, alerting destination and named incident owner are agreed.
- [ ] Calendar, Google Business Profile or Turnstile stays disabled until official API credentials, scopes, UI/privacy changes and client approval are complete.
- [ ] Legal/business owner approves retention and deletion rules before any automatic deletion implementation is enabled.

## Requires production credentials

- [ ] Owner creates/rotates the production admin username and BCrypt hash, PostgreSQL credentials, private media-storage credentials and Resend sender/domain settings.
- [ ] Hosting owner confirms the real reverse-proxy ranges and prevents direct origin access before changing `TRUSTED_PROXY_REGEX`.
- [ ] Authorised owner configures the production domain/DNS, HTTPS and environment variables after reviewing the deployment checklist.
- [ ] Calendar, Google Business Profile and Turnstile credentials/scopes are supplied only for integrations Clare has approved.

## Requires spending money

- [ ] Move the Render web service to a paid always-on instance only if Clare approves always-on hosting instead of the checked-in free plan.
- [ ] Purchase a paid PostgreSQL, object-storage, backup or monitoring plan only if the approved production design cannot meet its requirements on a suitable free allowance.

## Explicitly not performed

- No paid resource, production deployment, DNS change, live credential creation, real email delivery or external account mutation was made.
- No Google review scraping was added.
- No automatic personal-data deletion was enabled without an approved policy.
- Large working public CSS/JavaScript was not riskily rewritten solely to chase a cosmetic bundle target; compression, cache headers and image derivatives remain the safe optimisation path.
