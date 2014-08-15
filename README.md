Akvo FLOW API client examples
=============================


## Bash example

If you want to do a quick test, use this _one liner_
example in [Bash](https://www.gnu.org/software/bash/).
It assumes that [curl](http://curl.haxx.se/) and
[openssl](https://www.openssl.org/) are installed.

* Fetching the _Survey_ definitions from `http://localhost:8888`
* Change the values for `access_key` and `secret`

````
access_key="mykey"; \
secret="134"; \
d=$(date +%s); \
sig=$(printf "GET\n${d}\n/api/v1/surveys" | openssl sha1 -binary -hmac "${secret}" | base64); \
curl -H "Date: ${d}" -H "Authorization: ${access_key}:${sig}" http://localhost:8888/api/v1/surveys
````

