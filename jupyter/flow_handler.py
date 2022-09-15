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


def parse_data(raw_data, form, debug):
    question_group_list = {}
    question_list = {}
    for qg in form.get("questionGroups"):
        question_group_list.update(
            {qg["id"]: {
                 "name": qg["name"],
                 "repeatable": qg["repeatable"]
             }})
        for q in qg.get("questions"):
            question_list.update(
                {q["id"]: {
                     "name": q["name"],
                     "type": q["type"]
                 }})
    collections = []
    for form_instance in raw_data:
        if debug:
            print("___________________________________________________\n")
        metadata = {}
        for meta in form_instance:
            if meta != "responses":
                metadata.update({meta: form_instance[meta]})
                if debug:
                    print(f"{meta}: {form_instance[meta]}")
        data = metadata
        groups = []
        for qg in question_group_list:
            data_group = {}
            group_name = question_group_list[qg]["name"]
            if debug:
                print(f"\nGROUP: {group_name}\n")
            data_group.update({"name": group_name})
            question_group_responses = form_instance.get("responses").get(qg)
            values = []
            if question_group_responses:
                for repeat, responses in enumerate(question_group_responses):
                    value = {"repeat_index": repeat}
                    answers = {}
                    if question_group_list[qg]["repeatable"] and debug:
                        print(f"  REPEAT: {repeat}\n")
                    for qid in responses:
                        question = question_list[qid]
                        answer = responses[qid]
                        answer = flow_handler(answer, question.get("type"))
                        question_name = question.get("name")
                        answers.update({question_name: answer})
                        if debug:
                            print(f"  Q:{question_name}\n  A:{answer}\n")
                    value.update({"answers": answers})
                    values.append(value)
            data_group.update({"data": values})
            groups.append(data_group)
        data.update({"groups": groups})
        collections.append(data)
    return collections
