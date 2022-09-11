package dev.projectcoda.gateway.security;

import dev.projectcoda.gateway.conf.GatewayConfiguration;
import dev.projectcoda.gateway.util.RecaptchaUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A service that validates Google reCAPTCHAs.
 * @author Gerard Sayson
 */
public class CaptchaChecker {

	private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
	private final String recaptchaSecret;
	private final RestTemplateBuilder restTemplateBuilder;

	public CaptchaChecker(RestTemplateBuilder restTemplateBuilder, GatewayConfiguration configuration) {
		this.restTemplateBuilder = restTemplateBuilder;
		this.recaptchaSecret = configuration.getRecaptchaSecret();
	}

	@SuppressWarnings("unchecked")
	public String verifyRecaptcha(String recaptchaResponse) {
		Map<String, String> body = new HashMap<>();
		body.put("secret", recaptchaSecret);
		body.put("response", recaptchaResponse);
		@SuppressWarnings("rawtypes") ResponseEntity<Map> recaptchaResponseEntity = restTemplateBuilder.build().postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL + "?secret={secret}&response={response}", body, Map.class, body);
		Map<String, Object> responseBody = recaptchaResponseEntity.getBody();
		boolean recaptchaSuccess = (Boolean) Objects.requireNonNull(responseBody).get("success");
		if(!recaptchaSuccess) {
			List<String> errorCodes = (List<String>) responseBody.get("error-codes");
			return errorCodes.stream()
					.map(RecaptchaUtils.RECAPTCHA_ERROR_CODE::get)
					.collect(Collectors.joining(", "));
		} else {
			return "";
		}
	}
}
