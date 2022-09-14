def flow_handler(data, qType):
    try:
        if (data == 'Error'):
            return ""
        elif (qType == 'OPTION'):
            return handle_option(data)
        elif (qType == 'PHOTO'):
            return handle_photo(data)
        elif (qType == 'CADDISFLY'):
            return handle_caddisfly(data)
        elif (qType == 'VIDEO'):
            return handle_video(data)
        elif (qType == 'GEOSHAPE'):
            return handle_geoshape(data)
        elif (qType == 'GEO'):
            return handle_geolocation(data)
        elif (qType == 'FREE_TEXT'):
            return handle_freetext(data)
        elif (qType == 'SCAN'):
            return handle_barcode(data)
        elif (qType == 'DATE'):
            return handle_date(data)
        elif (qType == 'NUMBER'):
            return handle_number(data)
        elif (qType == 'CASCADE'):
            return handle_cascade(data)
        elif (qType == 'SIGNATURE'):
            return handle_signature(data)
        else:
            return ""
    except Exception:
        data = None
    return data


def handle_option(data):
    response = ""
    for value in data:
        if response == "":
            if not value.get("code"):
                response = value.get('text', "")
            else:
                response = value.get('code') + ":" + value.get('text', "")
        elif response:
            if not value.get("code"):
                response = response + "|" + value.get('text', "")
            else:
                response = response + "|" + value.get(
                    'code', "") + ":" + value.get('text', "")
    return response


def handle_freetext(data):
    return data


def handle_barcode(data):
    return data


def handle_date(data):
    return data


def handle_number(data):
    return data


def handle_cascade(data):
    response = ""
    for value in data:
        if response == "":
            if not value.get("code"):
                response = value.get('name', "")
            else:
                response = value.get('code', "") + ":" + value.get("name", "")
        elif response:
            if not value.get("code"):
                response = response + "|" + value.get('name', "")
            else:
                response = response + "|" + value.get(
                    'code', "") + ":" + value.get("name", "")
    return response


def handle_geoshape(data):
    return data


def handle_geolocation(data):
    response = []
    response.append(data.get('lat'))
    response.append(data.get('long'))
    return response


def handle_caddisfly(data):
    return data


def handle_photo(data):
    return data.get('filename', "")


def handle_video(data):
    return data.get('filename', "")


def handle_signature(data):
    return data.get('name', "")
