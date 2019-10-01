package com.windard.me.download;

import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author windard
 * @date 2019/10/1
 */
@Data
public class HttpFetcher {
    private HttpClient client = HttpClients.createDefault();
    private String requestUrl;
    private Long start = null;
    private Long end = null;

    public HttpFetcher(String requestUrl){
        this.requestUrl = requestUrl;
    }

    public Long fetchLength() throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        return connection.getContentLengthLong();
    }

    public FileInfo fetch() throws Exception {
        HttpGet httpGet = new HttpGet(requestUrl);
        httpGet.setHeader("Range", String.format(MainDownload.BYTES_RANGE_FORMAT, start, end));
        HttpResponse response = client.execute(httpGet);

        FileInfo fileInfo = new FileInfo(start, end - start);
        byte[] content = EntityUtils.toByteArray(response.getEntity());
        fileInfo.setContent(content);
        return fileInfo;
    }
}
