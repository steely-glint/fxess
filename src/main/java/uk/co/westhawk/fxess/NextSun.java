/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.westhawk.fxess;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.phono.srtplight.Log;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author thp
 */
public class NextSun {

    class SunRange {

        Long starts;
        Long ends;

        public String toString() {
            return ((starts != null) && (ends != null)) ? "+" + starts + "(" + (ends - starts) + "hrs)"
                    : "no sun.";
        }

        public SunRange extend(Long v) {
            if (starts == null) {
                starts = v;
                ends = v + 1;
            } else if (Objects.equals(v, ends)) {
                ends = v + 1;
            }
            return this;
        }

        public SunRange extend(SunRange b) {
            Log.info("combining " + this + " into " + b);
            if (starts < b.starts) {
                if (Objects.equals(ends, b.starts)) {
                    ends = b.ends;
                }
                return this;
            } else {
                if (Objects.equals(b.ends, starts)) {
                    b.ends = ends;
                }
                return b;
            }
        }
    }

    public final static void main(String[] argc) {
        Log.setLevel(Log.DEBUG);

        if (argc.length >= 2) {
            String a = argc[0];
            String o = argc[1];

            NextSun ns = new NextSun(Double.valueOf(a), Double.valueOf(o));
            new Thread(() -> {
                try {
                    while (true) {
                        var v = ns.getSuns();
                        Log.info("Suns =" + v);
                        Thread.sleep(1 * 60 * 1000);
                    }
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
    private final String uri;

    //https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=60.10&lon=9.58   
    public NextSun(Double lat, Double lon) {
        uri = String.format("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%3.2f&lon=%3.2f", lat, lon);
    }
    List<String> suncache = null;
    private Instant expires;

    public SunRange getSuns() throws IOException, InterruptedException {
        var now = Instant.now();

        if ((suncache == null) || ((expires != null) && (now.isAfter(expires)))) {
            Log.debug(uri);
            HttpRequest.Builder bu = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Fxess solar panel output prediction")
                    .header("lang", "en")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("Origin", "https://github.com/steely-glint/fxess")
                    .GET();

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
            Optional<String> h = response.headers().firstValue("expires");
            h.ifPresent((exp) -> {
                expires = ZonedDateTime.parse(exp, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            });
            Log.debug("Http status :" + status);
            Log.debug("response body :" + response.body());
            DocumentContext jsonContext = JsonPath.parse(response.body());
            suncache = jsonContext.read("$.properties.timeseries[?((@.data.next_1_hours) && @.data.next_1_hours.summary.symbol_code in ['clearsky_day'])].time");
            Log.info("sun count " + suncache.size());
        } else {
            Log.info("Using cached weather");
        }

        SunRange r = suncache.stream()
                .map((ts) -> now.until(Instant.parse(ts), ChronoUnit.HOURS))
                .filter((u) -> u >= 0)
                .reduce(new SunRange(),
                        (ret, v) -> ret.extend(v),
                        (a, b) -> a.extend(b)  
                );
        return r;
    }
}
