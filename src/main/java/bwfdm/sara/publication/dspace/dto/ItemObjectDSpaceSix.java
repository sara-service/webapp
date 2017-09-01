package bwfdm.sara.publication.dspace.dto;

import java.util.List;

/**
 * 
 * @author vk
 */
public class ItemObjectDSpaceSix {

	public String uuid;
	public String name;
	public String handle;
	public String type;
	public String link;
	public List<String> expand;
	public String lastModified;
	public String parentCollection;
	public List<String> parentCollectionList;
	public List<String> parentCommunityList;
	public String bitstreams;
	public String archived;
	public String withdrawn;
	public String metadata;

	// {
	// "uuid" : "6d408a16-6ab9-41d1-974b-373c06d5d63d",
	// "name" :
	// "Gestational Weight Gain and Fetal-Maternal Adiponectin, Leptin, and CRP: results of two birth cohorts studies",
	// "handle" : "123456789/1206",
	// "type" : "item",
	// "expand" : [ "metadata", "parentCollection", "parentCollectionList",
	// "parentCommunityList", "bitstreams", "all" ],
	// "lastModified" : "2017-03-23 11:15:28.61",
	// "parentCollection" : null,
	// "parentCollectionList" : null,
	// "parentCommunityList" : null,
	// "bitstreams" : null,
	// "archived" : "true",
	// "withdrawn" : "false",
	// "link" : "/rest/items/6d408a16-6ab9-41d1-974b-373c06d5d63d",
	// "metadata" : null
	// }
	//
	// {
	// "id":14301,
	// "name":"2015 Annual Report",
	// "handle":"123456789/13470",
	// "type":"item",
	// "link":"/rest/items/14301",
	// "expand":["metadata","parentCollection","parentCollectionList","parentCommunityList","bitstreams","all"],
	// "lastModified":"2015-01-12 15:44:12.978",
	// "parentCollection":null,
	// "parentCollectionList":null,
	// "parentCommunityList":null,
	// "bitstreams":null,
	// "archived":"true",
	// "withdrawn":"false"
	// }

}
