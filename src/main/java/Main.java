import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        String bodyBuildToArray = "[" + body + "]";
        List<News> newsList = jsonToList(bodyBuildToArray);
        List<String> urls = new ArrayList<>();
        for (News newsInfo : newsList) {
            String url = newsInfo.getUrl();
            urls.add(url);
        }
        for (int i = 0; i < urls.size(); i++) {
            HttpGet requestByURL = new HttpGet(urls.get(i));
            CloseableHttpResponse responseURL = httpClient.execute(requestByURL);
            byte[] contentNews = responseURL.getEntity().getContent().readAllBytes();
            List<String> strings = urls.stream()
                    .flatMap(value -> Arrays.stream(value.split("/")))
                    .collect(Collectors.toList());
            String newFileName = strings.get(strings.size() - 1);
            File file = new File(newFileName);
            try {
                file.createNewFile();
                try (FileOutputStream fos = new FileOutputStream(newFileName);
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


    public static List<News> jsonToList(String json) {
        List<News> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(json);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            for (Object newsNASA : jsonArray) {
                News news = gson.fromJson(String.valueOf(newsNASA), News.class);
                list.add(news);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
