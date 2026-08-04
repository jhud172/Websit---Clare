# SEO Setup Checklist

## Completed in code

- Added page titles
- Added meta descriptions
- Added canonical URLs
- Added Open Graph metadata
- Added Twitter/X card metadata
- Added dynamic robots.txt endpoint
- Added dynamic sitemap.xml endpoint
- Added host-mismatch protection so canonical/share URLs use the live request host when SITE_BASE_URL is stale
- Added ProfessionalService structured data using business details already present in the site
- Set the homepage search title to `Weddings and Celebrations of Life in Durham | Clare's Life Celebrations`
- Added the FAQ page to the canonical sitemap
- Kept former service and journal URLs as permanent redirects rather than indexable pages
- Confirmed the canonical production domain is `https://clareslifecelebrations.com`

## Manual steps still needed

1. Reconcile the local work with `origin/main`, then deploy the tested build to production. Do not request indexing before the new content is live.
2. Confirm the homepage source contains the new title, description, Open Graph title, Twitter title and prominent wording.
3. Confirm `/celebrations-of-life` returns `200` and `/funerals` permanently redirects to it.
4. Confirm `robots.txt` allows crawling and the generated sitemap contains only current canonical pages.
5. Create or log in to Google Search Console and add the domain property.
6. Verify the domain using the DNS TXT method unless an existing verified property is already available.
7. Submit or resubmit the sitemap URL:

   [https://clareslifecelebrations.com/sitemap.xml](https://clareslifecelebrations.com/sitemap.xml)

8. Use URL Inspection for the homepage and `/celebrations-of-life`, run **Test live URL**, then select **Request indexing**.
9. Update the Google Business Profile and any directory profiles that still use the previous service wording.
10. Monitor Search Console for indexing, redirect or canonical warnings. Google may take several days or weeks to recrawl and may choose to rewrite a title link.

## Notes

Searching the exact business name should be easier than ranking for broad search terms. Ranking for wider terms depends on competition, content quality, backlinks, reviews, business profile strength, and ongoing SEO.

The desired homepage title is `Weddings and Celebrations of Life in Durham | Clare's Life Celebrations`. The code aligns the title, social metadata, structured data and prominent page wording around that phrase, but no code change can force Google to update immediately or use an exact title.

The `/contact` URL currently redirects to the homepage because enquiries use the site-wide modal, so it is not listed in the sitemap as a standalone indexable page.
