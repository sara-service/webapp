package bwfdm.sara.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api")
public class Misc {
	@GetMapping("return-url")
	public String getReturnURL() {
		return Config.GITLAB;
	}

	@GetMapping("ir-meta")
	public IRMeta getIRMetadata() {
		return new IRMeta("https://kops.uni-konstanz.de/", "kops.svg");
	}

	private class IRMeta {
		@JsonProperty
		private final String url;
		@JsonProperty
		private final String logo;

		public IRMeta(final String url, final String logo) {
			this.url = url;
			this.logo = logo;
		}
	}
}
