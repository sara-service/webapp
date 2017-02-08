# The main file. 

import config
import data
from dspace import DSpaceServer

from pprint import pprint



def main():


    header_default = config.HEADER_DEFAULT

    # Create a server and login to it - OPARU
    server = DSpaceServer(config.OPARU_URL,config.OPARU_EMAIL, config.OPARU_PASSWORD, config.OPARU_REQUEST_TYPE,config.OPARU_VERIFY)
    print("Token: ", server.getToken())
    
    
    def getCommunities():
        r = server.request("communities", "get", header_default)
        pprint(r.json())
        
    def getCollections():
        r = server.request("collections", "get", header_default)
        pprint(r.json())    
    
    def getItems():
        r = server.request("items", "get", header_default)
        pprint(r.json())
        
    def getItemID():
        itemID = input("\nEnter the item ID: ")
        itemID = "items/" + itemID
        r = server.request(itemID, "get", header_default) #e.g. 10401 or 10394
        pprint(r.json())
    
    def getItemIDMetadata():
        itemID = input("\nEnter the item ID: ") #e.g. 10401 or 10394
        itemIDMetadata = "items/" + itemID + "/metadata"
        r = server.request(itemIDMetadata, "get", header_default) 
        pprint(r.json())
        
    def findItemsPerMetadataAuthor():
        author = input("\nEnter the author: ") #e.g. "VK test"
        metadata = {"metadata": [{'key': 'dc.contributor.author', 'language': None, 'value': author}]}
        r = server.request("items/find-by-metadata-field", "post", header_default, data=metadata["metadata"][0])
        i = 0
        for item in r.json():
            print("--- item[{0}] ---".format(i))
            pprint(item)
            i = i+1    
    
    # Read:
    # https://github.com/carthage-college/django-djpsilobus/blob/master/djpsilobus/core/utils.py
    #
    # Main info:
    # https://jspace.atlassian.net/wiki/display/DSPACEAPI/POST_items        
    def createNewItem():
        metadata = data.METADATA_TEST
        print("Metadata:")
        pprint(metadata["datacite"])
        
        #header = {**config.HEADER_DEFAULT, **config.OPARU_AUTH_USER} #???
        #header = {**config.HEADER_DEFAULT, **config.OPARU_AUTH} #???
        header = {**config.HEADER_DEFAULT}
        
        #header["rest-dspace-token"] = server.getToken()
        #header = config.HEADER_DEFAULT
        pprint(header)
        #exit(0)
        #metadata = {**metadata["datacite"][0], **config.OPARU_AUTH}
        #metadata = config.OPARU_AUTH
        #metadata["datacite"][0].append(config.OPARU_AUTH)
        print(metadata)
        
        pay_load = {**metadata, **config.OPARU_AUTH}
           
        #pprint(pay_load)
        
        #r = server.request("items.json", "post", header, data=metadata["datacite"][0])
        r = server.request("collections/37/items", "post", header, pay_load)
        pprint(r.text)        
        
       
       
       
    choice = ''
    
    while choice != 'q':
        print("[1] Get all communities.")
        print("[2] Get all collections.")
        print("[3] Get all items.")
        print("[4] Get item ID.")
        print("[5] Get item ID metadata")
        print("[6] Find items per metadata: author (e.g. 'VK test')")
        print("[7] Create a new item")
        
        print("[q] Exit.")

        choice = input("\nPlease make your choice and push the enter: ")
        
        options = {'1': getCommunities,
                   '2': getCollections,
                   '3': getItems,
                   '4': getItemID,
                   '5': getItemIDMetadata,
                   '6': findItemsPerMetadataAuthor,
                   '7': createNewItem,
                   'q': exit
        }
        
        try:
            options[choice]();
        except KeyError:
            print("Wrong number. The program will be closed.")
            choice = 'q'
    
    # Logout & Exit
    print("Logout.")
    r = server.logout()
    print("Token: ", server.getToken())
    print("Exit.")
    exit(0)
    
    

    
    
if __name__ == '__main__':
    print('This program is being run by itself')
    main()
else:
    print('I am being imported from another module')    

