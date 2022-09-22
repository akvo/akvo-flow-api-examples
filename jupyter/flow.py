from os import environ, mkdir, path
import requests as r
import logging


def log_to_file(filename, content):
    folder = 'log'
    if not path.exists(folder):
        mkdir(folder)
    with open(f"{folder}/{filename}.txt", "a") as log_file:
        log_file.write(content)


def get_access_token():
    tokenURI = 'https://akvofoundation.eu.auth0.com/oauth/token'
    auth = {
        "client_id": environ['AUTH0_CLIENT_ID'],
        "username": environ['PERSONAL_FLOW_USERNAME'],
        "password": environ['PERSONAL_FLOW_PASSWORD'],
        "grant_type": "password",
        "scope": "openid email",
    }

    try:
        account = r.post(tokenURI, data=auth).json()
    except Exception:
        logging.error('FAILED TO REQUEST TOKEN')
        return False
    return account["id_token"]


def get_response(token, url):
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/vnd.akvo.flow.v2+json",
        "Authorization": "Bearer {}".format(token)
    }
    return r.get(url, headers=headers)


def get_folders(baseURI, token, parent_id=0):
    res = get_response(token, f"{baseURI}/folders?parent_id={parent_id}")
    if res.status_code == 200:
        return res.json()
    return False


def get_surveys(baseURI, token, folder_id=0):
    res = get_response(
        token, f"{baseURI}/surveys?folder_id={folder_id}")
    if res.status_code == 200:
        return res.json().get("surveys")
    return False


def get_forms(baseURI, token, survey_id):
    res = get_response(token, f"{baseURI}/surveys/{survey_id}")
    if res.status_code == 200:
        return res.json().get("forms")
    return False


def get_form_instances(baseURI,
                       token,
                       survey_id,
                       form_id,
                       limit=False,
                       next_page_url=False,
                       result=[]):
    url = f"{baseURI}/form_instances?survey_id={survey_id}&form_id={form_id}"
    instance_name = baseURI.split("/")[-1]
    log_file = f"{instance_name}-{survey_id}-{form_id}"
    if next_page_url:
        url = next_page_url
        log_to_file(log_file, f"{next_page_url}\n")
    res = get_response(token, url)
    if res.status_code != 200:
        print(f"ERROR:{url}")
        log_to_file(log_file, f"{res.status_code} {next_page_url}\n")
        return result
    res = res.json()
    result += res.get("formInstances")
    if limit:
        if len(result) >= limit:
            return result
    if res.get("nextPageUrl"):
        get_form_instances(baseURI, token, survey_id, form_id, limit,
                           res.get("nextPageUrl"), result)
    return result
