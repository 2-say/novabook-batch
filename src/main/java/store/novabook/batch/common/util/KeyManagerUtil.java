package store.novabook.batch.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import store.novabook.batch.common.dto.DatabaseConfigDto;

public class KeyManagerUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger log = LoggerFactory.getLogger(KeyManagerUtil.class);

	private KeyManagerUtil() {
	}

	public static DataSource getDataSource(Environment environment, String keyId) {

		String appkey = environment.getProperty("nhn.cloud.keyManager.appkey");
		String userId = environment.getProperty("nhn.cloud.keyManager.userAccessKeyId");
		String secretKey = environment.getProperty("nhn.cloud.keyManager.secretAccessKey");
		RestTemplate restTemplate = new RestTemplate();
		String baseUrl =
			"https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/" + appkey + "/secrets/" + keyId;
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-TC-AUTHENTICATION-ID", userId);
		headers.set("X-TC-AUTHENTICATION-SECRET", secretKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			InetAddress localhost = InetAddress.getLocalHost();
			log.info("local host: {}", localhost.getHostAddress());
			log.info("local ip: {}", localhost.getHostName());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		log.info("baseurl: {}", baseUrl);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity,
			new ParameterizedTypeReference<Map<String, Object>>() {
			});
		log.info("response: {}",response);


		DatabaseConfigDto config = null;
		if (response.getStatusCode().is2xxSuccessful()) {
			Map<String, Object> responseBody = response.getBody();
			if (responseBody != null) {
				Optional<Map<String, String>> bodyOptional = Optional.ofNullable(
					(Map<String, String>)responseBody.get("body"));
				if (bodyOptional.isPresent()) {
					Map<String, String> body = bodyOptional.get();

					// body 안의 secret 값을 JSON 문자열로 추출
					String secretJson = body.get("secret");

					// JSON 문자열을 DTO로 변환
					try {
						config = objectMapper.readValue(secretJson, DatabaseConfigDto.class);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new RuntimeException("Response body does not contain 'body' key or is null");
				}
			} else {
				throw new RuntimeException("Response body is null");
			}
		} else {
			throw new RuntimeException("API call failed with status code: " + response.getStatusCode());
		}

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name"));
		dataSource.setUrl(config.url());
		dataSource.setUsername(config.username());
		dataSource.setPassword(config.password());
		dataSource.setInitialSize(
			Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.datasource.dbcp2.initial-size"))));
		dataSource.setMaxIdle(
			Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.datasource.dbcp2.max-idle"))));
		dataSource.setMinIdle(
			Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.datasource.dbcp2.min-idle"))));
		dataSource.setValidationQuery(environment.getProperty("spring.datasource.dbcp2.validation-query"));
		dataSource.setDefaultAutoCommit(
			Boolean.parseBoolean(environment.getProperty("spring.datasource.dbcp2.default-auto-commit")));
		return dataSource;
	}

}
