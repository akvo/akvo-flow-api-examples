# -*- coding: utf-8 -*-

# Copyright (C) 2014 Stichting Akvo (Akvo Foundation)
#
# This file is part of Akvo FLOW.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import binascii
import calendar
import hmac
import optparse
import sys
import urllib2

from datetime import datetime
from hashlib import sha1


def usage(script_name):
    print ("{} needs three args plz: "
            "--url <URL> --key <KEY> --secret <SECRET>".format(script_name)
        )

def get_script_args(argv):
    parser = optparse.OptionParser()
    parser.add_option('--key', action="store", dest="key")
    parser.add_option('--secret', action="store", dest="secret")
    parser.add_option('--url', action="store", dest="url")

    options, args = parser.parse_args(argv)
    if not (options.url and options.key and options.secret):
        usage(argv[0])
        sys.exit(2)
    return options

def unix_timestamp():
    now = datetime.utcnow()
    return calendar.timegm(now.timetuple())

def api_call_path(url):
    start = url.find('/api/v1')
    end = url.find('?')
    assert start >= 0, "Mis-configured URL, can't find '/api/v1'"
    if end < 0:
        return url[start:]
    else:
        return url[start:end]

def signature(path, secret):
    timestamp = unix_timestamp()
    payload = 'GET\n{timestamp}\n{path}'.format(timestamp=timestamp, path=path)
    signature = hmac.new(secret, payload, sha1)
    return timestamp, binascii.b2a_base64(signature.digest()).rstrip('\n')

def main(url, key, secret):
    path = api_call_path(url)
    timestamp, base64_signature = signature(path, secret)
    auth_header = "{}:{}".format(key, base64_signature)
    request = urllib2.Request(url, headers={"Date": timestamp, "Authorization": auth_header})
    contents = urllib2.urlopen(request).read()
    return contents

if __name__ == '__main__':
    args = get_script_args(sys.argv)
    print main(args.url, args.key, args.secret)
