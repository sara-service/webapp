package bwfdm.sara.publication;

/**
 * Interface for the publication repository.
 * 
 * Idea - to have some interface, which can allow to implement/connect differen
 * publication repositories.
 * 
 * Repositories for the beginning: - OPARU (Uni Ulm) - KOPS (Uni Konstanz)
 * 
 * TODO: implement all communication with the real repositories via these
 * methods.
 * 
 * @author vk
 */
public interface PublicationRepositoryDeprecated {

	public boolean loginPublicationRepository();

	public boolean logoutPublicationRepository();

	public boolean publishElement(String publicationLink, String metadata);

	public String changeElement();

	public String deleteElement();

	public String changeElementMetadata();

	public String getRepositoryUrl();

}
