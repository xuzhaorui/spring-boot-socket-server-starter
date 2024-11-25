package org.xuzhaorui.server;

@FunctionalInterface
public interface MessageConsumer {
    void consume(Object message);  // 消费消息的逻辑
}


