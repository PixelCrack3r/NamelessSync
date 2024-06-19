package me.pixelgames.pixelcrack3r.namelesssync.api;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    private URL url;

    private String method;
    private final Map<String, String> requestProperties;

    private String body;

    private String response;
    private int responseCode;

    private URLConnection connection;

    public HttpRequest() {
        this.method = "GET";
        this.requestProperties = new HashMap<>();
    }

    public void doRequest() throws IOException {

        if(this.url == null) throw new IOException("url cannot be null");
        HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

        connection.setDoInput(true);
        connection.setRequestMethod(this.method);

        for(String key : this.requestProperties.keySet()) {
            connection.setRequestProperty(key, this.requestProperties.get(key));
        }


        if(this.body != null) {
            connection.setRequestProperty("Content-Length",Integer.toString(this.body.getBytes().length));

            connection.setDoOutput(true);

            PrintWriter writer = new PrintWriter(connection.getOutputStream());

            writer.println(this.body);

            writer.flush();
            writer.close();
        }

        connection.connect();


        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();

        String line;
        while((line = reader.readLine()) != null) {
            response.append(line).append("\r\n");
        }

        this.responseCode = connection.getResponseCode();
        this.response = response.toString();

        connection.disconnect();
        this.connection = connection;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setBody(JsonObject object, ContentType type) {
        StringBuilder encoded = new StringBuilder();
        switch (type) {
            case URL_ENCODED: {
                for(String key : object.keySet()) {
                    encoded
                            .append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(object.get(key).getAsString(), StandardCharsets.UTF_8))
                            .append("&");
                }
                break;
            }
            case JSON: {
                encoded.append(object.toString());
                break;
            }
        }
        this.setBody(encoded.toString());
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getRequestProperties() {
        return this.requestProperties;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setProperty(String key, String value) {
        this.requestProperties.put(key, value);
    }

    public void getProperty(String key) {
        this.requestProperties.get(key);
    }

    public String getResponse() {
        return this.response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getHeaders() {
        return this.connection.getHeaderFields();
    }

    public String getHeader(String key) {
        return this.connection.getHeaderField(key);
    }

    public enum ContentType {

        JSON,
        URL_ENCODED;

    }

}