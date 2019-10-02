package com.windard.me.download;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * @author windard
 * @date 2019/10/1
 */
public class MainDownload {
    public final static String BYTES_RANGE_FORMAT = "bytes=%s-%s";
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("thread-%d").build();

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: java -jar downloader.jar http://xxx.com/test.file");
            System.exit(0);
        }
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            128, 256, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(12800),  threadFactory);

        String requestUrl = args[0];
        String filename = new URL(requestUrl).getPath().substring(1);
        requestUrl = URIUtil.encodeQuery(requestUrl);

        Long chunkSize = 1024000L;
        if (args.length > 1){
            chunkSize = Long.valueOf(args[1]);
        }

        File file = new File(filename);
        if (!file.exists() && !file.createNewFile()){
            throw new Exception("创建文件失败");
        }
        HttpFetcher fetcher = new HttpFetcher(requestUrl);
        Long contentLength = fetcher.fetchLength();
        ProgressBar pb = new ProgressBarBuilder()
            .setStyle(ProgressBarStyle.ASCII)
            .setTaskName(filename)
            .setInitialMax(contentLength)
            .build();
        Long length = 0L;
        Double count = Math.ceil(contentLength.doubleValue() / chunkSize.doubleValue());
        CountDownLatch latch = new CountDownLatch(count.intValue());

        while (length < contentLength){
            HttpFetcher httpFetcher = new HttpFetcher(requestUrl);
            httpFetcher.setStart(length);
            if (contentLength - length > chunkSize){
                length += chunkSize;
                httpFetcher.setEnd(length);
            }else{
                length += chunkSize;
                httpFetcher.setEnd(contentLength);
            }
            threadPoolExecutor.execute(new FileChannel(filename, httpFetcher, pb, latch));
        }

        threadPoolExecutor.shutdown();
        latch.await();
        pb.close();
    }
}
