package io.github.bubalehich.sports.etl;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rakuten")
record RakutenProperties(String baseUrl, String applicationId, String accessKey, String keyword, int hitsPerPage,
		int pages) {
}
