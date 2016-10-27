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

HEADER_CONTENT_TYPE = {"Content-Type": "application/{}".format(OPARU_REQUEST_TYPE)}
HEADER_ACCEPT = {"Accept": "application/{}".format(OPARU_REQUEST_TYPE)}

HEADER_DEFAULT = {**HEADER_CONTENT_TYPE, **HEADER_ACCEPT}

OPARU_URL = "https://vtstest.rz.uni-ulm.de" #without '/' at the end
