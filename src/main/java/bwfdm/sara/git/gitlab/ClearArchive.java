package bwfdm.sara.git.gitlab;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;

import bwfdm.sara.utils.UrlEncode;

public class ClearArchive {
	// these are deliberately hardcoded. makes it harder to accidentally clear
	// the wrong archive...
	private static final String API_URL = "https://testarchiv.unikn.netfuture.ch/api/v4";
	// this actually has to be a root token so it can clear the archive, not
	// just the temp-archive!
	private static final String API_TOKEN = "xxx";

	private static final Log logger = LogFactory.getLog(ClearArchive.class);

	private final PrivateTokenREST authRest;
	private final RESTHelper rest;

	public ClearArchive() {
		authRest = new PrivateTokenREST(API_URL, API_TOKEN);
		rest = new RESTHelper(authRest, "/projects");
	}

	private void delete(final String filter) {
		final List<GLProjectInfo> projects = rest.getList(
				rest.uri("").queryParam("simple", "true").queryParam("search",
						filter),
				new ParameterizedTypeReference<List<GLProjectInfo>>() {
				});
		for (final GLProjectInfo i : projects) {
			logger.info("say goodbye to " + i.path + " (" + i.title + ")");
			rest.delete(rest.uri("/" + UrlEncode.encodePathSegment(i.path)));
		}
	}

	public static void main(final String... args) {
		if (args.length == 0) {
			logger.fatal("Usage: " + ClearArchive.class.getSimpleName()
					+ " filter...");
			logger.fatal(
					"all projects matching filter (a gitlab search) will be deleted!");
			System.exit(1);
		}

		final ClearArchive deletor = new ClearArchive();
		for (final String arg : args)
			deletor.delete(arg);
		logger.info("archive at " + API_URL + " was successfully vandalized!");
	}
}
