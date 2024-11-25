package org.xuzhaorui.readingmode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.nio.ByteOrder;
import java.util.concurrent.locks.*;


public class ReadWriteModeLength implements ReadWriteMode {

    private static final Logger log = LoggerFactory.getLogger(ReadWriteModeLength.class);
    private final transient ReadWriteLock lock = new ReentrantReadWriteLock();

    // 读锁
    private final transient Lock readLock = lock.readLock();

    // 写锁使用 ReentrantLock
    private final transient Lock writeLock = new ReentrantLock();

    private static final long LOCK_TIMEOUT = 5;  // 超时时间，单位秒

    @Override
    public Object read(InputStream inputStream, int lengthFieldSize) throws IOException {
        log.info("Attempting to acquire read lock");
        try {
            if (readLock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                log.info("Read lock acquired");
                try {
                    int messageLength;
                    byte[] lengthBuffer = new byte[lengthFieldSize];
                    int bytesRead = inputStream.read(lengthBuffer);

                    if (bytesRead < lengthFieldSize) {
                        throw new IOException("Failed to read length field.");
                    }

                    // 使用 ByteBuffer 来转换字节数组为整型消息长度
                    ByteBuffer byteBuffer = ByteBuffer.wrap(lengthBuffer);
                    byteBuffer.order(ByteOrder.BIG_ENDIAN);  // 设置字节序为大端模式
                    messageLength = byteBuffer.getInt();

                    // 读取实际消息内容
                    byte[] messageBuffer = new byte[messageLength];
                    bytesRead = 0;
                    while (bytesRead < messageLength) {
                        int result = inputStream.read(messageBuffer, bytesRead, messageLength - bytesRead);
                        if (result == -1) {
                            throw new EOFException("Reached end of stream before reading full message.");
                        }
                        bytesRead += result;
                    }

                    return messageBuffer;
                } finally {
                    log.info("Releasing read lock");
                    readLock.unlock();
                }
            } else {
                throw new IOException("Failed to acquire read lock within " + LOCK_TIMEOUT + " seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread was interrupted while trying to acquire read lock", e);
        }
    }

    @Override
    public void write(OutputStream outputStream, Object data, int lengthFieldSize) throws IOException {
        log.info("Attempting to acquire write lock");
        try {
            if (writeLock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                log.info("Write lock acquired");
                try {
                    if (data instanceof byte[]) {
                        byte[] byteArray = (byte[]) data;
                        write(outputStream, lengthFieldSize, byteArray);
                    } else if (data instanceof String) {
                        String message = (String) data;
                        write(outputStream, lengthFieldSize, message.getBytes());
                    } else {
                        String errorStr = "Unsupported data type for write operation";
                        outputStream.write(errorStr.getBytes());
                        throw new IllegalArgumentException(errorStr);
                    }
                } finally {
                    log.info("Releasing write lock");
                    writeLock.unlock();
                }
            } else {
                throw new IOException("Failed to acquire write lock within " + LOCK_TIMEOUT + " seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread was interrupted while trying to acquire write lock", e);
        }
    }

    private static void write(OutputStream outputStream, int lengthFieldSize, byte[] byteArray) throws IOException {
        // 动态计算消息长度字段大小
        int messageLength = byteArray.length;

        // 使用 ByteBuffer 创建一个包含长度字段和消息体的完整字节数组
        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthFieldSize + messageLength);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        // 将消息长度字段写入缓冲区
        byteBuffer.putInt(messageLength);

        // 将消息体写入缓冲区
        byteBuffer.put(byteArray);

        // 一次性将长度字段和消息体数据写入输出流
        outputStream.write(byteBuffer.array());
    }
}





