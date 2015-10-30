import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This example shows how pagination works in the FLOW api by printing out all
 * survey instances ids for a given survey
 * 
 * usage: see pagination-example shell script
 */
public class PaginationExample {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String host;
    private final String secret;
    private final String accessKey;
    private final String surveyId;

    @SuppressWarnings("unchecked")
    public PaginationExample(String[] args) throws ParseException,
            InvalidKeyException, NoSuchAlgorithmException,
            ClientProtocolException, IOException {
        CommandLine commandLine = new BasicParser().parse(
                getCommandLineOptions(), args, true);

        host = commandLine.getOptionValue("host");
        secret = commandLine.getOptionValue("secret");
        accessKey = commandLine.getOptionValue("access_key");
        surveyId = commandLine.getOptionValue("survey_id");

        Map<String, Object> first = get(getSurveyInstancesURL());
        printInstanceIds((List<Map<String, Object>>) first
                .get("survey_instances"));
        String since = getSince(first);

        while (since != null) {
            Map<String, Object> next = get(getSurveyInstancesURL(since));
            printInstanceIds((List<Map<String, Object>>) next
                    .get("survey_instances"));
            since = getSince(next);
        }

        System.out.println("done.");
    }

    @SuppressWarnings("unchecked")
    private String getSince(Map<String, Object> response) {
        Map<String, Object> meta = (Map<String, Object>) response.get("meta");
        if (meta == null) {
            return null;
        }
        return (String) meta.get("since");
    }

    private static void printInstanceIds(
            List<Map<String, Object>> surveyInstances) {
        for (Map<String, Object> surveyInstace : surveyInstances) {
            System.out.println(surveyInstace.get("keyId"));
        }
    }

    public String getSurveyInstancesURL() {
        return host + "/api/v1/survey_instances?surveyId=" + surveyId;
    }

    public String getSurveyInstancesURL(String since) {
        return getSurveyInstancesURL() + "&since=" + since;
    }

    public Map<String, Object> get(String url) throws InvalidKeyException,
            NoSuchAlgorithmException, ClientProtocolException, IOException {
        int beginIndex = url.indexOf("/api");
        int endIndex = url.indexOf("?");
        endIndex = endIndex == -1 ? url.length() : endIndex;

        String resource = url.substring(beginIndex, endIndex);

        String date = String.valueOf(new Date().getTime() / 1000);
        String payload = "GET\n" + date + "\n" + resource;
        String signature = RestClient.generateHMAC(payload, secret);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.addHeader("Date", date);
        request.addHeader("Authorization", accessKey + ":" + signature);

        CloseableHttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unexpected response when fetching " + url);
        }

        BufferedReader content = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));
        StringBuilder sb = new StringBuilder();

        String s = content.readLine();
        while (s != null) {
            sb.append(s);
            s = content.readLine();
        }

        return OBJECT_MAPPER.readValue(sb.toString(),
                new TypeReference<Map<String, Object>>() {
                });
    }

    public static Options getCommandLineOptions() {
        Options options = new Options();

        Option hostOption = new Option("host", true, "Host");
        Option secretOption = new Option("secret", true, "Secret key");
        Option accessKeyOption = new Option("access_key", true, "Access key");
        Option surveyIdOption = new Option("survey_id", true, "Survey id");

        hostOption.setRequired(true);
        secretOption.setRequired(true);
        accessKeyOption.setRequired(true);
        surveyIdOption.setRequired(true);

        options.addOption(hostOption);
        options.addOption(secretOption);
        options.addOption(accessKeyOption);
        options.addOption(surveyIdOption);

        return options;
    }

    public static void main(String[] args) throws ParseException,
            InvalidKeyException, NoSuchAlgorithmException,
            ClientProtocolException, IOException {
        new PaginationExample(args);
    }
}
