# Website finalisation record — 3 August 2026

## Outcome

Clare's Life Celebrations website has received a full visual, interaction, accessibility, performance and content finalisation pass. The existing dark peacock-led identity has been retained and refined into a smoother, more consistent experience across desktop and mobile rather than replaced with a different design direction.

This record distinguishes work that is complete in the local project from the final publication decisions that still require Clare's approval. The package wording and prices supplied for this project have been implemented; no unsupported claims or additional prices have been invented.

## Scope completed

### Design system and visual polish

- Refined the dark colour system around charcoal, navy, emerald and champagne tones so backgrounds, cards, borders, controls and highlights feel consistent across the site.
- Removed the scratch-like diagonal surface treatment that made pages look unfinished.
- Improved spacing, card balance, content widths, typography hierarchy and mobile stacking across shared sections and individual pages.
- Standardised buttons, links, focus states, form controls, FAQ cards, package cards, modals and navigation treatments.
- Added a branded custom scrollbar that follows the peacock colour palette without compromising normal scrolling behaviour.
- Reworked the FAQ search and topic controls so they read as deliberate custom controls rather than browser-default elements.
- Refined the Journal, package, footer, error and supporting content views to match the quality of the main landing pages.
- Replaced spinning logo behaviour with a restrained peacock-inspired glow and sheen treatment.
- Retained decorative feather artwork in static, controlled positions where it supports the composition without competing with the content.

### Motion and page-load experience

- Introduced shared motion timings and easing values so page elements move consistently.
- Simplified content reveals into quick opacity-led transitions that avoid blur, jitter and layout movement.
- Removed the large hero-copy and image-arrival transforms that could briefly clip letters during first paint, so important headings and calls to action render clearly from the start.
- Reduced hover lift and large movement effects so cards and controls feel responsive without appearing unstable.
- Smoothed carousel timing and deferred inactive carousel images to reduce unnecessary work during initial page load.
- Confined the remaining full-width carousel motion to isolated image surfaces, preventing a moving hero from disturbing the fixed header or page text.
- Kept the homepage and Wedding hero transitions where they are visually stable, while making the Services hero image deliberately static; the lower Services carousel retains useful visual movement without destabilising the first screen.
- Added reduced-motion handling so visitors who request less animation receive an appropriately calm experience.
- Removed continuous decorative animation where it consumed resources or reduced text clarity.

### Navigation, controls and accessibility

- Added and refined the keyboard-accessible skip link and visible focus treatments.
- Improved navigation state labelling, including the current-page state and mobile menu controls.
- Aligned the JavaScript mobile navigation breakpoint with the CSS breakpoint at 1,240 pixels, preventing the menu from entering an inconsistent state between tablet and desktop widths.
- Ensured opening the enquiry modal from the mobile menu closes the menu and updates its expanded state.
- Corrected modal focus restoration so keyboard focus returns to the visible mobile menu button rather than a hidden navigation control.
- Improved enquiry consent, validation messaging, error links and file-input validation.
- Improved keyboard operation and state announcements for selectors, dates, calendar controls and FAQ filters.
- Added focus containment and restoration to the image lightbox.
- Added carousel pause controls, useful live status, inert handling for inactive content and improved reduced-motion behaviour.
- Increased touch-target reliability and improved responsive layout behaviour on narrow screens.

These changes materially improve accessibility and keyboard usability, but the checks completed here are not a formal WCAG compliance certification. A formal certification would require a dedicated manual and assistive-technology audit in the deployed production environment.

### Performance and asset optimisation

- Added compressed, correctly sized WebP variants for logos, wreaths, feather decorations, accreditation badges and the network badge while retaining source originals.
- Added appropriately sized favicon and Apple touch-icon assets.
- Self-hosted the two brand typefaces, preloaded their compact WOFF2 files and retained the Open Font Licence alongside them, removing the page's dependency on Google Fonts at runtime.
- Replaced oversized image references with optimised responsive assets where appropriate.
- Deferred non-critical and inactive imagery to reduce the initial request cost.
- Enabled response compression and public static-asset caching.
- Removed the continuously rendered atmospheric canvas and its JavaScript renderer.
- Reduced the measured cold page load from approximately **7.8 MB across 19 resources** to approximately **1.18 MB across 14 resources** — an estimated **84.8% reduction** in transferred page weight during the measured local-browser test.

The remaining largest requests are the primary hero photographs. They have been kept at a quality level suitable for their prominent full-width use.

### Content, services and on-site SEO

- Replaced visitor-facing funeral terminology with positive **Celebration of Life** wording throughout navigation, page titles, headings, calls to action, package content and supporting body copy.
- Preserved necessary internal technical identifiers, legacy redirects, filenames and third-party URLs where changing them could break routing, assets or external links. These identifiers are not presented as the primary wording to visitors.
- Retained the legacy `/funerals` route as a compatibility path while using `/celebrations-of-life` as the visitor-facing canonical service route.
- Added and integrated Naming Ceremonies and Vow Renewals as site services.
- Integrated the supplied Wedding Ceremony package wording and confirmed public pricing.
- Added the celebrant-versus-registrar explanation and supplied supporting imagery.
- Integrated the supplied Official Network Member artwork in an appropriate responsive position.
- Corrected inconsistent logo use on the FAQ, Privacy, confirmation and error treatments, replacing lone wreath or white-background versions with the site's finished layered CLC mark.
- Updated on-site titles, headings and descriptive content to support the intended **weddings and celebrations of life in Durham** positioning.
- Implemented the supplied public prices for Wedding, Naming Ceremony, Vow Renewal and Celebration of Life packages, including the stated travel rate and concise venue-farewell guide range. No `Price to be confirmed` placeholder remains in the published package content.

On-site SEO changes can guide search engines, but they do not update Google's displayed result immediately. After the finished site is deployed, its sitemap and affected pages should be submitted or re-requested through Google Search Console. Google controls when the pages are crawled and when its search result wording changes.

## Second-pass defects found and corrected

The site was reviewed again after the first polish pass. That second pass identified implementation details that looked acceptable in source but produced visible defects in a real browser:

1. **Cross-document view transitions caused compositing defects.** The experimental page-transition layer was removed because it could leave stale or clipped fragments of the previous page during navigation.
2. **A body-wide entrance fade delayed the whole interface.** It was removed so the page shell and useful content render immediately while smaller component-level reveals retain the intended sense of arrival.
3. **Broad header transforms and property transitions clipped text.** The header was limited to controlled colour and shadow changes, removing movement and overly broad transition declarations.
4. **The atmospheric feather canvas obscured real content.** Browser screenshots confirmed that animated feathers were physically passing over headings and navigation. The canvas element, renderer and associated styles were retired. Static feather artwork and the bespoke logo treatment preserve the theme without obstructing text or consuming continuous rendering time.
5. **The navigation breakpoint differed between CSS and JavaScript.** Both now use 1,240 pixels, eliminating awkward intermediate-width behaviour.
6. **Modal focus could return to a hidden desktop control on mobile.** Focus now returns to the visible menu trigger, and opening the modal also closes the mobile menu cleanly.
7. **The carousel reduced-motion listener had drifted into the date-picker scope.** A final clean-console browser pass exposed the runtime error even though the JavaScript syntax check passed. The listener now sits inside the carousel controller where its pause state and timers exist.
8. **Three unfinished text documents were publicly reachable.** Unlinked questionnaire, booking-form and contract placeholders were removed from the public static directory so visitors cannot discover unfinished resources while the final approved PDFs are unavailable.
9. **First-screen text could be damaged during GPU composition.** Large hero-copy and image-arrival animations were removed after timed screenshots caught briefly clipped letterforms. Headings now render immediately and remain stable at first paint.
10. **Full-width moving media could interfere with the fixed header.** Paint containment is applied only to the moving carousel tracks that need it. The stable Services hero no longer runs a top-of-page crossfade, while retained homepage and Wedding transitions were rechecked during their active state.
11. **Brand fonts depended on an external stylesheet.** Cormorant Garamond and Manrope are now served locally, preloaded and covered by the included licence file, reducing external failure points and avoiding a late font swap.
12. **Supporting pages still used an unfinished standalone wreath treatment.** Privacy, thank-you and error views now use the same finished layered CLC logo treatment as the FAQ and primary brand areas.

These corrections are important because they address the roughness seen during actual use rather than only changing surface styling.

## Verification completed

The following checks were run against the final local project state:

| Check | Result |
| --- | --- |
| CSS compilation (`npm run build:css`) | Passed |
| JavaScript syntax (`node --check src/main/resources/static/js/site.js`) | Passed |
| Maven test suite (`.\\mvnw.cmd -q test`) | Passed: 7 suites, 48 tests, 0 failures, 0 errors and 0 skipped |
| Git whitespace check (`git diff --check`) | Passed; only normal Windows line-ending notices were reported |
| Browser console warning/error review | No relevant warning or error was present in the checked final journey |
| Desktop and narrow-mobile rendering | Reviewed with captured browser screenshots |
| Static delivery | Gzip compression, seven-day public caching and local brand-font delivery verified locally |

Public-route checks returned successful pages for:

- `/`
- `/services`
- `/weddings`
- `/celebrations-of-life`
- `/faq`
- `/reviews`
- `/blog`
- `/privacy`
- `/thank-you`

The legacy `/contact` path redirects to the home-page enquiry journey, the retired `/funerals` and `/ceremonies` paths use permanent redirects to their current canonical services, and a deliberately invalid path renders the styled 404 page.

## Browser evidence

The comparison captures are stored in the project so the condition before and after finalisation can be reviewed directly.

### Baseline captures

- `output/playwright/site-finalisation-baseline/10-home-top.png`
- `output/playwright/site-finalisation-baseline/11-home-mid.png`
- `output/playwright/site-finalisation-baseline/20-home-mobile-top.png`
- `output/playwright/site-finalisation-baseline/21-home-mobile-mid.png`
- `output/playwright/site-finalisation-baseline/25-mobile-navigation-open.png`
- `output/playwright/site-finalisation-baseline/26-enquiry-modal-mobile.png`

### Accepted final desktop captures

- `output/playwright/site-finalisation-final/accepted/01-home-top.png`
- `output/playwright/site-finalisation-final/accepted/02-services-top.png`
- `output/playwright/site-finalisation-final/accepted/03-weddings-top.png`
- `output/playwright/site-finalisation-final/accepted/04-celebrations-top.png`
- `output/playwright/site-finalisation-final/accepted/05-faq-top.png`
- `output/playwright/site-finalisation-final/accepted/06-reviews-top.png`
- `output/playwright/site-finalisation-final/accepted/07-about-top.png`
- `output/playwright/site-finalisation-final/accepted/08-blog-top.png`
- `output/playwright/site-finalisation-final/accepted/09-privacy-top.png`
- `output/playwright/site-finalisation-final/accepted/10-not-found.png`

### Accepted final mobile and interaction captures

- `output/playwright/site-finalisation-final/accepted/20-home-mobile-top.png`
- `output/playwright/site-finalisation-final/accepted/21-services-mobile-top.png`
- `output/playwright/site-finalisation-final/accepted/22-weddings-mobile-top.png`
- `output/playwright/site-finalisation-final/accepted/23-celebrations-mobile-top.png`
- `output/playwright/site-finalisation-final/accepted/24-faq-mobile-top.png`
- `output/playwright/site-finalisation-final/accepted/25-faq-mobile-filtered.png`
- `output/playwright/site-finalisation-final/accepted/26-mobile-navigation-open.png`
- `output/playwright/site-finalisation-final/accepted/27-enquiry-modal-mobile.png`

Matched before-and-after home-page comparisons are also stored as `comparison-baseline-desktop-1280x720.png`, `comparison-baseline-mobile-380x720.png` and `comparison-final-mobile-380x720.png` so the visible viewport can be compared without browser-capture scaling differences.

Screenshots demonstrate the tested viewport states but do not replace full device, browser, accessibility or production monitoring.

## Decisions still required from Clare

The supplied wording and pricing are implemented. These final publication decisions still need Clare's instruction:

1. Give final client sign-off to the implemented package wording and supplied prices.
2. Confirm that the celebrant-versus-registrar wording and supplied comparison artwork can be published as presented.
3. Supply the approved questionnaire, booking-form and contract PDFs if these documents should later be offered as website downloads.
4. Confirm the preferred date for deployment and subsequent Google Search Console re-index requests.

Until that publication sign-off is supplied, the local build is a polished, fully priced review version and has intentionally not been released.

## Release status

- The changes have **not** been committed.
- The changes have **not** been pushed.
- The changes have **not** been deployed.
- No production or external service state has been changed.

This is deliberate: the working tree contains the completed local finalisation work and still awaits Clare's final publication sign-off plus an explicit release instruction.
