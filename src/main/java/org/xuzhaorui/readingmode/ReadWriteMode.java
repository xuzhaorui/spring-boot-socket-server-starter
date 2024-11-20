package org.xuzhaorui.readingmode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 读写模式
 */
public interface ReadWriteMode {

    Object read(InputStream inputStream, int length ) throws IOException;
    void write(OutputStream outputStream, Object data,int length) throws IOException;
}
