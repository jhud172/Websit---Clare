# Clare’s Life Celebrations

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
- `/ceremonies`
- `/weddings`
- `/funerals`
- `/reviews`
- `/contact`
- `/privacy`
- `/thank-you`

The blog routes can remain in the codebase, but they are not part of the launch navigation.

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

Then open `http://localhost:8080`.

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

- `SITE_NAME` defaults to `Clare’s Life Celebrations`
- `SITE_TAGLINE` defaults to `for every chapter`
- `SITE_CONTACT_EMAIL`
- `SITE_PHONE_NUMBER`
- `SITE_INSTAGRAM_URL`
- `SITE_BASE_URL`
- `SITE_FROM_EMAIL`
- `INQUIRY_NOTIFICATION_EMAIL`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

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
- final wedding pricing
- final funeral pricing
- real photography
- testimonials once available
- any final wording around travel
- any final social profile links

## Important legal note

Do not publish wording that implies Clare can perform a legally binding marriage ceremony unless that legal position has been formally confirmed.
