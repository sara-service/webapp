package bwfdm.sara.publication.dspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.Deposit;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.EntryPart;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClient;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDCollection;
import org.swordapp.client.SWORDError;
import org.swordapp.client.SWORDWorkspace;
import org.swordapp.client.ServiceDocument;
import org.swordapp.client.UriRegistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.SaraMetaDataField;

public class DSpace implements PublicationRepository {
	
	class SDData {
		public SDData(ServiceDocument sd, List<SWORDWorkspace> ws) {
			this.sd=sd;
			this.ws=ws;
		}
		public final ServiceDocument sd;
		public final List<SWORDWorkspace> ws;
	}

	protected static final Logger logger = LoggerFactory.getLogger(DSpace.class);

	private final String swordUser, swordPwd, swordApiEndpoint, swordServiceDocumentRoot;
	private final Repository dao;

	// for SWORD
	private SWORDClient swordClient;

	// for IR
	private final String depositType;
	private final boolean checkLicense;
	private final String publicationType;

	@JsonCreator
	public DSpace(@JsonProperty("sword_user") final String su, @JsonProperty("sword_pwd") final String sp,
			@JsonProperty("sword_api_endpoint") final String se,
			@JsonProperty(value = "deposit_type", required = false) final String dt,
			@JsonProperty(value = "check_license", required = false) final String cl,
			@JsonProperty(value = "publication_type", required = false) final String pt,
			@JsonProperty("dao") final Repository dao) {
		this.dao = dao;

		swordUser = su;
		swordPwd = sp;
		swordApiEndpoint = se;
		swordServiceDocumentRoot = swordApiEndpoint + "/servicedocument";

		depositType = dt;

		if ((cl != null) && (cl.toLowerCase().equals("false"))) {
			checkLicense = false;
		} else {
			checkLicense = true;
		}

		publicationType = pt;

		swordClient = new SWORDClient();
	}

	public SDData serviceDocument(final AuthCredentials authCredentials, final String sdURL_) {
		ServiceDocument sd = null;
		List<SWORDWorkspace> ws = null;
		final String sdURL = (sdURL_ == null) ? swordServiceDocumentRoot : sdURL_; 
		
		try {
			sd = swordClient.getServiceDocument(sdURL, authCredentials);
			if (sd != null)
				 ws = sd.getWorkspaces();
		} catch (SWORDClientException | ProtocolViolationException e) {
			logger.error(
					"Exception by accessing service document: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
		
		return new SDData(sd,ws);
	}

	@Override
	public boolean isAccessible() {
		// checks whether the root service document is accessible
		return (serviceDocument(new AuthCredentials(swordUser, swordPwd), null) != null);
	}

	@Override
	public boolean isUserRegistered(final String loginName) {
		// checks whether the user has access / is registered
		return (serviceDocument(new AuthCredentials(swordUser, swordPwd, loginName), null) != null);
	}

	@Override
	public boolean isUserAssigned(final String loginName) {
		final SDData sdd = serviceDocument(new AuthCredentials(swordUser, swordPwd, loginName), null);

		if (sdd.sd == null)
			return false;

		Hierarchy hierarchy = getHierarchy(loginName);
		return (hierarchy.getCollectionCount() > 0);
	}

	private Hierarchy buildHierarchyLevel(final Hierarchy hierarchy, final String sdURL, final String loginName) {
		final SDData sdd = serviceDocument(new AuthCredentials(swordUser,swordPwd,loginName),sdURL); 
		String lvlName = null;
		SWORDWorkspace root = null;
	
	    if (sdd.ws != null) {
	    	if (sdd.ws.size()!=1) {
	    		logger.error("Something is strange! There should be exactly one top-level workspace!");
	    		return null;		
	    	} else {
	    		root = sdd.ws.get(0);
	    		lvlName = root.getTitle();
	    		logger.info("Found bibliography level "+lvlName);
	    	}
	    }
	    
	    hierarchy.setName(lvlName);
		
	    for (final SWORDCollection coll : root.getCollections()) {
	    	final List<String> subservices = coll.getSubServices();
	    	boolean isCollection = subservices.isEmpty();
	    	
	    	Hierarchy child = new Hierarchy(coll.getTitle(), null);
	    	child.setURL(coll.getHref().toString());
	    	
	    	final String[] chops=child.getURL().split("/");
	    	try {
	    		child.setHandle(chops[chops.length-2]+"/"+chops[chops.length-1]);
	    	} catch (ArrayIndexOutOfBoundsException e) {
	    		logger.error("Cannot obtain a valid DSpace handle from the URL!");
	    		child.setHandle(null);
	    	}

	    	if (isCollection) {
	    		logger.info("FOUND COLLECTION "+child.getName());
	    		child.setCollection(true);
	    		if (checkLicense) {
		    		try {
		    			child.setPolicy(coll.getCollectionPolicy());
		    		} catch (ProtocolViolationException e) {
		    			logger.info("No policy found for "+coll.getTitle()+"! Collections must deliver a policy!");
		    		}
	    		}
				hierarchy.addChild(child);
	    	} else {
	    		logger.info("FOUND COMMUNITY "+child.getName());
	    		child.setCollection(false);
	    		for (String sd: subservices) {
	    			hierarchy.addChild(buildHierarchyLevel(child, sd, loginName));
	    		}
			}
	    }
		
		return hierarchy;
	}
	
	@Override
	public Hierarchy getHierarchy(String loginName) {
		Hierarchy h = new Hierarchy(null,null);
		return buildHierarchyLevel(h, swordServiceDocumentRoot, loginName);
	}

	@Override
	public SubmissionInfo publishMetadata(String userLogin, String collectionURL, Map<String, String> metadataMap) {

		String mimeFormat = "application/atom+xml";
		String packageFormat = UriRegistry.PACKAGE_BINARY;

		return publishElement(userLogin, collectionURL, mimeFormat, packageFormat, null, metadataMap);
	}

	@Override
	public SubmissionInfo publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath,
			Map<String, String> metadataMap) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private SubmissionInfo publishElement(String userLogin, String collectionURL, String mimeFormat,
			String packageFormat, File file, Map<String, String> metadataMap) {

		// FIXME TODO
		SubmissionInfo submissionInfo = new SubmissionInfo();

		// Check if only 1 parameter is used (metadata OR file).
		// Multipart is not supported.
		if (((file != null) && (metadataMap != null)) || ((file == null) && (metadataMap == null))) {
			return null;
		}

		AuthCredentials authCredentials = new AuthCredentials(swordUser, swordPwd, userLogin);

		Deposit deposit = new Deposit();

		try {
			// Check if "meta data as a Map"
			if (metadataMap != null) {
				EntryPart ep = new EntryPart();
				for (Map.Entry<String, String> metadataEntry : metadataMap.entrySet()) {
					if (metadataEntry.getKey().equals(SaraMetaDataField.TYPE.getDisplayName())) {
						if (publicationType != null) {
							metadataEntry.setValue(publicationType);
						}
					}
					ep.addDublinCore(metadataEntry.getKey(), metadataEntry.getValue());
				}
				deposit.setEntryPart(ep);
			}

			// Check if "file"
			if (file != null) {
				deposit.setFile(new FileInputStream(file));
				deposit.setFilename(file.getName()); // deposit works properly
														// ONLY with a
														// "filename" parameter
														// --> in curl: -H
														// "Content-Disposition:
														// filename=file.zip"
			}

			deposit.setMimeType(mimeFormat);
			deposit.setPackaging(packageFormat);

			if (depositType != null) {
				switch (depositType.toLowerCase()) {
				case "workflow":
					deposit.setInProgress(false);
					break;
				default: // "workspace"
					deposit.setInProgress(true);
					break;
				}
			} else {
				deposit.setInProgress(true);
			}
			submissionInfo.inProgress = deposit.isInProgress();

			DepositReceipt receipt = swordClient.deposit(collectionURL, deposit, authCredentials);

			String[] parts = receipt.getLocation().split("/");

			if (receipt.getSplashPageLink() != null) {
				submissionInfo.edit_ref = receipt.getSplashPageLink().getHref();
			}
			submissionInfo.item_ref = parts[parts.length - 1];
			return submissionInfo;

		} catch (FileNotFoundException e) {
			logger.error("Exception by accessing a file: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;

		} catch (SWORDClientException | SWORDError | ProtocolViolationException e) {
			logger.error("Exception by making deposit: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
	}


	@Override
	public Repository getDAO() {
		return dao;
	}

	@Override
	public void dump() {
		// TODO Auto-generated method stub
	}

}
