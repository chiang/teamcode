#!/usr/bin/python

import sys
import requests
import logging

repository = sys.argv[1]
revision = sys.argv[2]

logging.basicConfig(filename=repository + '-error.log',level=logging.ERROR)

url = 'http://svn.rightstack.net:9494/api/v1/changeset?rms_id=123'
response = requests.post(url, data = {'repository':repository, 'revision':revision});

#logging.debug("repository: " + repository)
#logging.debug("revision: " + revision)