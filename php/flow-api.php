<?
/*
 *  Copyright (C) 2014 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo FLOW.
 *
 *  Akvo FLOW is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo FLOW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

$access_key = $argv[1];
$secret = $argv[2];
$url = $argv[3];

$date = time(); // UNIX timestamp

$resource = substr($url, strpos($url, "/api/v1"));
$res_parts = split("\?", $resource);

$payload = "GET\n$date\n" . $res_parts[0];

$signature = base64_encode(hash_hmac("sha1", $payload, $secret, TRUE));

$curl = curl_init();

curl_setopt($curl, CURLOPT_URL, $url);
curl_setopt($curl, CURLOPT_HTTPHEADER, array("Date: $date", "Authorization: $access_key:$signature"));

curl_exec($curl);
curl_close($curl);

?>
