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
    r = server.request("items/6053", "get", header_default)
    #pprint(r.json())
    
    # Search item per metadata
    #metadata = {'name': 'Zur axiomatischen Fundierung und Struktur von Aktienkursindizes'}
    #r = server.request("items/find-by-metadata-field", "post", header_default, metadata) # NULL pointer exception :)
    #pprint(r.text)
    
    
    
    
    
    
    
    
    # Logout
    r = server.logout()
    print("Token: ", server.getToken())
    

      
      
    
    
if __name__ == '__main__':
    print('This program is being run by itself')
    main()
else:
    print('I am being imported from another module')    

