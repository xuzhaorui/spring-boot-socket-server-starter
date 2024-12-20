package org.xuzhaorui.readingmode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.exception.pre.MessageSerializationException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * WebSocket读写模式，目前仅适配文本消息
 */
public class ReadWriteModeWebSocket implements ReadWriteMode {

    private static final Logger log = LoggerFactory.getLogger(ReadWriteModeWebSocket.class);

    @Override
    public Object read(InputStream inputStream, int length) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead =inputStream.read(buffer);
        if (bytesRead != -1){
            byte[] frame = new byte[bytesRead];
            System.arraycopy(buffer, 0, frame, 0, bytesRead);

            // 解码 WebSocket 消息
            return  decodeMessage(frame);
        }else {
            throw new MessageSerializationException("读取websocket请求数据失败");
        }

    }

    @Override
    public void write(OutputStream outputStream, Object data, int length) throws IOException {
        if (data instanceof String) {
            // 简化版编码逻辑，适用于文本消息
            byte[] message = ((String) data).getBytes(StandardCharsets.UTF_8);
            // 构建帧头
            byte[] frame = new byte[message.length + 2];
            frame[0] = (byte) (0x80 | 0x1);  // FIN=1, Opcode=0x1 (文本消息)
            frame[1] = (byte) message.length;   // Payload length

            // 复制消息数据
            System.arraycopy(message, 0, frame, 2, message.length);

            // 发送消息
            outputStream.write(frame);

        } else {
            throw new MessageSerializationException("WebSocket 不支持的数据类型");
        }
    }

    private String decodeMessage(byte[] frame)  {
        // 简化版解码逻辑，适用于文本消息
        if (frame.length < 2) return "";

        // 第一个字节：FIN 和操作码
        int fin = frame[0] & 0x80;
        int opcode = frame[0] & 0x0F;

        // 第二个字节：MASK 和长度
        int maskBit = frame[1] & 0x80;
//        int payloadLength = frame[1] & 0x7F;

        if (fin == 0 || opcode != 0x1 || maskBit == 0) {
            // 非最终帧、非文本消息或未加掩码的消息不处理
            return "";
        }

        // 读取掩码密钥
        byte[] maskingKey = new byte[4];
        System.arraycopy(frame, 2, maskingKey, 0, 4);

        // 读取并解码消息数据
        byte[] encodedData = new byte[frame.length - 6];
        System.arraycopy(frame, 6, encodedData, 0, encodedData.length);

        byte[] decodedData = new byte[encodedData.length];
        for (int i = 0; i < encodedData.length; i++) {
            decodedData[i] = (byte) (encodedData[i] ^ maskingKey[i % 4]);
        }

        return new String(decodedData, StandardCharsets.UTF_8);
    }
}
