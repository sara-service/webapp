# Main settings of the project

# Authentification information
OPARU_AUTH = {
    "email": "kushnarenko.volodymyr@gmail.com",
    "password": "OparuTest"
    }

# Default values
OPARU_EMAIL = OPARU_AUTH["email"]
OPARU_PASSWORD = OPARU_AUTH["password"]
#OPARU_TOKEN = ""
OPARU_REQUEST_TYPE = "json"
OPARU_VERIFY = False
OPARU_HEADERS = {
            "Content-Type": "application/{}".format(OPARU_REQUEST_TYPE),
            "rest-dspace-token": "{}".format(""),
            "Accept": "application/{}".format(OPARU_REQUEST_TYPE)
            }
OPARU_URL = "https://vtstest.rz.uni-ulm.de" #without '/' at the end
