from os import environ
import requests as r
import logging


def get_access_token():
    tokenURI = 'https://akvofoundation.eu.auth0.com/oauth/token'
    auth = {
        "client_id": environ['AUTH0_CLIENT_ID'],
        "username": environ['FLOW_USERNAME'],
        "password": environ['FLOW_PASSWORD'],
        "grant_type": "password",
        "scope": "openid email",
    }

    try:
        account = r.post(tokenURI, data=auth).json()
    except:
        logging.error('FAILED TO REQUEST TOKEN')
        return False
    return account["id_token"]


def get_response(token, url):
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/vnd.akvo.flow.v2+json",
        "Authorization": "Bearer {}".format(token)
    }
    response = r.get(url, headers=headers).json()
    return response


def get_folders(baseURI, token, parent_id=0):
    return get_response(token, f"{baseURI}/folders?parent_id={parent_id}")


def get_surveys(baseURI, token, folder_id=0):
    return get_response(token, f"{baseURI}/surveys?folder_id={folder_id}").get("surveys")


def get_forms(baseURI, token, survey_id):
    return get_response(token, f"{baseURI}/surveys/{survey_id}").get("forms")


def get_form_instances(baseURI, token, survey_id, form_id, limit=False,
                       next_page_url=False, result=[]):
    url = f"{baseURI}/form_instances?survey_id={survey_id}&form_id={form_id}"
    if next_page_url:
        url = next_page_url
    res = get_response(token, url)
    result += res.get("formInstances")
    if limit:
        if len(result) >= limit:
            return result
    if res.get("nextPageUrl"):
        get_form_instances(baseURI, token, survey_id, form_id, limit,
                           res.get("nextPageUrl"), result)
    return result
