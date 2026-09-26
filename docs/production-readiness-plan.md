# Clare’s Life Celebrations — production readiness plan

## Implemented in the application

- Spring Boot 4.1 and the fixed Spring Framework dependency line.
- Spring Security for dashboard route protection, BCrypt credentials, CSRF, session rotation, expiry, secure-cookie support and security headers.
- Five-attempt admin login throttling with a 15-minute temporary lock.
- PostgreSQL/JPA persistence for enquiries, reviews, moderation state and aggregate analytics.
- Database-first enquiry handling so email is only a notification channel.
- S3-compatible media storage for Cloudflare R2, AWS S3 or another compatible provider; database media storage remains available for local development.
- Magic-byte validation for JPG, PNG, WebP and PDF uploads, alongside size/count and path controls.
- Invisible honeypot, minimum completion time and IP-window rate limits for public enquiry and review submissions.
- Progressive availability enquiry with optional phone, venue, message and attachments.
- Package and source-page context on enquiry actions.
- Clare Dashboard enquiry workflow: New → Contacted → Consultation booked → Quote sent → Booked → Completed.
- Aggregate conversion funnel without visitor profiles or stored IP addresses.
- Dedicated Naming Ceremony and Vow Renewal pages, simplified service navigation and availability-focused CTAs.
- Higher-contrast peacock-led colour hierarchy and reduced routine reveal animation.
- Response compression, seven-day public asset caching, component-organised CSS sources and exclusion of legacy design exports from the production JAR.
- Dependabot configuration for GitHub Actions, Maven and npm.
- CI packaging plus Axe checks across the six principal public journeys, using a ChromeDriver matched to the CI runner browser.

## External production gates — approval or credentials required

These are intentionally not created or changed automatically because they can be billable or require access to Clare’s accounts.

1. Approve a paid, always-on Render web-service instance. The current `plan: free` remains unchanged until explicit approval.
2. Provision a durable PostgreSQL service with backups and set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.
3. Create an S3-compatible bucket (Cloudflare R2 is suitable), keep it private, and set the six `MEDIA_STORAGE_*` values in Render.
4. Generate the dashboard password hash locally, store only `ADMIN_PASSWORD_HASH` and `ADMIN_USERNAME` in Render, and remove the legacy plaintext `REVIEWS_ADMIN_PASSWORD` variable if present.
5. Run a one-off migration of any surviving `data/reviews/reviews.json`, review uploads and analytics files before switching live traffic. Do not assume files on the free Render instance still exist.
6. Verify the production CSP and all security headers with the deployed domain, including any Cloudflare overrides.
7. Confirm a retention/deletion policy with Clare for enquiries, private notes, reviews and attachments; update the privacy notice with the named database/object-storage providers.
8. Google Calendar Free/Busy remains a separate integration. It requires Clare’s Google OAuth approval and a decision about which calendar represents availability. The site must never expose event names or private calendar content.
9. Contextual Google review import requires Clare’s Google Business Profile permission and must preserve the provider’s attribution rules.
10. Turnstile escalation is prepared as the next layer only if production traffic shows abuse; it requires a Cloudflare site key and secret.

## Deployment order

1. Take a database backup and export any recoverable filesystem data.
2. Provision PostgreSQL and object storage.
3. Set all Render secrets and verify `SESSION_COOKIE_SECURE=true`.
4. Deploy to a non-public preview service and run the full test/browser checklist.
5. Import legacy records once, then verify counts and sample images.
6. Switch live traffic only after enquiry submission, dashboard login, moderation and email notification all pass.
7. Upgrade the Render web-service instance after Clare’s explicit billing approval.
