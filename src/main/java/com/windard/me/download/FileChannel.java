package com.windard.me.download;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.CountDownLatch;

import me.tongfei.progressbar.ProgressBar;

/**
 * @author windard
 * @date 2019/10/1
 */
public class FileChannel implements Runnable {
    private HttpFetcher fetcher;
    private String filename;
    private ProgressBar progressBar;
    private CountDownLatch latch;

    public FileChannel(String filename, HttpFetcher fetcher, ProgressBar progressBar, CountDownLatch latch){
        this.filename = filename;
        this.fetcher = fetcher;
        this.progressBar = progressBar;
        this.latch = latch;
    }

    public void run() {
        try {
            Long length = download();
            progressBar.stepBy(length);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    public Long download() throws Exception{
        FileInfo fileInfo = fetcher.fetch();

        RandomAccessFile out = null;
        File file = new File(filename);
        if (!file.exists() && !file.createNewFile()){
            throw new Exception("创建文件失败");
        }
        out = new RandomAccessFile(file, "rw");

        java.nio.channels.FileChannel channel = out.getChannel();
        FileLock lock = channel.lock(fileInfo.getContentStart(), fileInfo.getContentLength(), true);
        while (lock == null || !lock.isValid()){
            lock = channel.lock(fileInfo.getContentStart(), fileInfo.getContentLength(), true);
            System.out.println("获得锁🔐失败");
            Thread.sleep(100);
        }

        out.seek(fileInfo.getContentStart());
        out.write(fileInfo.getContent());

        lock.release();
        channel.close();
        out.close();

        return fileInfo.getContentLength();
    }
}
