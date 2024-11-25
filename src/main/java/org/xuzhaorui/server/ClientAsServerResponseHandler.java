package org.xuzhaorui.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.annotation.ClientAsServer;
import org.xuzhaorui.exception.SocketClientAsServerException;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.store.SocketMessageInfo;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.utils.AnnotationUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * 客户端作为服务器响应处理程序 生产者-消费者模式
 */
public class ClientAsServerResponseHandler {


    private static final Logger log = LoggerFactory.getLogger(ClientAsServerResponseHandler.class);

    private final SocketMessageInfoRegistry socketMessageInfoRegistry;
    // 全局共享消息数据
    private final Lock lock = new ReentrantLock();
    private final Condition messageAvailable = lock.newCondition();
    private Object sharedMessage = null;  // 用于存储消息

    public ClientAsServerResponseHandler(SocketMessageInfoRegistry socketMessageInfoRegistry) {
        this.socketMessageInfoRegistry = socketMessageInfoRegistry;
    }

    // process 方法作为生产者，处理客户端的响应
    public void process(SocketRequest request) throws RuntimeException {
        Object messageBean = request.getMessageBean();
        SocketMessageInfo socketMessageInfo = socketMessageInfoRegistry.getSocketMessageInfo(messageBean.getClass());

        boolean isClientAsServer = AnnotationUtils.findValue(messageBean, ClientAsServer.class, socketMessageInfo.getClientAsServerPaths());

        if (isClientAsServer) {
            lock.lock();  // 加锁保护共享资源
            try {
                sharedMessage = messageBean;  // 将解析后的消息存入共享区
                messageAvailable.signal();  // 唤醒等待的线程
            } finally {
                lock.unlock();  // 确保在任何情况下都释放锁
            }
            throw new SocketClientAsServerException("客户端作为服务端响应直接放行");
        }
    }

    // 消费者方法，等待客户端响应消息
    public void sendMessageAndAwaitResponse(MessageConsumer consumer) {
        try {
            lock.lock();  // 加锁等待共享数据
            while (sharedMessage == null) {  // 如果共享消息数据为空，等待唤醒
                messageAvailable.await();  // 阻塞等待，直到有新消息被设置
            }
            // 在这里 sharedMessage 已经不为空，可以处理
            log.info("获取到共享消息：{}", sharedMessage);
            consumer.consume(sharedMessage);
            sharedMessage = null;  // 清空消息数据，准备下一次消费
        } catch (InterruptedException e) {
            log.error("等待客户端响应消息时被中断", e);
        } finally {
            lock.unlock();  // 确保释放锁
        }
    }
}
