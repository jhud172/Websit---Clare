# Clare's Life Celebrations

Launch brochure website for Clare Brunton's celebrant business, built with Spring Boot, Thymeleaf, a dedicated JS file, and a dedicated compiled CSS file generated from a Tailwind-enabled source stylesheet.

## Stack

- Java 17
- Spring Boot
- Thymeleaf
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
- `/reviews`
- `/contact`
- `/privacy`
- `/thank-you`

The blog routes can remain in the codebase, but they are not part of the launch navigation.

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

## Key files

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

The launch contact form collects:

- full name
- email
- phone
- ceremony type
- event date
- venue
- enquiry message
- privacy consent

The form posts into Spring Boot and is ready to:

1. email Clare a new enquiry notification
2. send the enquirer a confirmation email

If SMTP variables are missing, the site still runs and logs the enquiry rather than failing hard.

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
- `REVIEWS_STORAGE_DIRECTORY` defaults to `data/reviews`
- `REVIEWS_PHOTO_DIRECTORY` defaults to `uploads`
- `REVIEWS_ADMIN_USERNAME` required for `/reviews/admin`
- `REVIEWS_ADMIN_PASSWORD` required for `/reviews/admin`
- `ANALYTICS_STORAGE_DIRECTORY` defaults to `data/analytics`

On Render free web services, use the Resend variables rather than Gmail SMTP. Render blocks outbound SMTP ports on free services, so `smtp.gmail.com:587` can time out even when the Gmail app password is correct.

If you use Gmail SMTP locally, set `SPRING_MAIL_PASSWORD` to a Google App Password (not your normal account password). Error `534-5.7.9 Application-specific password required` means Gmail rejected the login.

## Reviews moderation

- Public visitors can submit reviews from `/reviews`.
- Submissions are stored as pending and do not auto-publish.
- Admin moderation routes:
  - `/reviews/admin/login`
  - `/reviews/admin`
- Approved reviews appear on `/reviews` and approved five-star reviews are featured on the homepage carousel.
- Jessica's supplied wedding review is source-backed so it remains available across deployments; the former fictional demo reviews are no longer shown or seeded.

The default JSON review store is suitable for local development. The current free Render service has an ephemeral filesystem, so newly submitted reviews and uploaded review photos are not durable across production restarts or redeployments. Moving those submissions to durable external storage or a paid persistent disk requires a separate implementation decision and explicit approval before any billable change.

## Local visit analytics

The admin panel shows simple aggregate public page visit counts for today, the last 7 days, the last 30 days and all time. Counts are stored in `ANALYTICS_STORAGE_DIRECTORY` and do not store names, IP addresses, user agents or personal visitor profiles.

## Docker / Render deployment

The Docker build now has two build stages before runtime:

1. Node builds the compiled site CSS
2. Maven packages the Spring Boot application

Suggested deploy path:

1. Push the repo to GitHub
2. Create the Render web service from the repo or Blueprint
3. Let Render build from `Dockerfile`
4. Set the environment variables
5. Connect the custom domain in Render
6. Point DNS from Cloudflare to Render

## Notes for content review

Before launch, Clare should still sign off:

- final about copy
- real photography
- publication permission and supporting evidence for supplied testimonials
- whether prices should be shown as fixed or “from” prices, plus their effective date
- deposits, payment timing, cancellation and rescheduling terms
- the exact mileage calculation and any final wording around travel
- the preferred public label for the concise £275–£350 farewell option
- an official North East Wedding Network profile link and any badge-usage rules
- any final social profile links

## Important legal note

Do not publish wording that implies Clare can perform a legally binding marriage ceremony unless that legal position has been formally confirmed.
