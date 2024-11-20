package org.xuzhaorui.messageserialization;


public interface SocketMessageSerializer<T,R>{
    /**
     * 序列化方法，将对象转化序列化
     * @param object 需要序列化的对象
     * @return 序列化后的数据
     * @throws RuntimeException 序列化异常
     */
    T serialize(R object) throws RuntimeException;

    /**
     * 反序列化方法，将消息转化为对象
     * @param message 待反序列化的消息
     * @param clazz 对象的类类型
     * @return 反序列化后的对象
     * @throws Exception 反序列化异常
     */
    R deserialize( T message, Class<R> clazz) throws Exception;
}
