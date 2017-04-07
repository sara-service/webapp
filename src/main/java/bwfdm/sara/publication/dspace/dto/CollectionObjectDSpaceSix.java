/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication.dspace.dto;

import java.util.List;

/**
 *
 * @author vk
 */
public class CollectionObjectDSpaceSix {
    
    public String uuid;
    public String name;
    public String handle;
    public String type;
    public String link;
    public List<String> expand;
    public String logo;
    public String parentCommunity;
    public List<String> parentCommunityList;
    public List<String> items;
    public String license;
    public String copyrightText;
    public String introductoryText;
    public String shortDescription;
    public String sidebarText;
    public String numberItems;
    
    /*
    
    {
      "uuid" : "399e769f-faca-4955-8796-824670669c33",
      "name" : "test-collection",
      "handle" : "123456789/512",
      "type" : "collection",
      "expand" : [ "parentCommunityList", "parentCommunity", "items", "license", "logo", "all" ],
      "logo" : null,
      "parentCommunity" : null,
      "parentCommunityList" : [ ],
      "items" : [ ],
      "license" : null,
      "copyrightText" : "",
      "introductoryText" : "",
      "shortDescription" : "",
      "sidebarText" : "",
      "numberItems" : 0,
      "link" : "/rest/collections/399e769f-faca-4955-8796-824670669c33"
    }
    
    */
}
