package bwfdm.sara;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

import bwfdm.sara.extractor.licensee.LicenseeExtractor;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class })
public class Application extends SpringBootServletInitializer {
	/** Tomcat entry point. */
	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	/** Eclipse IDE entry point. */
	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * {@link LicenseeExtractor#getInstance()} does lazy-init so it doesn't slow
	 * down servlet startup, but that massively slows down the first license
	 * detection. Thus we just initialize it in the background at low priority
	 * so it will hopefully be ready once we first need it.
	 */
	@PostConstruct
	public static void initializeLicenseeInBackground() {
		final Thread bginit = new Thread("LicenseeExtractor background init") {
			@Override
			public void run() {
				LicenseeExtractor.getInstance();
			};
		};
		bginit.setPriority(Thread.MIN_PRIORITY);
		bginit.start();
	}
}
