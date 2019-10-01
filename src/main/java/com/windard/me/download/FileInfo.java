package com.windard.me.download;

import lombok.Data;

/**
 * @author windard
 * @date 2019/10/1
 */
@Data
public class FileInfo {
    private Long contentStart;
    private Long contentLength;
    private byte[] content;

    public FileInfo(Long contentStart, Long contentLength){
        this.contentStart = contentStart;
        this.contentLength = contentLength;
    }
}
