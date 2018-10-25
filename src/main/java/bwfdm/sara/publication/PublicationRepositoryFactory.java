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

import bwfdm.sara.publication.dspace.DSpace;

@JsonIgnoreProperties(ignoreUnknown = false)
public class PublicationRepositoryFactory {
	// @SuppressWarnings("unchecked")
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, Class<? extends PublicationRepository>> ADAPTERS = new HashMap<>();

	static {
		ADAPTERS.put("DSpace", DSpace.class);
	}

	@JsonProperty
	public final Repository dao;

	// @JsonProperty
	// private JsonNode args;

	public PublicationRepositoryFactory(final Repository dao) {
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
	public PublicationRepository newPublicationRepository(final Map<String, Object> args) {
		final Class<? extends PublicationRepository> cls = ADAPTERS.get(dao.adapter);
		return MAPPER.convertValue(args, cls);
	}
}