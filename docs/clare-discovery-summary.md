# Clare Discovery Summary

Date: 2026-04-15

## Clare's direction

- Business name: Clare's Life Celebrations
- Main launch focus: weddings and funerals first
- Long-term scope: all ceremonies
- Desired tone: boho, modern, warm, personal, fun, bubbly
- Audience: couples, families, venues, funeral directors, and a wider mixed audience
- Contact details to use:
  - Mobile: 07885 428304
  - Email: clarebruntoncelebrant@gmail.com
- Domain preference:
  - `clarebruntonlifeceremonies.com`
- Accreditation:
  - Academy of Modern Celebrants, certified wedding celebrant
- Page style preference:
  - less text
  - around 300 words per page
  - image-led and easy to scan
- Photography:
  - Clare will send portraits
  - stock imagery is acceptable for launch if needed
- Pricing:
  - happy to show prices
  - exact wording and price points still to come
- Legal/privacy:
  - privacy policy required
  - GDPR wording required
  - contract and deposit terms required, but those should be treated as a separate deliverable from the brochure site

## Key business notes

- Clare is retiring from the Police in July and wants the site live quickly.
- The website needs to look professional enough for funeral directors and venues.
- The site should not feel stiff or old-fashioned.
- The brand should feel modern country / boho rather than overly formal.
- Clare wants consistent visual identity across the website, logo, business cards, and social channels.
- Her home address must not appear anywhere on the public site.

## Recommended launch scope

Do not overload phase 1. The fastest premium launch should focus on clarity, trust, and enquiry conversion.

### Phase 1 launch pages

1. Home
2. About
3. Ceremonies
4. Weddings
5. Funerals
6. Reviews
7. Contact
8. Privacy Policy

### Phase 2 after launch

1. Blog / journal
2. Other ceremonies expansion page for naming ceremonies, vow renewals, and milestone ceremonies
3. Advanced wedding enquiry form
4. Funeral-specific enquiry form
5. Contract / booking pack flow
6. Refined brand assets and stationery alignment

## Why this structure

- It keeps the first version lean enough to launch quickly.
- It still gives weddings and funerals their own strong landing pages.
- It avoids burying Clare's main services inside one generic services page.
- It protects the premium feel by reducing clutter.
- It avoids delaying launch for blog content, complex forms, or legal document workflows.

## Page-by-page content direction

### Home

- Strong headline introducing Clare as a modern celebrant
- Short positioning copy
- Immediate split into weddings and funerals
- Trust markers:
  - certified celebrant
  - warm and personal approach
  - available for bespoke ceremonies
- Short testimonial preview
- Clear CTA to enquire

### About

- Clare's background
- Why celebrancy matters to her
- Her tone and personality
- Reassurance about professionalism and care
- Portrait photography

### Ceremonies

- Overview page for all services
- Short sections for:
  - weddings
  - funerals
  - naming ceremonies
  - vow renewals
  - other milestone ceremonies
- Links through to weddings and funerals as the main commercial paths

### Weddings

- Positioning for modern, bespoke ceremonies
- What is included
- Package structure
- FAQ starter
- CTA to enquire

### Funerals

- Positioning for compassionate, well-held services
- Clare's role with families and funeral directors
- Package structure
- Travel note
- CTA to enquire

### Reviews

- Client testimonials
- Can also include venue or funeral director feedback later

### Contact

- Contact form
- direct email link
- mobile number
- simple intro for response expectations

### Privacy Policy

- GDPR-compliant data handling explanation
- contact form and enquiry data usage
- retention, access, deletion, and lawful basis wording

## Pricing and form guidance

Keep this split clear:

- Public site:
  - package summaries
  - guide pricing
  - enquiry CTA
- Later workflow:
  - fuller booking intake
  - contract terms
  - deposit handling

Do not block launch on the advanced booking form. Start with a clean premium enquiry form first.

## Brand direction

- Warm neutrals, soft greens, muted natural tones
- Premium serif paired with a clean supporting sans-serif
- More editorial than corporate
- Avoid anything that feels old-fashioned, overly ornate, or funeral-home generic
- Logo can be explored separately once name and direction are fully locked

## Open items still needed from Clare

1. Final about copy / personal story
2. Exact wedding package wording and pricing
3. Exact funeral package wording and pricing
4. Review/testimonial content
5. Photography
6. Style references, colours, and font inspirations
7. Any additional site references she likes
8. Whether blog is needed at launch or should wait

## Recommended repo implementation order

This project is currently Maven-based Spring Boot, not Gradle-based.

Local run command:

```powershell
.\mvnw.cmd spring-boot:run
```

Primary files likely to change first:

- `src/main/resources/templates/home.html`
- `src/main/resources/templates/about.html`
- `src/main/resources/templates/services.html`
- `src/main/resources/templates/contact.html`
- `src/main/resources/templates/privacy.html`
- `src/main/resources/static/css/site.css`
- `src/main/resources/static/js/site.js`
- `src/main/java/co/uk/clarebrunton/ceremonies/controller/SiteController.java`

Likely new templates to add:

- `src/main/resources/templates/weddings.html`
- `src/main/resources/templates/funerals.html`
- `src/main/resources/templates/reviews.html`

## WhatsApp draft back to Clare

Hi Clare, this is brilliant, thank you. It gives me a much clearer picture of the direction now and I can already see the style it wants to go in.

I think the strongest first version will be warm, modern and premium, with weddings and funerals leading the site first, then the wider ceremony side supporting that around it. That will keep it clear for visitors and help it feel professional straight away rather than too busy.

The business name works well and the tone you described makes sense too, so I can start shaping it around that. I also think keeping the wording lighter and the pages more visual is the right move.

The only bits I need from you now before I lock the first structure properly are:

1. Your about me wording or rough notes
2. Any style references, colours or fonts you like
3. Any photos you want using, even temporary ones
4. Your draft package wording and prices when ready
5. Any testimonials or reviews if you already have people in mind

Once I’ve got those, I’ll map the pages out properly and show you the full direction before I get too far into the build.

## Internal recommendation

Do not promise the contract flow yet.

The contract, deposit terms, and advanced booking capture should be framed as a second-step business workflow deliverable unless Clare explicitly wants to pay for that in the first scope.
