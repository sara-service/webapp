# The main file. 

from dspace import Server
import config


def main():
    server = Server(config.OPARU_URL,config.OPARU_TOKEN,config.OPARU_REQUEST_TYPE,config.OPARU_VERIFY)
    
    request = server.login(config.OPARU_MAIL, config.OPARU_PASSWORD)
    print(request.text)
    
    
    
    
    
    
    
if __name__ == '__main__':
    print('This program is being run by itself')
    main()
else:
    print('I am being imported from another module')    

