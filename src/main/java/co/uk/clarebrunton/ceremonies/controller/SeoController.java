package co.uk.clarebrunton.ceremonies.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.service.SiteUrlResolver;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class SeoController {

	private static final List<String> SITEMAP_PATHS = List.of(
			"/",
			"/about",
			"/services",
			"/weddings",
			"/celebrations-of-life",
			"/faq",
			"/reviews",
			"/privacy",
			"/blog",
			"/blog/creating-a-wedding-ceremony-that-still-feels-like-you",
			"/blog/how-to-shape-a-celebration-of-life-tribute",
			"/blog/questions-to-ask-before-booking-a-celebrant"
	);

	private final SiteProperties siteProperties;
	private final SiteUrlResolver siteUrlResolver;

	public SeoController(SiteProperties siteProperties, SiteUrlResolver siteUrlResolver) {
		this.siteProperties = siteProperties;
		this.siteUrlResolver = siteUrlResolver;
	}

	@GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	public String robots(HttpServletRequest request) {
		String baseUrl = siteUrlResolver.resolvePublicBaseUrl(request, siteProperties.getBaseUrl());
		return """
				User-agent: *
				Allow: /

				Sitemap: %s/sitemap.xml
				""".formatted(baseUrl);
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String sitemap(HttpServletRequest request) {
		String baseUrl = siteUrlResolver.resolvePublicBaseUrl(request, siteProperties.getBaseUrl());
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
		for (String path : SITEMAP_PATHS) {
			xml.append("  <url>\n");
			xml.append("    <loc>").append(baseUrl).append(path).append("</loc>\n");
			xml.append("    <changefreq>monthly</changefreq>\n");
			xml.append("    <priority>").append(priorityFor(path)).append("</priority>\n");
			xml.append("  </url>\n");
		}
		xml.append("</urlset>\n");
		return xml.toString();
	}

	private String priorityFor(String path) {
		if ("/".equals(path)) {
			return "1.0";
		}
		if ("/privacy".equals(path)) {
			return "0.3";
		}
		if (path.startsWith("/blog/")) {
			return "0.5";
		}
		if ("/blog".equals(path)) {
			return "0.6";
		}
		if ("/reviews".equals(path)) {
			return "0.7";
		}
		if ("/about".equals(path)) {
			return "0.8";
		}
		return "0.9";
	}

}
