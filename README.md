# Clare's Life Celebrations

Launch brochure website for Clare Brunton's celebrant business, built with Spring Boot, Thymeleaf, a dedicated JS file, and a dedicated compiled CSS file generated from a Tailwind-enabled source stylesheet.

## Stack

- Java 17
- Spring Boot 4.1
- Thymeleaf
- Spring Security with BCrypt admin credentials and CSRF protection
- PostgreSQL/JPA with versioned Flyway migrations for enquiries, reviews and aggregate analytics
- S3-compatible object storage for production uploads
- Tailwind + PostCSS build pipeline
- Dedicated JS in `src/main/resources/static/js/site.js`
- Docker deploy to Render

This keeps the project within the repo rules:

- HTML templates contain markup only
- CSS lives in dedicated stylesheet files
- JavaScript lives in dedicated JS files
- The frontend remains server-rendered and content-led rather than over-engineered

## Launch routes

- `/`
- `/about`
- `/services`
- `/weddings`
- `/celebrations-of-life`
- `/naming-ceremonies`
- `/vow-renewals`
- `/reviews`
- `/contact`
- `/privacy`
- `/thank-you`

The Journal remains in the launch navigation to support useful ceremony guidance and search visibility.

`/ceremonies` and the previous Celebration of Life URL remain as compatibility redirects for saved links and existing search traffic.

## Local development

Install frontend dependencies:

```bash
npm install
```

Build the CSS once:

```bash
npm run build:css
```

Or watch the source stylesheet while designing:

```bash
npm run dev:css
```

Run Spring Boot:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Then open `http://localhost:8081`.

## Ceremony photographs and film

The seven new client photographs appear in the homepage introduction, About page and the Weddings page gallery. Placement details are in [docs/ceremony-photography.md](docs/ceremony-photography.md).

The supplied woodland ceremony clip is included at `src/main/resources/static/videos/weddings/woodland/woodland-wedding-ceremony.mp4`, with a matching poster and a portrait player on the Weddings page. Photos are grouped by celebration (`woodland` and `floral-arch`) with descriptive filenames. See [video format and replacement instructions](src/main/resources/static/videos/README.md).

## Key files

### Carousel motion

Photo carousels use a shared crossfade in `src/main/frontend/styles/components/16-carousel-motion.css`. JavaScript supplies the same transition duration to CSS and completes the change on the animation event, with a timeout fallback. The next photograph is decoded before it appears; a failed image leaves the current photograph visible. The latest manual selection is queued during a transition.

Carousel dots have fixed hit areas so the controls do not shift. Autoplay pauses while the browser tab is hidden, and reduced-motion preferences disable transitions and initial autoplay.

### Source files

- `src/main/java/co/uk/clarebrunton/ceremonies/controller/SiteController.java`
- `src/main/java/co/uk/clarebrunton/ceremonies/model/InquiryForm.java`
- `src/main/java/co/uk/clarebrunton/ceremonies/service/InquiryNotificationService.java`
- `src/main/resources/templates/`
- `src/main/frontend/styles/site.css`
- `src/main/resources/static/css/site.css`
- `src/main/resources/static/js/site.js`
- `src/main/resources/application.yml`
- `Dockerfile`
- `render.yaml`

## Enquiry flow

The progressive availability form collects four essentials first:

- full name
- email
- ceremony type
- a preferred date or “flexible/not decided”

The second step optionally collects phone, venue, message and attachments. Package buttons also send the selected package and source page. Every valid enquiry is written to the database before email notification is attempted, and privacy consent is required before submission.

The availability modal uses `17-enquiry-experience.css` for its portrait-led layout, consistent field spacing and reduced-motion-aware transitions. JavaScript enhances the bound ceremony select into a themed keyboard-accessible menu and the date preference into visible radio choices; native selects remain available without JavaScript. The second step shows a ceremony summary and message character count, and going back preserves entered details. Existing field names, server validation, consent, uploads and submission tokens are retained.

The form posts into Spring Boot and is ready to:

1. email Clare a new enquiry notification
2. send the enquirer a confirmation email

If email delivery is unavailable, the saved dashboard record remains available for follow-up.

## Environment variables

Set these in Render before launch:

- `SITE_NAME` defaults to `Clare's Life Celebrations`
- `SITE_TAGLINE` defaults to `For Moments That Matter`
- `SITE_CONTACT_EMAIL`
- `SITE_PHONE_NUMBER`
- `SITE_INSTAGRAM_URL`
- `SITE_BASE_URL`
- `SITE_FROM_EMAIL`
- `INQUIRY_NOTIFICATION_EMAIL`
- `REVIEWS_NOTIFICATION_EMAIL`
- `RESEND_API_KEY`
- `RESEND_FROM_EMAIL`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_SMTP_CONNECTION_TIMEOUT` defaults to `5000`
- `SPRING_MAIL_SMTP_TIMEOUT` defaults to `5000`
- `SPRING_MAIL_SMTP_WRITE_TIMEOUT` defaults to `5000`
- `SPRING_DATASOURCE_URL` (a JDBC PostgreSQL URL in production)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `ADMIN_USERNAME` required for `/reviews/admin`
- `ADMIN_PASSWORD_HASH` required for `/reviews/admin` and must be a BCrypt hash
- `MEDIA_STORAGE_PROVIDER` use `s3` in production or `database` for local development
- `MEDIA_STORAGE_BUCKET`, `MEDIA_STORAGE_REGION`, `MEDIA_STORAGE_ENDPOINT`, `MEDIA_STORAGE_ACCESS_KEY`, `MEDIA_STORAGE_SECRET_KEY`
- `SESSION_COOKIE_SECURE=true` in production
- `TRUSTED_PROXY_REGEX` only when the deployment proxy ranges differ from the safe private/loopback default
- `FLYWAY_BASELINE_ON_MIGRATE=true` for the first migration-aware start of an existing database; set it to `false` after adoption
- `TURNSTILE_ENABLED`, `TURNSTILE_SITE_KEY`, `TURNSTILE_SECRET_KEY` (all optional; disabled by default)
- `CALENDAR_INTEGRATION_ENABLED=false` and `GOOGLE_REVIEWS_INTEGRATION_ENABLED=false` remain off until official API access and client approval exist
- `RETENTION_INQUIRIES`, `RETENTION_REVIEWS`, `RETENTION_ANALYTICS`, `RETENTION_ASSETS` are report-only candidate periods; no automatic deletion is enabled

On Render free web services, use the Resend variables rather than Gmail SMTP. Render blocks outbound SMTP ports on free services, so `smtp.gmail.com:587` can time out even when the Gmail app password is correct.

`MAIL_HEALTH_ENABLED` defaults to `false` because email can use the Resend HTTPS API instead of SMTP. Set it to `true` only when SMTP is configured and should be included in the application health check.

If you use Gmail SMTP locally, set `SPRING_MAIL_PASSWORD` to a Google App Password (not your normal account password). Error `534-5.7.9 Application-specific password required` means Gmail rejected the login.

## Clare Dashboard

- Public visitors can submit reviews from `/reviews`.
- Submissions are stored as pending and do not auto-publish.
- Secure admin routes:
  - `/reviews/admin/login`
  - `/reviews/admin`
- The dashboard stores enquiries and tracks the workflow from New through Contacted, Consultation booked, Quote sent, Booked and Completed.
- It also displays aggregate conversion events and review moderation.
- Approved reviews appear on `/reviews` and approved five-star reviews are featured on the homepage carousel.
- Jessica's supplied wedding review is source-backed so it remains available across deployments; the former fictional demo reviews are no longer shown or seeded.

Local development defaults to an H2 database under `data/`. Production must be configured with PostgreSQL and S3-compatible media storage; no customer record relies on the Render filesystem.

Generate an admin hash without exposing the password in shell history:

```powershell
.\scripts\generate-admin-password.ps1
```

Database backup and restore procedures, migration adoption, monitoring probes, optional integration flags and recovery verification are documented in `docs/operations-runbook.md`. The scripts require PostgreSQL client tools and use standard `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` and `PGPASSWORD`/pgpass configuration; credentials are never accepted as script arguments.

## Local visit analytics

The admin panel shows public page totals and an aggregate funnel from service view to submitted enquiry. Analytics records do not store names, IP addresses, user agents or personal visitor profiles.

## Docker / Render deployment

The Docker build now has two build stages before runtime:

1. Node builds the compiled site CSS
2. Maven packages the Spring Boot application

Suggested deploy path:

1. Push the repo to GitHub
2. Create the Render web service from the repo or Blueprint
3. Let Render build from `Dockerfile`
4. Provision durable PostgreSQL and S3-compatible object storage, then set the environment variables
5. Connect the custom domain in Render
6. Point DNS from Cloudflare to Render

The checked-in Blueprint deliberately remains on Render’s free web plan until Clare approves a paid always-on instance. No production, paid or DNS change is performed by this repository work. See `docs/free-production-readiness-checklist.md` for completed local work and exact external gates.

## Notes for content review

Before launch, Clare should still sign off:

- final about copy
- real photography
- publication permission and supporting evidence for supplied testimonials
- whether prices should be shown as fixed or “from” prices, plus their effective date
- deposits, payment timing, cancellation and rescheduling terms
- the exact mileage calculation and any final wording around travel
- the preferred public label for the concise £250–£300 farewell option
- an official North East Wedding Network profile link and any badge-usage rules
- any final social profile links

## Important legal note

Do not publish wording that implies Clare can perform a legally binding marriage ceremony unless that legal position has been formally confirmed.
