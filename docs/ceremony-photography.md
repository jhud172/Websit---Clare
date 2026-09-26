# Clare's ceremony photographs and film

All seven supplied JPEGs are included unchanged (about 827 KiB combined). Their modest original dimensions suit supporting photography, avoiding full-width hero enlargement.

| Supplied photo | Subject and placement | Asset |
| --- | --- | --- |
| 1 | Couple and Clare beneath a floral arch; wedding gallery | `images/weddings/floral-arch/floral-arch-wedding-couple-with-clare.jpg` |
| 2 | Handfasting ribbons and flowers; wedding gallery detail | `images/weddings/floral-arch/floral-arch-wedding-handfasting-ribbons.jpg` |
| 3 | Clare leading a ceremony; landscape gallery image | `images/weddings/floral-arch/floral-arch-wedding-ceremony.jpg` |
| 4 | Woodland couple with Clare; leads the new wedding gallery | `images/weddings/woodland/woodland-wedding-couple-with-clare.jpg` |
| 5 | Couple sharing a kiss; wedding gallery | `images/weddings/woodland/woodland-wedding-first-kiss.jpg` |
| 6 | Clare with the groom before the ceremony; wedding gallery | `images/weddings/woodland/woodland-wedding-preparations.jpg` |
| 7 | Clare's portrait; homepage introduction and first About carousel slide | `images/clare/clare-brunton-portrait.jpg` |

The wedding gallery at `/weddings#ceremony-moments` preserves full image proportions and reuses the site's image lightbox, including Escape, keyboard focus containment and focus return. Existing wedding carousels remain.

These images are not assigned to individual reviews: no reviewer names or review associations were supplied.

## Woodland wedding film

The supplied `BDDBAFF8-4656-469D-9A1C-B5E99F8A03B5.mov` shows Clare leading the woodland ceremony pictured in Photos 4–6. Its web version is `src/main/resources/static/videos/weddings/woodland/woodland-wedding-ceremony.mp4`. A still from two seconds into that clip is stored at `src/main/resources/static/images/weddings/woodland/woodland-wedding-ceremony-poster.jpg`.

The 5.1-second, 512 × 910 H.264 clip has no audio track. It was remuxed losslessly into MP4 with fast-start enabled; no upscaling or cropping was applied, and the visible creator credits remain. The original MOV attachment was left unchanged. The public player includes a short description and a no-sound label.

Related files share the `woodland-wedding-` prefix and `woodland` subfolder. Photos 1–3 use `floral-arch-wedding-` in the `floral-arch` subfolder. Clare's portrait is named `clare-brunton-portrait.jpg`. All paths in this document are relative to `src/main/resources/static` unless stated otherwise.

See [the video instructions](../src/main/resources/static/videos/README.md) for replacement, caption and rebuild steps.

## Verification

- `npm run build:css` and `node --check src/main/resources/static/js/site.js` passed.
- `./mvnw test` passed: 38 tests, zero failures or errors.
- Browser checks passed for gallery layout at 1440, 768 and 390 pixels, all six photographs loading, and the new portrait on Home and About at desktop and phone widths.
- Image enlargement, Tab focus containment, Escape dismissal, focus restoration and reduced-motion styling were checked.
- Actual clip playback passed in Chromium at desktop, tablet and phone widths; native video reports 5.1 seconds at 512 × 910 with no media error. Seeking to three seconds passed, and layouts have no horizontal overflow.
- The server returns `206 Partial Content` and `video/mp4` for byte-range requests. MP4 inspection confirms the `moov` atom precedes `mdat` for fast-start delivery. FFmpeg decoded the full output without errors.
- All seven renamed photographs were checked against their original SHA-256 hashes and loaded successfully on the Weddings, Home and About pages. No old asset paths remain in source templates or documentation.
- The refreshed local preview runs at `http://localhost:8082/weddings#ceremony-film` because port 8081 was occupied.
- Local preview used an isolated in-memory database. No production deployment was performed.
