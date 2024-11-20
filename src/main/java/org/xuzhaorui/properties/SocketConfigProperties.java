package org.xuzhaorui.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Socket 配置属性
 */
@Component
@ConfigurationProperties(prefix = "socket.server")
public class SocketConfigProperties {

    /**
     * 端口
     */
    private int port = 8888;
    private boolean enabled = true;
    /**
     * Socket 传输实体的包
     */
    private String scanSocketMessagePackage;
    /**
     * 读取模式 line 行读模式，适用与Json； length 长度读模式，适用与kryo
     */
    private String readingMode = "line";

    /**
     * 对应 length 长度读模式
     */
    private int length = 4;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getScanSocketMessagePackage() {
        return scanSocketMessagePackage;
    }

    public void setScanSocketMessagePackage(String scanSocketMessagePackage) {
        this.scanSocketMessagePackage = scanSocketMessagePackage;
    }

    public String getReadingMode() {
        return readingMode;
    }

    public void setReadingMode(String readingMode) {
        this.readingMode = readingMode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
