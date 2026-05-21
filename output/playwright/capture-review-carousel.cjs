const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 2048, height: 930 } });
  await page.goto('http://localhost:8080/', { waitUntil: 'networkidle' });
  const carousel = page.locator('.review-carousel');
  await carousel.scrollIntoViewIfNeeded();
  await page.waitForTimeout(500);
  await carousel.screenshot({ path: 'output/playwright/review-carousel-upgrade-section.png' });
  await browser.close();
})();
