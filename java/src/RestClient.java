/*
 *  Copyright (C) 2014 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo FLOW.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RestClient {

    public static void main(String[] args) throws InvalidKeyException,
            NoSuchAlgorithmException, ClientProtocolException, IOException,
            ParseException {

        Options options = setupCommandLineOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, args, true);

        String secret = commandLine.getOptionValue("secret");
        String accessKey = commandLine.getOptionValue("access_key");
        String url = commandLine.getOptionValue("url");

        int beginIndex = url.indexOf("/api");
        int endIndex = url.indexOf("?");
        endIndex = endIndex == -1 ? url.length() : endIndex;

        String resource = url.substring(beginIndex, endIndex);

        String date = String.valueOf(new Date().getTime() / 1000);
        String payload = "GET\n" + date + "\n" + resource;
        String signature = generateHMAC(payload, secret);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.addHeader("Date", date);
        request.addHeader("Authorization", accessKey + ":" + signature);

        CloseableHttpResponse response = client.execute(request);

        BufferedReader content = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));
        StringBuilder sb = new StringBuilder();

        String s = content.readLine();
        while (s != null) {
            sb.append(s);
            s = content.readLine();
        }

        System.out.println(sb.toString());
    }

    public static String generateHMAC(String content, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes(),
                mac.getAlgorithm());
        mac.init(secret);
        byte[] digest = mac.doFinal(content.getBytes());
        return Base64.encodeBase64String(digest);
    }

    public static Options setupCommandLineOptions() {
        Options options = new Options();

        Option secretOption = new Option("secret", true, "Client secret");
        Option accessKeyOption = new Option("access_key", true,
                "Client access key");
        Option urlOption = new Option("url", true, "Rest resource endpoint");

        secretOption.setRequired(true);
        accessKeyOption.setRequired(true);
        urlOption.setRequired(true);

        options.addOption(secretOption);
        options.addOption(accessKeyOption);
        options.addOption(urlOption);

        return options;
    }
}
