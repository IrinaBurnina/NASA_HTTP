import com.google.gson.JsonIOException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {


    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient =
                HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout(5000)
                                .setSocketTimeout(30000)
                                .setRedirectsEnabled(false)
                                .build())
                        .build();
        HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=8aegeJ2xbKhMcdXkuYhGKJw9u6K3cjrqyhtIGVmV");
        CloseableHttpResponse response = httpClient.execute(request);
        String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        String fileName = null;
        String url = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(body);
            url = jsonObject.get("url").toString();
            String[] strings = url.split("/");
            fileName = strings[strings.length - 1];
        } catch (JsonIOException | org.json.simple.parser.ParseException err) {
            System.out.println(err.getMessage());
        }
        HttpGet requestByURL = new HttpGet(url);
        CloseableHttpResponse responseURL = httpClient.execute(requestByURL);
        byte[] contentNews = responseURL.getEntity().getContent().readAllBytes();
        File file = new File(fileName);
        try {
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(fileName);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(contentNews, 0, contentNews.length);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
