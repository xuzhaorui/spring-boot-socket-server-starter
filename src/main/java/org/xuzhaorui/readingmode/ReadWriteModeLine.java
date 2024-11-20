package org.xuzhaorui.readingmode;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReadWriteModeLine implements ReadWriteMode {
    @Override
    public Object read(InputStream inputStream, int length) throws IOException {
        // 每次创建一个新的 BufferedReader 实例，不会发生线程共享问题
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        // 线程安全的读取
        return reader.readLine();
    }

    @Override
    public void write(OutputStream outputStream, Object data, int length) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        writer.println((String) data);
    }
}
