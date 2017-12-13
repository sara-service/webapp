package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
//import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;


import bwfdm.sara.publication.db.EPersonDAO;

import bwfdm.sara.publication.db.ItemDAO;
import bwfdm.sara.publication.db.ItemType;
import bwfdm.sara.publication.db.ItemState;

import bwfdm.sara.publication.db.SourceDAO;
import bwfdm.sara.publication.db.ArchiveDAO;
import bwfdm.sara.publication.db.RepositoryDAO;

import bwfdm.sara.publication.db.CollectionDAO;
import bwfdm.sara.publication.db.MetadataDAO;

import bwfdm.sara.publication.PublicationRepositoryFactory;
import bwfdm.sara.publication.PublicationRepository;

/**
 * Database containing config stuff for the publication backend.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class PublicationDatabase {
	
	private static final String PERSON_TABLE = "public.eperson";
	private static final String ITEM_TABLE = "public.item";
	private static final String ARCHIVE_TABLE = "public.archive";
	private static final String REPOSITORY_TABLE = "public.repository";
	private static final String REPOSITORY_PARAM_TABLE = "public.repository_params";
	private static final String SOURCE_TABLE = "public.source";
	private static final String COLLECTION_TABLE = "public.collection";
	private static final String METADATA_TABLE = "public.metadata";
	
	private final JdbcTemplate db;
	private final DataSource ds;

	/**
	 * Creates a DAO for reading config values.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 */
	public PublicationDatabase(final DataSource db) {
		this.ds = db;
		this.db = new JdbcTemplate(db);
	}
	
	public final DataSource getDataSource() { return this.ds; }
	
	
	
	private static final String PERSON_FIELDS = "uuid, contact_email, password, last_active";
	
	private static final RowMapper<EPersonDAO> PERSON_MAPPER = new RowMapper<EPersonDAO>() {
		@Override
		public EPersonDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID uuid = (UUID) rs.getObject("uuid");
			
			final String contact_email = rs.getString("contact_email");
			final String password = rs.getString("password");
			final Date last_active = rs.getTimestamp("last_active");
			return new EPersonDAO(uuid,contact_email,password,last_active);
		}
	};
	
	public List<EPersonDAO> getPersonList() {
		return db.query("select " + PERSON_FIELDS + " from " + PERSON_TABLE, PERSON_MAPPER);
	}
	
	private static final String SOURCE_FIELDS = "uuid, display_name, url, adapter, enabled";

	private static final RowMapper<SourceDAO> SOURCE_MAPPER = new RowMapper<SourceDAO>() {
		@Override
		public SourceDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final String n = rs.getString("display_name");
			final String u = rs.getString("url");
			final String a = rs.getString("adapter");
			final Boolean e = rs.getBoolean("enabled");
			
			return new SourceDAO(id, n, u, a, e);
		}
	};
	
	public List<SourceDAO> getSourceList() {
		return db.query("select " + SOURCE_FIELDS + " from " + SOURCE_TABLE, SOURCE_MAPPER);
	}
	
	private static String ARCHIVE_FIELDS = "uuid, display_name, url, adapter, enabled";
	
	private static final RowMapper<ArchiveDAO> ARCHIVE_MAPPER = new RowMapper<ArchiveDAO>() {
		@Override
		public ArchiveDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final String n = rs.getString("display_name");
			final String u = rs.getString("url");
			final String a = rs.getString("adapter");
			final Boolean e = rs.getBoolean("enabled");
			
			return new ArchiveDAO(id, n, u, a, e);
		}
	};
	
	public List<ArchiveDAO> getArchiveList() {
		return db.query("select " + ARCHIVE_FIELDS + " from " + ARCHIVE_TABLE, ARCHIVE_MAPPER);
	}
	
	private static final String ITEM_FIELDS = 
			"uuid, eperson_uuid, source_uuid, archive_uuid, repository_uuid, foreign_uuid, "
			+ "item_type, item_state, date_created, date_last_modified, in_archive, citation_handle";
	
	private static final RowMapper<ItemDAO> ITEM_MAPPER = new RowMapper<ItemDAO>() {
		@Override
		public ItemDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final UUID pRef = (UUID) rs.getObject("eperson_uuid");
			final UUID sRef = (UUID)rs.getObject("source_uuid");
			final UUID aRef = (UUID)rs.getObject("archive_uuid");
			final UUID rRef = (UUID)rs.getObject("repository_uuid");
			final String fRef = rs.getString("foreign_uuid");
			final ItemType t = ItemType.valueOf(rs.getString("item_type"));
			final ItemState s = ItemState.valueOf(rs.getString("item_state"));
			final Date crDate = rs.getTimestamp("date_created");
			final Date lmDate = rs.getTimestamp("date_last_modified");
			final String citationHandle = rs.getString("citation_handle");
			
			return new ItemDAO(id,t,s,crDate,lmDate,pRef,sRef,aRef,rRef,fRef,citationHandle);
		}
	};

	public List<ItemDAO> getItemList() {
		return db.query("select " + ITEM_FIELDS + " from " + ITEM_TABLE, ITEM_MAPPER);
	}
	
	private static final String REPOSITORY_FIELDS = "uuid, display_name, url, contact_email, adapter, logo_base64, enabled ";
	
	private static final RowMapper<PublicationRepositoryFactory> REPOSITORY_FACTORY_MAPPER = new RowMapper<PublicationRepositoryFactory>() {
		@Override
		public PublicationRepositoryFactory mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final String n = rs.getString("display_name");
			final String u = rs.getString("url");
			final String m = rs.getString("contact_email");
		    final String l = rs.getString("logo_base64");
		    final String a = rs.getString("adapter");
		    final Boolean e = rs.getBoolean("enabled");
			return new PublicationRepositoryFactory(new RepositoryDAO(id,n,u,m,l,a,e));
		}
	};
	
	private Map<String, String> readArguments(final String table,
			final Object id) {
		final Map<String, String> args = new HashMap<>();
		db.query("select param, value from " + table + " where id = UUID(?)",
				new RowCallbackHandler() {
					@Override
					public void processRow(final ResultSet rs)
							throws SQLException {
						args.put(rs.getString("param"), rs.getString("value"));
					}
				}, id);
		return args;
	}
	
	public PublicationRepository newPubRepo(final String id) {
		final PublicationRepositoryFactory factory = db.queryForObject(
				"select * from " + REPOSITORY_TABLE
						+ " where uuid = UUID(?)", REPOSITORY_FACTORY_MAPPER, id);
		return factory.newPubRepo(readArguments(REPOSITORY_PARAM_TABLE, id));
	}
	
	public List<PublicationRepositoryFactory> getRepositoryFactoryList() {
		return db.query("select " + REPOSITORY_FIELDS + " from "
				+ REPOSITORY_TABLE, REPOSITORY_FACTORY_MAPPER);
	}
	
	
	private static String COLLECTION_FIELDS = "id, foreign_uuid, enabled";
	
	private static final RowMapper<CollectionDAO> COLLECTION_MAPPER = new RowMapper<CollectionDAO>() {
		@Override
		public CollectionDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("id");
			final String foreign_uuid = rs.getString("foreign_uuid");
			final Boolean enabled = rs.getBoolean("enabled");
			return new CollectionDAO(id,foreign_uuid,enabled);
		}
	};
	
	public List<CollectionDAO> getCollectionList(final Object id) {
		return db.query("select " + COLLECTION_FIELDS + " from " + COLLECTION_TABLE + " where id = UUID(?)", COLLECTION_MAPPER, id);
	}
	
	private static String METADATA_FIELDS = "id, display_name, map_from, map_to, enabled";
	
	
	private static final RowMapper<MetadataDAO> METADATA_MAPPER = new RowMapper<MetadataDAO>() {
		@Override
		public MetadataDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("id");
			final String display_name = rs.getString("display_name");
			final Boolean enabled = rs.getBoolean("enabled");
			final String map_from = rs.getString("map_from");
			final String map_to = rs.getString("map_to");
			return new MetadataDAO(id,display_name,map_from, map_to, enabled);
		}
	};
	
	public List<MetadataDAO> getMetadataList(final Object id) {
		return db.query("select " + METADATA_FIELDS + " from " + METADATA_TABLE + " where id = UUID(?)", METADATA_MAPPER, id);
	}
	
}
