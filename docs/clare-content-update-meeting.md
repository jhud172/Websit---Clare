# Clare website content update: implementation record and meeting questions

Updated: 3 August 2026

Sources supplied by Clare: `Wedding Ceremony Packages draft.docx`, the North East Wedding Network membership badge and the peacock-themed Celebrant versus Registrar comparison graphic.

## Completed in this update

- Replaced every wedding-package placeholder with Clare's supplied names, descriptions, inclusions, ideal-client wording and prices.
- Added all twelve supplied wedding optional extras and prices in a responsive, accessible list.
- Added full Naming Ceremony package sections for **Little Star (£395)** and **Grow With Love (£545)**.
- Added full Vow Renewal package sections for **Forever Yours (£695)** and **Forever & Always (£895)**.
- Added the two supplied Celebration of Life packages: **Gentle Farewell (£395)** and **Cherished Memories (£595)**.
- Added the supplied concise venue-ceremony guide price of **£275–£350** and the extended Celebration of Life range of **£395–£595**.
- Added all eleven shared ceremony extras and prices supplied for Naming Ceremonies and Vow Renewals.
- Replaced the remaining public price-confirmation messages in the service FAQs with the confirmed package figures.
- Added the North East Wedding Network badge beside the wedding-package introduction, with intrinsic dimensions and responsive sizing to prevent layout shift.
- Added Jessica's client-supplied five-star wedding review, headline and ceremony date of 24 May 2026.
- Removed the four fictional demo testimonials that were previously displayed as approved feedback; Jessica is now the only source-backed review until more genuine reviews are approved.
- Added British-format review dates, accessible star-rating labels, a readable homepage excerpt and the complete review on the Reviews page.
- Strengthened the review-submission consent wording and added review/photo handling to the privacy policy.
- Kept the canonical `/celebrations-of-life` route and the previous URL redirect so old links continue to work.
- Kept Naming Ceremony, Vow Renewal, wedding and Celebration of Life enquiry buttons connected to the correct preselected enquiry type.
- Aligned the homepage's prominent wording and Google-facing title around **Weddings and Celebrations of Life in Durham**.
- Added Clare's Celebrant versus Registrar graphic to the Wedding page at its full 1024×1536 aspect ratio, with a full-size link and no text-cropping treatment.
- Added an accessible, search-readable explanation of the different celebrant and registrar roles beside the graphic, based on current England and Wales civil-ceremony guidance.
- Replaced the four numbered Services hero markers with Clare's transparent layered emblem.
- Corrected the FAQ emblem styling so the transparent artwork no longer receives a white circular image background.
- Removed every full-rotation logo hover rule and replaced them with one consistent peacock-light interaction: a small lift, gentle wreath expansion, teal-and-gold halo and one short sheen.
- Added a static reduced-motion treatment that keeps the wreath at its correct resting scale.
- Added `/faq` to the generated sitemap and changed former Celebration of Life service and journal URLs to permanent redirects.
- Corrected the recorded production domain to `https://clareslifecelebrations.com` in environment, deployment and SEO documentation.

## Confirmed public prices now implemented

| Service | Package | Price |
| --- | --- | ---: |
| Wedding | Essential Ceremony | £725 |
| Wedding | Signature Ceremony | £925 |
| Wedding | Complete Ceremony Experience | £1,195 |
| Naming Ceremony | Little Star | £395 |
| Naming Ceremony | Grow With Love | £545 |
| Vow Renewal | Forever Yours | £695 |
| Vow Renewal | Forever & Always | £895 |
| Celebration of Life | Gentle Farewell | £395 |
| Celebration of Life | Cherished Memories | £595 |
| Concise venue farewell | Typical guide range | £275–£350 |

Additional travel is shown as 50p per mile where applicable. Each supplied optional extra is listed on the relevant service page.

## Editorial and implementation decisions

- Corrected the clear source typo “person vows” to “personal vows”.
- Replaced “etc.” in the Complete package ritual list with “and more” for polished public wording.
- Used British English and expanded a small number of fragments into complete web sentences without changing the package meaning or price.
- Preserved the prior instruction to use **Celebration of Life** in all visitor-facing service wording. Where Clare's document used the previous term or professional title, the public copy now says “concise venue ceremony”, “personal farewell” or “appointed ceremony professional”.
- Softened the absolute phrase “no time restrictions” to “greater freedom”, because an independent venue may still impose its own limits.
- Clarified that wedding symbolic rituals are optional extras only where they are not already included in the Complete Ceremony Experience.
- Preserved Jessica's attributed wording, including its conversational grammar. Only non-breaking spaces and the missing space after “once!” were normalised.
- Kept Jessica's review in source-controlled curated content because the current free hosting filesystem is not durable. Ordinary visitor submissions remain subject to moderation.
- Retained the wedding-page explanation that legal registration in England and Wales is completed separately with a registrar.
- Treated Clare's supplied comparison graphic as supporting artwork rather than the sole legal explanation. Some statements in its registrar column are broad opinions, so the website's HTML uses neutral, verifiable wording and directs couples to current GOV.UK guidance.

## Questions to ask Clare in the meeting

These points do not block the website update, but they should be confirmed before the next final content sign-off.

### Prices, booking and payment

1. Are all package figures fixed prices, or should any be displayed as “from” prices?
2. From what date do these prices apply, and should the website show a price-list year or review date?
3. Do the prices include all applicable taxes, ordinary materials and expenses?
4. What deposit or booking fee is required, when is the balance due, and what are the cancellation or rescheduling terms?
5. Can optional extras be combined freely, and are any extras already included in a package unavailable as a second or additional ritual?
6. Does the £100 booking-within-six-weeks charge apply to every wedding package, and can it be combined with other extras?

### Travel, visits and delivery

7. Is the 50p mileage charge calculated one way or as a round trip, and from which starting point?
8. Please confirm the distinction between 30 miles of included ceremony travel and the 40-mile Signature venue-visit limit.
9. Does the weekend venue-planning visit charge apply to Naming Ceremonies, Vow Renewals or both?
10. Are there venue size or guest-number limits for use of Clare's PA system?

### Celebration of Life wording

11. Is “concise venue ceremony” the preferred public label for the £275–£350 option, or would Clare like a different name that still follows the site's Celebration of Life terminology?
12. Is “appointed ceremony professional” acceptable where the supplied wording referred to the recognised professional title, or may that title appear as an exception?

### Review and membership evidence

13. Has Jessica explicitly agreed to publication of her name, full review and wedding date on Clare's own website? Clare should retain the original review and permission evidence outside the public repository.
14. Would Jessica like a wedding photograph added to her review, and has she approved that photograph for website use?
15. Is there an official North East Wedding Network profile URL the new badge should link to, and has Clare been given any usage or minimum-size rules for the badge?

### New wording, comparison graphic and Google

16. Is **Weddings and Celebrations of Life in Durham** the exact phrase Clare wants used on the homepage and in the Google-facing title?
17. Does Clare own, license or otherwise have permission to publish the supplied Celebrant versus Registrar graphic on her business website?
18. Would Clare like a corrected version of the comparison graphic that keeps the peacock design but replaces broad registrar statements such as “one-size-fits-all”, “transactional” and “stiff & formal” with neutral role differences?
19. Is Clare happy for the website to state clearly that her independent celebrant-led ceremony is not legally binding by itself in England and Wales and that couples choosing the civil route need a registrar for the legal marriage?
20. Does Clare already control a verified Google Search Console property for `clareslifecelebrations.com`, or will DNS verification access be needed after deployment?
21. Are there Google Business Profile, social-media or directory descriptions that Clare would like updated to the same wording after the website is live?

## Verification record

- `npm run build:css` completed successfully and regenerated the deployable `site.css` bundle.
- `.\\mvnw.cmd test` completed successfully: 48 tests, with no failures, errors or skipped tests.
- `git diff --check` completed successfully; Git reported only the repository's existing line-ending notices.
- A repository-wide source audit found no remaining price placeholders or fictional demo-review names.
- A rendered-text audit found no visitor-visible use of the previous service term on Weddings, Services, Celebrations of Life, Reviews or Privacy. Internal compatibility names, the legacy redirect and a third-party directory URL remain unchanged because visitors do not see them as page wording.
- Live-browser checks covered Weddings, Services, Celebrations of Life, Reviews and Privacy at 320×720, 390×844, 768×1024, 1024×768 and 1440×900. All 25 page/viewport combinations had one main heading, no horizontal page or package-card overflow, and no undersized package enquiry buttons.
- A second whole-site check covered all public pages, all three journal posts and the legacy redirects at 390×844 and 1440×900. All 32 route/viewport combinations returned a working page or the intended redirect, retained one main heading, had no horizontal overflow and contained no previous service term in visible copy, metadata, labels, image descriptions or form prompts.
- The supplied 938×938 membership badge loaded at its full intrinsic resolution and rendered responsively without causing horizontal overflow.
- The full Jessica review was visually checked on a 390px mobile viewport; the card expands naturally without clipping, an inner scrollbar or author-line overlap.
- Package enquiries were interaction-tested: Little Star preselected Naming ceremony, Forever & Always preselected Vow renewal, Gentle Farewell preselected Celebration of Life or memorial, and the Essential wedding package preselected Wedding ceremony.
- The Weddings package anchor cleared the fixed header, and the final browser pass reported no console errors or warnings.
- The new homepage title, description, Open Graph title, Twitter title, hero wording and ProfessionalService alternate name all render as **Weddings and Celebrations of Life in Durham**.
- The former `/funerals` address and former journal address return `301 Moved Permanently`; the sitemap contains `/celebrations-of-life` and `/faq` and excludes both legacy URLs.
- A new rendered whole-site audit covered all twelve public pages and journal routes at 390×844 and 1440×900. All 24 checks returned `200`, retained one main heading, had no horizontal overflow, exposed no previous service term and reported no browser console warnings or errors.
- Focused Services, Weddings and FAQ checks passed at 320×720, 390×844, 768×1024, 1024×768 and 1440×900. The Services rail contains four layered emblems and no number markers; the FAQ emblem and both image layers remain transparent at every size.
- The supplied Wedding comparison graphic loaded at its full 1024×1536 resolution, used `object-fit: contain` and caused no horizontal overflow at any tested viewport. Desktop and mobile screenshots were visually inspected.
- Logo hover inspection showed scale-and-lift matrices with no rotation, an active teal-and-gold halo and the short peacock sheen. Under `prefers-reduced-motion: reduce`, animation and transition durations were `0s`, the sheen was disabled and the wreath retained its resting scale.
- Temporary browser diagnostics and generated local analytics test data were removed after verification. The authoritative baseline/final screenshots remain under `output/playwright/`, and the verified local application is intentionally left running on port 8091 for James to review.
- The final release pass rechecked 11 rendered routes with no new browser console warnings or errors, found no duplicate IDs, missing image alternatives or unnamed links/buttons, and confirmed all 46 discovered internal links/assets resolve successfully.
- The final Maven result is 7 suites and 48 tests with 0 failures, 0 errors and 0 skipped; gzip delivery, seven-day static caching and locally served brand fonts were also verified.
