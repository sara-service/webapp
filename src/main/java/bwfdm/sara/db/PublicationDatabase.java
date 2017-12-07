package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Date;
//import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import bwfdm.sara.publication.db.ArchiveDAO;
import bwfdm.sara.publication.db.RepositoryDAO;
import bwfdm.sara.publication.db.SourceDAO;

import bwfdm.sara.publication.db.ItemDAO;
import bwfdm.sara.publication.db.ItemType;
import bwfdm.sara.publication.db.ItemState;

/**
 * Database containing config stuff for the publication backend.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class PublicationDatabase {
	
	private static final String ITEM_TABLE = "public.item";
	private static final String ARCHIVE_TABLE = "public.archive";
	private static final String REPOSITORY_TABLE = "public.repository";
	private static final String SOURCE_TABLE = "public.source";
	
	private final JdbcTemplate db;

	/**
	 * Creates a DAO for reading config values.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 */
	public PublicationDatabase(final DataSource db) {
		this.db = new JdbcTemplate(db);
	}

	private static final String SOURCE_FIELDS = "uuid, name, URL, api_Endpoint, oauth_id, oauth_secret";

	private static final RowMapper<SourceDAO> SOURCE_MAPPER = new RowMapper<SourceDAO>() {
		@Override
		public SourceDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final String name = rs.getString("name");
			final String URL = rs.getString("URL");
			final String apie = rs.getString("api_Endpoint");
			final String i = rs.getString("oauth_id");
			final String s = rs.getString("oauth_secret");
			
			return new SourceDAO(id,name,URL,apie,i,s);
		}
	};
	
	public List<SourceDAO> getSourceList() {
		return db.query("select " + SOURCE_FIELDS + " from " + SOURCE_TABLE, SOURCE_MAPPER);
	}
	
	private static String ARCHIVE_FIELDS = "uuid, name, URL";
	
	private static final RowMapper<ArchiveDAO> ARCHIVE_MAPPER = new RowMapper<ArchiveDAO>() {
		@Override
		public ArchiveDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final String name = rs.getString("name");
			final String URL = rs.getString("URL");
			
			return new ArchiveDAO(id,name,URL);
		}
	};
	
	public List<ArchiveDAO> getArchiveList() {
		return db.query("select " + ARCHIVE_FIELDS + " from " + ARCHIVE_TABLE, ARCHIVE_MAPPER);
	}
	
	private static final String ITEM_FIELDS = 
			"uuid, submitter_uuid, archive_uuid, repository_uuid, fuuid, "
			+ "itemtype, itemstate, date_created, date_last_modified, citation_handle";
	
	private static final RowMapper<ItemDAO> ITEM_MAPPER = new RowMapper<ItemDAO>() {
		@Override
		public ItemDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final UUID sRef = (UUID)rs.getObject("submitter_uuid");
			final UUID aRef = (UUID)rs.getObject("archive_uuid");
			final UUID rRef = (UUID)rs.getObject("repository_uuid");
			final String fRef = rs.getString("fuuid");
			final ItemType t = ItemType.valueOf(rs.getString("itemtype"));
			final ItemState s = ItemState.valueOf(rs.getString("itemstate"));
			final Date crDate = rs.getTimestamp("date_created");
			final Date lmDate = rs.getTimestamp("date_last_modified");
			final String citationHandle = rs.getString("citation_handle");
			
			return new ItemDAO(id,t,s,crDate,lmDate,sRef,rRef,aRef,fRef,citationHandle);
		}
	};

	public List<ItemDAO> getItemList() {
		return db.query("select " + ITEM_FIELDS + " from " + ITEM_TABLE, ITEM_MAPPER);
	}
	
	private static final String REPOSITORY_FIELDS = 
			"uuid, name, URL, query_API_endpoint, query_user, query_pwd, submit_API_endpoint, "
			+ "submit_user, submit_pwd, contactemail, version, logo_base64, default_collection";
	
	private static final RowMapper<RepositoryDAO> REPOSITORY_MAPPER = new RowMapper<RepositoryDAO>() {
		@Override
		public RepositoryDAO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID id = (UUID) rs.getObject("uuid");
			final String name = rs.getString("name");
			final String URL = rs.getString("URL");
			final String query_API_endpoint = rs.getString("query_API_endpoint");
			final String query_user = rs.getString("query_user");
			final String query_pwd = rs.getString("query_pwd");
			final String submit_API_endpoint = rs.getString("submit_API_endpoint");
			final String submit_user = rs.getString("submit_user");
			final String submit_pwd = rs.getString("submit_pwd");
			final String contactEMail = rs.getString("contactemail");
		    final String version = rs.getString("version");
		    final String logo = rs.getString("logo_base64");
		    final String default_collection =rs.getString("default_collection");
		
		    return new RepositoryDAO(
		    	id,
				name, URL,
				query_API_endpoint, query_user, query_pwd,
				submit_API_endpoint, submit_user, submit_pwd,
				contactEMail,
				version, logo, default_collection
				);
			}
					
	};
	
	public List<RepositoryDAO> getRepositoryList() {
		return db.query("select " + REPOSITORY_FIELDS + " from "
				+ REPOSITORY_TABLE, REPOSITORY_MAPPER);
	}
}
