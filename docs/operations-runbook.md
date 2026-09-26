# Operations runbook

## Database migrations

Flyway owns the schema under `src/main/resources/db/migration`; Hibernate runs in `validate` mode. New databases apply all migrations before JPA starts. For an existing pre-Flyway database, take a verified backup first, start once with `FLYWAY_BASELINE_ON_MIGRATE=true`, confirm the schema history and application smoke checks, then set the variable to `false`. Never edit a migration already applied to a shared database; add the next version.

## Backup

Install PostgreSQL client tools and configure `PGHOST`, optional `PGPORT`, `PGDATABASE`, `PGUSER` and either `PGPASSWORD` for the current process or a protected pgpass file. Do not paste credentials into command arguments.

```powershell
.\scripts\backup-database.ps1 -OutputDirectory D:\ProtectedBackups\Clare
```

The script creates a custom-format compressed dump without ownership/privilege statements plus a SHA-256 manifest. It refuses to overwrite a file. Store both files outside the repository and web-server filesystem, with access restricted to the business owner/operator.

## Restore rehearsal

Use a disposable, non-production PostgreSQL database. The repeated target name is deliberate protection. `-WhatIf` shows the operation; `-ReplaceExisting` enables `--clean --if-exists` and is destructive inside that named database.

```powershell
.\scripts\restore-database.ps1 -BackupFile D:\ProtectedBackups\Clare\clare-YYYYMMDD-HHMMSS.dump -TargetDatabase clare_restore_test -ConfirmTargetDatabase clare_restore_test -WhatIf
.\scripts\restore-database.ps1 -BackupFile D:\ProtectedBackups\Clare\clare-YYYYMMDD-HHMMSS.dump -TargetDatabase clare_restore_test -ConfirmTargetDatabase clare_restore_test -ReplaceExisting
```

Start the application against the restored database with outbound email disabled. Verify Flyway reaches the latest version, `/actuator/health/readiness` is `UP`, the public pages load, the admin can sign in, enquiry/review counts look credible, and private attachments can be downloaded only while authenticated. Record the rehearsal date and result. Test restores regularly; an untested dump is not a recovery guarantee.

## Monitoring and incident triage

- Liveness: `/actuator/health/liveness` (process can continue).
- Readiness: `/actuator/health/readiness` (application, database and media configuration are ready).
- Public health output is intentionally minimal; other actuator endpoints require the admin role.
- Logs use enquiry/review reference IDs. Search by reference, not by email address or message content.
- If notification delivery fails, the dashboard database record remains the source of truth. Check Resend/SMTP configuration, then follow up from the saved record; do not ask the customer to resubmit unless the record is absent.
- If readiness fails after a deployment, inspect migration/database connectivity and media configuration before serving traffic. Roll back the application image only after considering whether a forward-only migration has run.

## Trusted proxies and cookies

Tomcat processes `X-Forwarded-*` only from `TRUSTED_PROXY_REGEX`. Keep direct application access blocked at the platform boundary. Set `SESSION_COOKIE_SECURE=true` behind HTTPS. Do not broaden the trusted proxy expression to all addresses.

## Optional integrations

Calendar, official Google review import and Cloudflare Turnstile are disabled by default. The calendar and review provider interfaces are safe extension points; review pages must never be scraped. Turnstile's conditional widget and fail-closed server verification are implemented, but it requires both keys and must only be enabled after the privacy wording is approved. Calendar access should request read-only free/busy scope, never event-body access.

## Retention

The configured periods currently power report-only candidate counts. Automatic deletion is intentionally absent until Clare approves the exact business/legal periods, treatment of booked-client records, backup expiry and deletion audit requirements. Deletion must remove linked media transactionally or place it in a retry queue; it must never silently leave orphaned personal files.
