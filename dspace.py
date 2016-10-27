# The main functions for the communication with DSpace
#
# Some ideas were taken from:
# https://github.com/carthage-college/django-djpsilobus/tree/master/djpsilobus

import json
import requests

# Import config-structures
import config

# DSpaceServer-class, methods to communicate with REST 
class DSpaceServer(object):
    
    # Constructor
    def __init__(self, url, email, password, request_type, verify):
                 
        self.url = url
        self.rest_url = url + "/rest"
        self.request_type = request_type
        self.verify = verify 
        
        self.token = self.login(email, password) 
        
           
                        
        #self.headers_content_type = {"Content-Type": "application/{}".format(request_type)}
        #self.headers_token = {"rest-dspace-token": "{}".format(self.token)}
        #self.headers_accept = {"Accept": "application/{}".format(request_type)}
        #
        #self.headers_all = {**self.headers_content_type, **self.headers_token, **self.headers_accept}
    
            
    # Login
    def login(self, email, password):
        
        headers = {"Content-Type": "application/{}".format(self.request_type)}
        data = {"email": email, "password": password}      
        r = self.request("login", "post", headers, data) 
        self.token = r.text
        return self.token
    
    
    # Logout
    def logout(self): 
        headers = {'Content-Type': 'application/json', 'rest-dspace-token': self.token}
        r = self.request("logout", "post", headers) 
        if not (self.isAuthenticatedToken(self.token)):
            self.token = "" #if logout is successful -> reset the token
        return r        
    
    
    # Uniform reuest: post, get, put, delete
    def request(self, uri, request_action, headers=None, data=None, file=None, **kwargs):  
        
        # Original requests:
        #
        #requests.get(url, params)
        #requests.post(url, data, json)
        #requests.put(url, data)
        #requests.delete(url)
        #
        # Input-Parameters
        # 1 - url: string
        # 2 - data/params/kwargs: Dict,bytes/file
        # 3 - json/kwargs:
        # 4 - kwargs
        #
        # --> that's why we have to convert all "headers/data/file" to **kwargs 
               
        #print(headers)
        #print(data)
        #print(file)
        #print(kwargs)
        
        # Make full url like a "rest_url/uri"
        url = "{}/{}".format(self.rest_url, uri)
        
        # Check request action (post, get, put, delete)
        if request_action == "post":
            request_action = requests.post
        elif request_action == "get":
            request_action = requests.get
        elif request_action == "put":
            request_action = requests.put
        elif request_action == "delete":
            request_action = requests.delete
        else:
            return None
      
        # Call original "requests.post/get/put/delete"
        # All input parameters instead of "url" now are **kwargs
        #
        # TODO: think about data -> json or string?
        # TODO: "files" as data      
        r = request_action(url, headers=headers, data=json.dumps(data), verify=self.verify)
        return r
        
    
    def getToken(self):
        return self.token
    
    
    # Check status of the token
    # Return: request
    def getTokenStatus(self, token):
        
        headers = {'Content-Type': 'application/json', 'Accept': 'application/json', 'rest-dspace-token': token}
        r = self.request("status", "get", headers) 
        return r
    
    
    # Check if token is authentication 
    # Return: True/False
    def isAuthenticatedToken(self, token):
        r = self.getTokenStatus(token)
        # long construction because we are not sure what REST-server can return
        if r.json()['authenticated'] == True:
            return True
        else:
            return False
    
    
    
    
    
    def getUrl(self):
        return self.rest_url
    
    def getEmail(self):
        return self.email
    
    def getPassword(self):
        return self.password
    
    
    
    #------------------------------
    # TOREMOVE
    #------------------------------
    
    # Request "POST"
    def requestPost(self, uri, headers, data, file=None):
        
        url = "{}/{}".format(self.rest_url, uri)
        if file == None:
            r = requests.post(url, headers=headers, data=json.dumps(data), verify=self.verify)
        else:
            # TODO: think about!
            # Special request for file upload
            url += "?name{}".format
            with open(file, 'rb') as payload:
                files = {file: payload}
                r = requests.post(url, headers=headers, data=json.dumps(data), files=files, verify=self.verify)
        
        return r 
        
    

    
        
    
     
    
    