# Main settings of the project

# Authentification information
OPARU_AUTH = {
    "email": "project-sara@uni-konstanz.de",
    "password": "SaraTest"
    }

OPARU_AUTH_USER = {
    "user": "project-sara@uni-konstanz.de",
    "pass": "SaraTest"
    }

# Default values
OPARU_EMAIL = OPARU_AUTH["email"]
OPARU_PASSWORD = OPARU_AUTH["password"]
#OPARU_TOKEN = ""
OPARU_REQUEST_TYPE = "json"
OPARU_VERIFY = False

HEADER_CONTENT_TYPE = {"Content-Type": "application/{}".format(OPARU_REQUEST_TYPE)}
HEADER_ACCEPT = {"Accept": "application/{}".format(OPARU_REQUEST_TYPE)}
HEADER_TOKEN = {"rest-dspace-token": ""}
HEADER_DEFAULT = {**HEADER_CONTENT_TYPE, **HEADER_ACCEPT}

OPARU_URL = "https://vtstest.rz.uni-ulm.de" #without '/' at the end
