package co.uk.clarebrunton.ceremonies.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SiteErrorController implements ErrorController {

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		HttpStatus status = resolveStatus(request);
		String title = status == HttpStatus.NOT_FOUND ? "Page not found" : "Something went wrong";
		String description = status == HttpStatus.NOT_FOUND
				? "The page you were looking for could not be found."
				: "An unexpected error has occurred.";

		model.addAttribute("status", status.value());
		model.addAttribute("pageTitle", title);
		model.addAttribute("pageDescription", description);
		model.addAttribute("robotsContent", "noindex, nofollow");
		return "error";
	}

	private HttpStatus resolveStatus(HttpServletRequest request) {
		Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (statusCode instanceof Integer code) {
			HttpStatus status = HttpStatus.resolve(code);
			if (status != null) {
				return status;
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

}
