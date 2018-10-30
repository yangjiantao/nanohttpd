package org.nanohttpd.util;

/**
 * server传输进度回调接口
 */
public interface ProgressListener {
    /**
     * @param bytesRead totalTypesRead
     * @param contentLength totalTypes
     * @param done isFinish
     */
    void update(long bytesRead, long contentLength, boolean done);

    /**
     * 是否已取消。传输文件时可能用到。
     * @return
     */
    boolean isCanceled();
}