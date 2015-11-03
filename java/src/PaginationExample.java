import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This example shows how pagination works in the FLOW API by printing out all
 * survey instance ids for a given survey
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

        // The response will contain two keys:
        // 'survey_instances' is the list of survey instances. The size of
        // the survey instances list will be no greater than 20.
        // The response will also contain a 'meta' map. If there are more than
        // 20 responses this map will contain a 'since' cursor which can be
        // used to fetch the next batch of survey instances.
        Map<String, Object> first = get(getSurveyInstancesURL());
        printInstanceIds((List<Map<String, Object>>) first
                .get("survey_instances"));

        // Get the 'since' cursor
        String since = getSince(first);

        while (since != null) {
            Map<String, Object> next = get(getSurveyInstancesURL(since));
            printInstanceIds((List<Map<String, Object>>) next
                    .get("survey_instances"));
            since = getSince(next);
        }

        System.out.println("done.");
    }

    /**
     * Check the responses 'meta' map if it contains the 'since' cursor
     *
     * @param response
     * @return the 'since' cursor string or null if one does not exist
     */
    @SuppressWarnings("unchecked")
    private String getSince(Map<String, Object> response) {
        Map<String, Object> meta = (Map<String, Object>) response.get("meta");
        if (meta == null) {
            return null;
        }
        return (String) meta.get("since");
    }

    /**
     * A survey instance map contains many key/values. In this example we only
     * print the id. This id can be used to fetch the associated question
     * answers.
     *
     * @param surveyInstances
     *            a list of survey instances
     */
    private static void printInstanceIds(
            List<Map<String, Object>> surveyInstances) {
        for (Map<String, Object> surveyInstance : surveyInstances) {
            System.out.println(surveyInstance.get("keyId"));
        }
    }

    /**
     * @return The url to fetch survey instances
     */
    public String getSurveyInstancesURL() {
        return host + "/api/v1/survey_instances?surveyId=" + surveyId;
    }

    /**
     * @param since
     *            The cursor obtained from a previous call to the
     *            survey_instances endpoint
     * @return The url to fetch the next batch of survey instances
     */
    public String getSurveyInstancesURL(String since) {
        return getSurveyInstancesURL() + "&since=" + since;
    }

    /**
     * Issue a get request to the server and return the response
     *
     * @param url
     * @return The response from the server.
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public Map<String, Object> get(String url) throws InvalidKeyException,
            NoSuchAlgorithmException, ClientProtocolException, IOException {
        int beginIndex = url.indexOf("/api");
        int endIndex = url.indexOf("?") == -1 ? url.length() : url.indexOf("?");
        String resource = url.substring(beginIndex, endIndex);

        String date = String.valueOf(new Date().getTime() / 1000);
        String payload = "GET\n" + date + "\n" + resource;
        String signature = generateHMAC(payload, secret);

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

    /**
     * Generate a HMAC token based on the url and the users secret key.
     *
     * @param content
     * @param secretKey
     * @return The token
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String generateHMAC(String content, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes(),
                mac.getAlgorithm());
        mac.init(secret);
        byte[] digest = mac.doFinal(content.getBytes());
        return Base64.encodeBase64String(digest);
    }

    /**
     * Set up the command line options. All the options are required.
     *
     * @return
     */
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

    /**
     * Main entry point of the example program.
     *
     * @param args
     * @throws ParseException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static void main(String[] args) throws ParseException,
            InvalidKeyException, NoSuchAlgorithmException,
            ClientProtocolException, IOException {
        new PaginationExample(args);
    }
}
