# The main functions for the communication with DSpace
#
# Some ideas were taken from:
# https://github.com/carthage-college/django-djpsilobus/tree/master/djpsilobus

import json
import requests

# Import config-structures
import config
#from config import AUTH_SETTINGS

# Server-class, methods to communicate with REST 
class Server(object):
    
    # Constructor
    def __init__(self, url, token, request_type, verify):
                 
        self.rest_url = url
        self.token = token
        self.request_type = request_type
        self.verify = verify              
        self.headers_content_type = {"Content-Type": "application/{}".format(request_type)}
        self.headers_token = {"rest-dspace-token": "{}".format(token)}
        self.headers_accept = {"Accept": "application/{}".format(request_type)}
        
    
    def login(self, user, password):
        
        uri = "login"
        headers = {**self.headers_content_type}
        data = {'email': user, 'password': password}         
        r = self.request(uri, "post", headers, data)
        self.token = r
        return r        
    
    
    # Uniform reuest: post, get, delete
    def request(self, uri, action, headers=None, data=None, file=None):  
        
        # check request type (post, get, delete)
        if action == "post":
            action = requests.post
        elif action == "get":
            action = requests.get
        elif action == "delete":
            action = requests.delete
        else:
            return None
        
        url = "{}/{}".format(self.rest_url, uri)
        
        r = action(url, headers=headers, data=json.dumps(data), verify=self.verify)
        return r 
    
    
    def getUrl(self):
        return self.rest_url
    
    def getEmail(self):
        return self.email
    
    def getPassword(self):
        return self.password
    
        
    
    
    