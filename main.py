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
       
    # Get communities
    #header = {"Content-Type": "application/{}".format(config.OPARU_REQUEST_TYPE)}
    
    r = server.request("communities", "get", header_default)
    #pprint(r.json())
    
    
    # Get items
    r = server.request("items", "get", header_default)
    #pprint(r.json())
    
    # Get item ID
    #r = server.request("items/6053", "get", header_default)
    r = server.request("items/10401", "get", header_default)
    pprint(r.json())
    
    # Get item ID metadata
    #r = server.request("items/6053/metadata", "get", header_default)
    
    #VK Test 1
    #r = server.request("items/10394/metadata", "get", header_default)
    #VK Test 2
    r = server.request("items/10401/metadata", "get", header_default)
    pprint(r.json())
    #exit(0)
    
    
    # Search item per metadata
    
    #metadata = data.METADATA_ENTRY
    #metadata["key"] = "dc.contributor.author"
    #metadata["value"] = "Süslü, Mustafa Kemal"
    #metadata = {'key': 'dc.publisher', 'language': None, 'value': 'Universität Ulm'}
    #metadata = {'key': 'dc.type', 'language': '', 'value': 'Dissertation'}
    
    #works
    #metadata = {"metadata": [{'key': 'dc.identifier.doi', 'language': None, 'value': 'http://dx.doi.org/10.5072/OPARUtest-6003'}]}
    
    #works - VK
    metadata = {"metadata": [{'key': 'dc.contributor.author', 'language': None, 'value': 'VK test'}]}
    
    print("------------------")
    print("Try to find item via metadata:")
    print(metadata)
    print("------------------")
    r = server.request("items/find-by-metadata-field", "post", header_default, data=metadata["metadata"][0])
    #pprint(r)
    i = 0
    for item in r.json():
        print("--- item[{0}] ---".format(i))
        pprint(item)
        i = i+1
        
     
    
    
    # Create an item
    # Read:
    # https://github.com/carthage-college/django-djpsilobus/blob/master/djpsilobus/core/utils.py
    #
    metadata = data.METADATA_DEFAULT
    pprint(metadata["default"][0])
    
    
    
    # Logout
    r = server.logout()
    print("Token: ", server.getToken())
    

      
      
    
    
if __name__ == '__main__':
    print('This program is being run by itself')
    main()
else:
    print('I am being imported from another module')    

