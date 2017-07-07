package bwfdm.sara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

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
}
