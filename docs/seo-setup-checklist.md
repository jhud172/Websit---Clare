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

## Manual steps still needed

1. Confirm the final production domain is `https://www.clarebruntonlifeceremonies.com`.
2. Set `SITE_BASE_URL` to the production domain. `robots.txt` and `sitemap.xml` are generated dynamically.
3. Create or log in to Google Search Console.
4. Add the website property.
5. Verify the domain using DNS TXT verification.
6. Submit the sitemap URL:

   [https://www.clarebruntonlifeceremonies.com/sitemap.xml](https://www.clarebruntonlifeceremonies.com/sitemap.xml)

7. Use URL Inspection to request indexing for the homepage.
8. Set up or update the Google Business Profile if the business serves a local area.
9. Wait for Google to crawl and index the site. This can take days or weeks.

## Notes

Searching the exact business name should be easier than ranking for broad search terms. Ranking for wider terms depends on competition, content quality, backlinks, reviews, business profile strength, and ongoing SEO.

The `/contact` URL currently redirects to the homepage because enquiries use the site-wide modal, so it is not listed in the sitemap as a standalone indexable page.
