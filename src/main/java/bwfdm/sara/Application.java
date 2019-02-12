package bwfdm.sara;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

import bwfdm.sara.extractor.licensee.LicenseeExtractor;

@SpringBootApplication
@Configuration
@ServletComponentScan // only needed for running in Eclipse
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class })
public class Application extends SpringBootServletInitializer {
	private static final Log logger = LogFactory.getLog(Application.class);

	/** Tomcat entry point. */
	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	/** Eclipse IDE entry point. */
	public static void main(final String[] args) throws IOException {
		// load email credentials from a separate file. these context params are
		// needed for development but cannot be put in the (public)
		// application.properties used for the other development context params.
		final Properties emailCred = new Properties();
		emailCred.load(new FileInputStream("email.properties"));

		final SpringApplication app = new SpringApplication(Application.class);
		app.setDefaultProperties(emailCred);
		app.run(args);
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
