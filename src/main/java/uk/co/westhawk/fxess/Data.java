/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.westhawk.fxess;

import com.phono.srtplight.Log;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;

/**
 *
 * @author thp
 */
public class Data {

    private final String user;
    private final String pass;

    class Battery {

        Integer soc;
        Double power;
        Integer temperature;

        public String toString() {
            return "Battery state:" + soc + "% power:" + power + "kw temp:" + temperature + "c";
        }
    }

    public final static void main(String[] argc) {
        Log.setLevel(Log.DEBUG);
        if (argc.length >= 2) {
            String u = argc[0];
            String p = argc[1];
            Data a = new Data(u, p);
            new Thread(() -> {
                try {
                    var v = a.getBattery();
                    Log.info("Soc =" + v.soc);
                    Thread.sleep(10 * 60 * 1000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } else {
            System.exit(1);
        }
    }
    private String devId;
    private String token;

    Data(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    void fetchToken() {
        try {
            String body = buildBody(user, pass);
            token = getToken(body);
            Log.debug("Token is " + token);
            if (token != null) {
                devId = getDevice(token);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String hex_md5(String inp) {
        String ret = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] dig = md5.digest(inp.getBytes());
            ret = getHex(dig, dig.length);
        } catch (NoSuchAlgorithmException ex) {
            Log.error("Error = " + ex);
        }
        return ret;
    }

    static String getHex(byte[] in, int len) {
        char cmap[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder ret = new StringBuilder();
        int top = Math.min(in.length, len);
        for (int i = 0; i < top; i++) {
            ret.append(cmap[0x0f & (in[i] >>> 4)]);
            ret.append(cmap[in[i] & 0x0f]);
        }
        return ret.toString();
    }

    private String buildBody(String user, String pass) {
        //     credentials = {'user': username, 'password': hashlib.md5(password.encode()).hexdigest()}
        String creds = hex_md5(pass);
        String body = "{\"user\":\"" + user + "\",\"password\":\"" + creds + "\"}";
        Log.debug("body is :" + body);
        return body;
    }

    private String getToken(String body) throws Exception {
        String uri = "https://www.foxesscloud.com/c/v0/user/login";

        HttpRequest.Builder bu = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15")
                .header("lang", "en")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Origin", "https://www.foxesscloud.com")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        HttpRequest request = bu.build();
        var client = HttpClient.newHttpClient();

        HttpResponse<String> response
                = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        if (Log.getLevel() >= Log.DEBUG) {
            Log.debug("headers :");

            response.headers().map().
                    forEach((String k, List<String> vs) -> {
                        for (String v : vs) {
                            Log.debug("\t" + k + ":" + v);
                        }
                    });
            Log.debug("Http status :" + status);
            Log.debug("response body :" + response.body());
        }
        DocumentContext jsonContext = JsonPath.parse(response.body());
        return jsonContext.read("$.result.token");
    }

    String getDevice(String token) throws IOException, InterruptedException {

        /*
        headers = {'token': token['value'], 'User-Agent': token['user_agent'], 'lang': token['lang'], 'Content-Type': 'application/json;charset=UTF-8', 'Connection': 'keep-alive'}
    query = {'pageSize': 100, 'currentPage': 1, 'total': 0, 'queryDate': {'begin': 0, 'end':0} }
    response = requests.post(url="https://www.foxesscloud.com/c/v0/device/list", headers=headers, data=json.dumps(query))
         */
        String uri = "https://www.foxesscloud.com/c/v0/device/list";
        String body = "{\"pageSize\": 100, \"currentPage\": 1, \"total\": 0, \"queryDate\": {\"begin\": 0, \"end\":0} }";
        HttpRequest.Builder bu = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15")
                .header("lang", "en")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Origin", "https://www.foxesscloud.com")
                .header("token", token)
                .POST(HttpRequest.BodyPublishers.ofString(body));

        HttpRequest request = bu.build();
        var client = HttpClient.newHttpClient();

        HttpResponse<String> response
                = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        Log.debug("headers :");
        response.headers().map().
                forEach((String k, List<String> vs) -> {
                    for (String v : vs) {
                        Log.debug("\t" + k + ":" + v);
                    }
                });
        Log.debug("Http status :" + status);
        Log.debug("response body :" + response.body());
        DocumentContext jsonContext = JsonPath.parse(response.body());
        String id = jsonContext.read("$.result.devices[0].deviceID");
        return id;
    }

    public Battery getBattery() throws IOException, InterruptedException {

        if ((token == null) || (devId == null)) {
            fetchToken();
        }

        String uri = "https://www.foxesscloud.com/c/v0/device/battery/info?id=" + devId;
        var bat = new Battery();

        Log.debug(uri);
        HttpRequest.Builder bu = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15")
                .header("lang", "en")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Origin", "https://www.foxesscloud.com")
                .header("token", token)
                .GET();

        var client = HttpClient.newHttpClient();

        HttpRequest request = bu.build();
        HttpResponse<String> response
                = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Log.debug("Http status :" + status);

        if ((status >= 200) && (status < 300)) {
            Log.debug("headers :");
            response.headers().map().
                    forEach((String k, List<String> vs) -> {
                        for (String v : vs) {
                            Log.debug("\t" + k + ":" + v);
                        }
                    });
            DocumentContext jsonContext = JsonPath.parse(response.body());
            Log.debug(bat.toString());

            bat.soc = jsonContext.read("$.result.soc");
            Log.debug(bat.toString());

            bat.power = jsonContext.read("$.result.power");
            Log.debug(bat.toString());

            //bat.temperature = jsonContext.read("$.result.temperature");
            Log.debug(bat.toString());
        } else {
            token = null;
            devId = null;
        }
        return bat;
        /*
        {"errno":0,"result":{"status":1,"soc":87,"volt":118.7,"current":3.1,"power":0.403,"residual":6690,"temperature":14.7,"timestamp":"","warning":false,"descUrl":""}}
         */
    }
}
