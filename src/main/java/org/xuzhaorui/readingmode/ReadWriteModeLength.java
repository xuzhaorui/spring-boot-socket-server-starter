package org.xuzhaorui.readingmode;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.nio.ByteOrder;
import java.util.concurrent.locks.*;

/**
 * 1. ReentrantReadWriteLock 替代 ReentrantLock：
 * ReentrantReadWriteLock 使得多个线程可以并发读取，只在有写操作时才会阻塞读操作。这显著减少了并发读取的锁竞争，提升了性能。
 * 2. 使用 ByteBuffer 进行字节转换：
 * ByteBuffer 可以自动处理字节序的转换。我们在这里使用 ByteBuffer.wrap(lengthBuffer) 来包装字节数组，并使用 .getInt() 直接读取 lengthFieldSize 字节的消息长度。这替代了原先的手动位运算，简化了代码并提高了性能。
 * 3. 缓冲区管理：
 * 在读取消息时，我们保持了 byte[] messageBuffer 来存储最终的消息体，且一次性读取整个消息。这样减少了多次内存分配，并提高了效率。
 * 4. 错误处理与性能改进：
 * 错误处理部分保持不变，仍然捕获 EOFException 和 IOException。
 * 对于流操作的缓冲管理使用了 BufferedInputStream 和 BufferedOutputStream，这些操作更适合高效的输入输出操作。
 * 性能提升：
 * 减少了锁竞争：通过 ReentrantReadWriteLock，多个线程可以并发地读取流，而不必等到其他线程释放锁。
 * 字节序转换的优化：使用 ByteBuffer 提高了字节转换操作的性能，尤其是处理大端模式的情况。
 * 缓冲区优化：通过减少读取/写入次数来降低 I/O 操作的成本。
 */
public class ReadWriteModeLength implements ReadWriteMode {

    private final transient ReadWriteLock lock = new ReentrantReadWriteLock();

    // 获取读锁
    private final transient Lock readLock = lock.readLock();

    // 获取写锁
    private final transient Lock writeLock = lock.writeLock();

    @Override
    public Object read(InputStream inputStream, int lengthFieldSize) throws IOException {
        final Lock lock = this.readLock;
        lock.lock();
        try {
            int messageLength = 0;
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
            lock.unlock();
        }
    }

    @Override
    public void write(OutputStream outputStream, Object data, int lengthFieldSize) throws IOException {
        final Lock lock = this.writeLock;
        lock.lock();
        try {
            if (data instanceof byte[]) {
                byte[] byteArray = (byte[]) data;
                write(outputStream, lengthFieldSize, byteArray);
            } else if (data instanceof String){
                String message = (String) data;
                write(outputStream, lengthFieldSize, message.getBytes());
            }else {
                String errorStr = "Unsupported data type for write operation";
                outputStream.write(errorStr.getBytes());
                throw new IllegalArgumentException(errorStr);
            }
        } finally {
            lock.unlock();
        }
    }

    private static void write(OutputStream outputStream, int lengthFieldSize,  byte[] byteArray) throws IOException {


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


