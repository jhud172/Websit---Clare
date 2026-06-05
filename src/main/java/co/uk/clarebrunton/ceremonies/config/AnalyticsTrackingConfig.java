package co.uk.clarebrunton.ceremonies.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.uk.clarebrunton.ceremonies.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class AnalyticsTrackingConfig implements WebMvcConfigurer {

	private final AnalyticsService analyticsService;

	public AnalyticsTrackingConfig(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new PublicVisitInterceptor(analyticsService))
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/reviews/admin/**",
						"/review-photos/**",
						"/css/**",
						"/js/**",
						"/images/**",
						"/documents/**",
						"/favicon.*",
						"/error"
				);
	}

	private static class PublicVisitInterceptor implements HandlerInterceptor {

		private final AnalyticsService analyticsService;

		PublicVisitInterceptor(AnalyticsService analyticsService) {
			this.analyticsService = analyticsService;
		}

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
			if ("GET".equalsIgnoreCase(request.getMethod()) && !hasFileExtension(request.getRequestURI())) {
				analyticsService.recordVisit();
			}
			return true;
		}

		private boolean hasFileExtension(String requestUri) {
			int lastSlash = requestUri.lastIndexOf('/');
			int lastDot = requestUri.lastIndexOf('.');
			return lastDot > lastSlash;
		}

	}

}
