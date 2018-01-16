package bwfdm.sara.publication;

/** 
 * @author sk
 */

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import bwfdm.sara.publication.db.RepositoryDAO;
import bwfdm.sara.publication.dspace.DSpace_RESTv5_SWORDv2;

@JsonIgnoreProperties(ignoreUnknown = false)
public class PublicationRepositoryFactory {
	// @SuppressWarnings("unchecked")
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, Class<? extends PublicationRepository>> ADAPTERS = new HashMap<>();

	static {
		ADAPTERS.put("DSpace_RESTv5_SWORDv2", DSpace_RESTv5_SWORDv2.class);
	}

	@JsonProperty
	public final RepositoryDAO dao;

	// @JsonProperty
	// private JsonNode args;

	public PublicationRepositoryFactory(final RepositoryDAO dao) {
		this.dao = dao;
	}

	/**
	 * Creates a new {@link PublicationRepository} instance.
	 * <p>
	 * Uses Jackson to construct the object from JSON. For this to work, the
	 * constructor must be annotated with {@link JsonCreator}, and all its arguments
	 * must have {@link JsonProperty} annotation giving the argument's name in the
	 * JSON, ie. something like {@code @JsonProperty("url")}.
	 * 
	 * @return a new {@link PublicationRepository}
	 */
	public PublicationRepository newPubRepo(final Map<String, String> args) {
		final Class<? extends PublicationRepository> cls = ADAPTERS.get(dao.adapter);
		args.put("uuid", dao.uuid.toString());
		args.put("url", dao.url);
		return MAPPER.convertValue(args, cls);
	}
}