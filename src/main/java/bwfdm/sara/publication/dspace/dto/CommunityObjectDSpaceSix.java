/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication.dspace.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vk
 */
public class CommunityObjectDSpaceSix {
    
    public String uuid;
    public String name;
    public String handle;
    public String type;
    public String link;
    public List<String> expand;
    public String logo;
    public String parentCommunity;
    public String copyrightText;
    public String introductoryText;
    public String shortDescription;
    public String sidebarText;
    public String countItems;
    public List<String> subcommunities;
    public List<String> collections;   
  
    /*
    
    
    "uuid":"122b7dfd-7814-40fb-b98f-fe3f968c9680",
    "name":"Fakultät für Ingenieurwissenschaften, Informatik und Psychologie",
    "handle":"123456789/82",
    "type":"community",
    "expand":["parentCommunity","collections","subCommunities","logo","all"],
    "logo":null,
    "parentCommunity":null,
    "copyrightText":"",
    "introductoryText":"",
    "shortDescription":"",
    "sidebarText":"",
    "countItems":59,
    "collections":[],
    "link":"/rest/communities/122b7dfd-7814-40fb-b98f-fe3f968c9680",
    "subcommunities":[]}
    
    
    {
    "uuid":456,
    "name":"Reports Community",
    "handle":"10766/10213",
    "type":"community",
    "link":"/rest/communities/456",
    "expand":["parentCommunity","collections","subCommunities","logo","all"],
    "logo":null,
    "parentCommunity":null,
    "copyrightText":"",
    "introductoryText":"",
    "shortDescription":"Collection contains materials pertaining to the Able Family",
    "sidebarText":"",
    "countItems":3,
    "subcommunities":[],
    "collections":[]
    }
    
    */


}
