package bwfdm.sara.api;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import bwfdm.sara.Config;
import bwfdm.sara.project.PublicationSession;
import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.ItemPublication;
import bwfdm.sara.publication.ItemState;
import bwfdm.sara.publication.MetadataMapping;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.PublicationRepository.SubmissionInfo;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.SaraMetaDataField;
import bwfdm.sara.publication.db.PublicationField;

@RestController
@RequestMapping("/api/publish")
public class Publication {
	private static final ISO8601DateFormat ISO8601 = new ISO8601DateFormat();
	private static final Log logger = LogFactory.getLog(Publication.class);
	@Autowired
	private Config config;

	@GetMapping("")
	public PubrepoConfig getPubRepoConfig(final HttpSession session) {
		return new PubrepoConfig(getMetadata(session), getPubRepos());
	}

	public class PubrepoConfig {
		@JsonProperty("meta")
		public final Map<PublicationField, String> metadata;
		@JsonProperty("repos")
		public final List<Repository> repos;

		public PubrepoConfig(Map<PublicationField, String> meta,
				List<Repository> repos) {
			this.metadata = meta;
			this.repos = repos;
		}
	}

	@GetMapping("repos")
	public List<Repository> getPubRepos() {
		return config.getPublicationDatabase().getList(Repository.class,
				"where enabled order by display_name asc");
	}

	@GetMapping("meta")
	public Map<PublicationField, String> getMetadata(
			final HttpSession session) {
		final Map<PublicationField, String> data = PublicationSession
				.getInstance(session).getMetadata();
		// make sure all keys exist (JavaScript needs this)
		for (PublicationField field : PublicationField.values())
			if (!data.containsKey(field))
				data.put(field, null);
		return data;
	}

	@PutMapping("meta")
	public void setMetadata(
			@RequestBody final Map<PublicationField, String> values,
			final HttpSession session) {
		PublicationSession.getInstance(session).setMetadata(values);
	}

	@GetMapping("getpubid")
	public String getPublicationID(final HttpSession session) {
		final PublicationSession project = PublicationSession
				.getInstance(session);
		return project.getPubID();
	}

	@GetMapping("finalMapping")
	private MultiValueMap<String, String> finalMapping(
			final HttpSession session) {
		final PublicationSession project = PublicationSession
				.getInstance(session);

		final Map<PublicationField, String> meta = project.getMetadata();

		// FIXME this is somewhat hacky
		final UUID repository_uuid = UUID
				.fromString(meta.get(PublicationField.PUBLICATION_REPOSITORY));

		MultiValueMap<String, String> finalMap = new LinkedMultiValueMap<String, String>();
		finalMap.putIfAbsent(SaraMetaDataField.ABSTRACT.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.DESCRIPTION)));
		finalMap.putIfAbsent(SaraMetaDataField.SUBMITTER.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.SUBMITTER)));
		finalMap.putIfAbsent(SaraMetaDataField.AUTHOR.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.SUBMITTER)));
		finalMap.putIfAbsent(SaraMetaDataField.TITLE.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.TITLE)));
		finalMap.putIfAbsent(SaraMetaDataField.VERSION.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.VERSION)));
		finalMap.putIfAbsent(SaraMetaDataField.TYPE.getDisplayName(),
				Arrays.asList("Git Archive"));
		finalMap.putIfAbsent(SaraMetaDataField.PUBLISHER.getDisplayName(),
				Arrays.asList("SARA SERVICE VERSION 'IOMI WORKSHOP'"));
		finalMap.putIfAbsent(SaraMetaDataField.ARCHIVE_URL.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.ARCHIVE_URL)));
		finalMap.putIfAbsent(SaraMetaDataField.ABSTRACT.getDisplayName(),
				Arrays.asList(meta.get(PublicationField.DESCRIPTION)));
		finalMap.putIfAbsent(SaraMetaDataField.DATE_ARCHIVED.getDisplayName(),
				Arrays.asList(ISO8601.format(project.getItem().date_created)));

		// configured mappings (repository specific)
		List<MetadataMapping> mms = project.getPublicationDatabase()
				.getList(MetadataMapping.class);

		// apply configured mappings
		for (final MetadataMapping mm : mms) {
			if (mm.repository_uuid.equals(repository_uuid)) {
				// remap iff the new key entry does not exist yet
				if (finalMap.containsKey(mm.map_from)) {
					if (!finalMap.containsKey(mm.map_to)) {
						// write new mapping
						finalMap.put(mm.map_to, finalMap.get(mm.map_from));
						// remove the original mapping
						finalMap.remove(mm.map_from);
					}
				}
			}
		}

		return finalMap;
	}

	@PostMapping("verify")
	public boolean doesCodeVerify(final HttpSession session,
			final String vcode) {
		final PublicationSession project = PublicationSession
				.getInstance(session);

		return project.verifyCode(vcode);
	}

	@GetMapping("sendVerification")
	public void sendEMailVerification(final HttpSession session) {
		final PublicationSession project = PublicationSession
				.getInstance(session);

		final Map<PublicationField, String> meta = project.getMetadata();

		final String userLogin = meta.get(PublicationField.PUBREPO_LOGIN_EMAIL);
		final String submitter = meta.get(PublicationField.SUBMITTER);
		final String pubid = project.getPubID();

		final SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom("SARA Service <noreply@sara-service.org>");
		mail.setTo(userLogin);
		mail.setSubject("Your submission <" + pubid + ">");
		mail.setText("Dear " + submitter
				+ "\n\n please acknowledge your submission to \""
				+ meta.get(PublicationField.PUBREPO_REPOSITORYNAME) + "\"\n"
				+ "using this code:\n\n"
				+ project.getVerificationCode() + "\n\n"
				+ "If you did not initiate this submission just do nothing!\n\n"
				+ "Sincerely yours\n SARA Service Bot");
		config.getEmailSender().send(mail);
	}

	@GetMapping("trigger")
	public RedirectView triggerPublication(final HttpSession session) {
		final PublicationSession project = PublicationSession
				.getInstance(session);

		if (!project.isVerified()) {
			return null;
		}

		final Map<PublicationField, String> meta = project.getMetadata();

		final UUID repository_uuid = UUID
				.fromString(meta.get(PublicationField.PUBLICATION_REPOSITORY));
		final PublicationRepository repo = project.getPublicationDatabase()
				.getPubRepo(repository_uuid);

		final String collectionURL = meta
				.get(PublicationField.PUBREPO_COLLECTION);
		final String userLogin = meta.get(PublicationField.PUBREPO_LOGIN_EMAIL);

		MultiValueMap<String, String> metadataMap = finalMapping(session);
		ItemPublication i = new ItemPublication();
		i.item_uuid = project.getItemUUID();
		i.repository_login_id = userLogin;

		i.date_created = i.date_last_modified = new Date();
		i.item_state = ItemState.SUBMITTED.name();
		i.repository_uuid = repository_uuid;
		i.collection_id = collectionURL;

		// TODO Error Handling
		final SubmissionInfo submissionInfo = repo.publishMetadata(userLogin,
				collectionURL, metadataMap);

		i.repository_url = submissionInfo.edit_ref;
		i.item_id = submissionInfo.item_ref;

		String redirectionUrl;

		if (submissionInfo.edit_ref != null) {
			logger.info("submitted into workspace, user review needed!");
			logger.info(i.repository_url);
			logger.info(i.item_id);
		} else {
			logger.info("submitted into workflow, no user review needed!");
			logger.info(i.item_id);
		}

		i = project.getPublicationDatabase().insertInDB(i);

		Map<PublicationField, String> m = new EnumMap<>(PublicationField.class);
		m.put(PublicationField.REPOSITORY_URL, i.repository_url);
		project.setMetadata(m);

		if (submissionInfo.inProgress) {
			redirectionUrl = "/final_workspace.html";
		} else {
			redirectionUrl = "/final_workflow.html";
		}

		return new RedirectView(redirectionUrl);
	}

	@PostMapping("query-hierarchy")
	public CollectionList queryHierarchy(
			@RequestParam("user_email") final String user_email,
			@RequestParam("repo_uuid") final String repo_uuid) {
		// FIXME should use WHERE clause instead!
		List<PublicationRepository> pubRepos = config.getPublicationDatabase()
				.getPubRepos();
		PublicationRepository repo = getPubRepo(pubRepos, repo_uuid);

		if (!repo.isUserRegistered(user_email)) {
			logger.info("User " + user_email + " is NOT registered!");
			return new CollectionList(false);
		}
		logger.debug("User " + user_email + " is registered!");

		if (!repo.isUserAssigned(user_email)) {
			logger.info("User " + user_email
					+ " is registered but does not have submit rights to anything!");
			return new CollectionList(true);
		}
		logger.debug("User " + user_email
				+ " is registered and has submit rights to some collections!");

		Hierarchy root = repo.getHierarchy(user_email);
		// root.dump("");
		return new CollectionList(root);

	}

	private PublicationRepository getPubRepo(
			final List<PublicationRepository> pubRepos,
			final String repo_uuid) {
		final UUID uuid = UUID.fromString(repo_uuid);
		for (PublicationRepository r : pubRepos)
			if (r.getDAO().uuid.equals(uuid))
				return r;
		throw new NoSuchElementException(
				"no PublicationRepository with UUID " + repo_uuid);
	}

	public class CollectionList {
		@JsonProperty("user-valid")
		public final boolean isUserValid;
		@JsonProperty("hierarchy")
		public final Hierarchy accessibleCollections;

		public CollectionList(boolean isUserValid) {
			this.isUserValid = isUserValid;
			this.accessibleCollections = null;
		}

		public CollectionList(Hierarchy accessibleCollections) {
			this.isUserValid = true;
			this.accessibleCollections = accessibleCollections;
		}
	}
}
