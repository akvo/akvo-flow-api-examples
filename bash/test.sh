#!/bin/bash

# curl -I "localhost:3000/orgs/akvoflowsandbox/$*" \
#     -H "Content-Type: application/json" \
#     -H "x-akvo-email: akvo.flow.user.test@gmail.com"

# curl -s "http://localhost:3000/orgs/akvoflowsandbox/form_instances?survey_id=152342023&form_id=146532016&page_size=1&cursor=CjASKmoPYWt2b2Zsb3dzYW5kYm94chcLEg5TdXJ2ZXlJbnN0YW5jZRjq2qRGDBgAIAA" \
#      -H "Content-Type: application/json" \
#      -H "x-akvo-email: akvo.flow.user.test@gmail.com" \
#      | jq

# curl -s "http://localhost:3000/orgs/akvoflowsandbox/form_instances?survey_id=152342023&form_id=146532016&page_size=1&cursor=CjASKmoPYWt2b2Zsb3dzYW5kYm94chcLEg5TdXJ2ZXlJbnN0YW5jZRjq2qRGDBgAIAA" \
#      -H "Content-Type: application/json" \
#      -H "x-akvo-email: akvo.flow.user.test@gmail.com" \
#      | jq

# curl -s "http://localhost:3000/orgs/akvoflowsandbox/stats?survey_id=148412306&form_id=145492013&question_id=147432013" \
#      -H "Content-Type: application/json" \
#      -H "Accept: application/vnd.akvo.flow.v2+json" \
#      -H "x-akvo-email: akvo.flow.user.test@gmail.com" \

# token=$(curl -s \
#      -d "client_id=S6Pm0WF4LHONRPRKjepPXZoX1muXm1JS" \
#      -d "username=${PERSONAL_FLOW_USERNAME}" \
#      -d "password=${PERSONAL_FLOW_PASSWORD}" \
#      -d "grant_type=password" \
#      -d "scope=openid email" \
#      "https://akvotest.eu.auth0.com/oauth/token" \
#     | jq -r .id_token)

token=$(curl -s \
     -d "client_id=qsxNP3Nex0wncADQ9Re6Acz6Fa55SuU8" \
     -d "username=akvo.flow.test.user8@gmail.com" \
     -d "password=7WqCnqCY6kQJV6YQ7dXT" \
     -d "grant_type=password" \
     -d "scope=openid email" \
     "https://akvotest.eu.auth0.com/oauth/token" \
    | jq -r .id_token)

## Online - Stats - Number
# curl -v "https://api-auth0.akvotest.org/flow/orgs/uat1/stats?survey_id=657869130&form_id=644719136&question_id=704409115" \
#      -H "Content-Type: application/json" \
#      -H "Accept: application/vnd.akvo.flow.v2+json" \
#      -H "Authorization: Bearer ${token}"

## Online - Stats - Option
curl -v "https://api-auth0.akvotest.org/flow/orgs/uat1/stats?survey_id=657869130&form_id=644719136&question_id=661689141" \
     -H "Content-Type: application/json" \
     -H "Accept: application/vnd.akvo.flow.v2+json" \
     -H "Authorization: Bearer ${token}"


## Online - Form Instance
# curl -s "https://api-auth0.akvotest.org/flow/orgs/uat1/form_instances?survey_id=657869130&form_id=644719136&page_size=1" \
#      -H "Content-Type: application/json" \
#      -H "Accept: application/vnd.akvo.flow.v2+json" \
#      -H "Authorization: Bearer ${token}"
