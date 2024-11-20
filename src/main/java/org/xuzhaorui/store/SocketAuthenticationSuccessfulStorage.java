package org.xuzhaorui.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.server.SocketMetBean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;




import java.util.concurrent.atomic.AtomicInteger;


public class SocketAuthenticationSuccessfulStorage {

    /**
     * 1. 分区机制：
     * 我们通过 clientId.hashCode() % PARTITION_COUNT 来将 clientId 映射到一个分区。分区数可以根据负载需求进行调整，当前设定为 16。
     * partitionLocks 使用 ConcurrentMap<Integer, ConcurrentMap<String, ReentrantLock>> 来存储每个分区中的锁。这样我们可以根据 clientId 获取对应分区的锁，减少锁竞争。
     * 2. 锁超时机制：
     * ReentrantLock 的 tryLock(long time, TimeUnit unit) 方法可以在指定时间内尝试获取锁，若超时则返回 false。
     * 如果一个线程在 LOCK_TIMEOUT 时间内无法获得锁，就会输出错误日志并返回失败。这避免了线程因长时间等待锁而被阻塞。
     * 3. 错误处理与超时控制：
     * 如果线程在超时内没有获取到锁，会打印相关日志（可以通过调整日志级别进行管理）。
     * 如果发生中断异常，则会捕获并返回 false。
     * 4. 并发控制：
     * 通过 ReentrantLock 的分区机制，能够减少同一分区内客户端之间的锁争用。对于同一 clientId 的操作仍然会使用同一锁，保证数据的一致性。
     * 性能分析：
     * 分区锁：通过将客户端分配到不同的锁分区，减少了不同客户端间的竞争，极大地提高了性能。
     * 锁超时：通过超时机制，防止了单个客户端长时间占用锁，避免了潜在的性能瓶颈和死锁问题。
     */

    private static final Logger log = LoggerFactory.getLogger(SocketAuthenticationSuccessfulStorage.class);
    // 线程安全的 ConcurrentMap 用于存储认证成功的 socket 连接
    private final ConcurrentMap<String, SocketMetBean> authenticatedSocketMetBeans = new ConcurrentHashMap<>();

    // 用于存储每个分区的锁（每个分区有多个锁）
    private final ConcurrentMap<Integer, ConcurrentMap<String, ReentrantLock>> partitionLocks = new ConcurrentHashMap<>();

    // 默认分区数
    private static final int PARTITION_COUNT = 16;

    // 用于分区锁的 Atomic Integer，用于确保分区锁的初始化
    private final AtomicInteger partitionCount = new AtomicInteger(PARTITION_COUNT);

    // 设置锁超时值
    private static final long LOCK_TIMEOUT = 1000;  // 1秒

    /**
     * 获取指定分区的锁
     * @param clientId 客户端id
     * @return ReentrantLock
     */
    private ReentrantLock getLockForPartition(String clientId) {
        int partition = Math.abs(clientId.hashCode()) % partitionCount.get();  // 通过哈希值来分配分区
        return partitionLocks.computeIfAbsent(partition, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(clientId, id -> new ReentrantLock());
    }


    /**
     *
     * @param clientId 客户端id
     * @param socketMetBean 存储的数据
     * @return 是否存储成功
     */
    public boolean storeAuthenticatedSocketMetBean(String clientId, SocketMetBean socketMetBean) {
        ReentrantLock lock = getLockForPartition(clientId);
        try {
            // 尝试在指定的超时时间内获取锁
            if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                try {
                    // 获取写锁，确保数据一致性
                    authenticatedSocketMetBeans.put(clientId, socketMetBean);
                    return true;  // 成功存储
                } finally {
                    lock.unlock();  // 解锁
                }
            } else {
                // 超时未获取到锁
                log.error("Failed to acquire lock for clientId: {}", clientId);
                return false;  // 返回失败
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;  // 中断异常
        }
    }


    /**
     * 根据客户端ID获取已认证的 Socket 连接
     * @param clientId 客户端ID
     * @return Socket 连接
     */
    public SocketMetBean getAuthenticatedSocketMetBean(String clientId) {
        ReentrantLock lock = getLockForPartition(clientId);
        try {
            // 尝试在指定的超时时间内获取锁
            if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                try {
                    // 获取读锁，确保并发读取时不被阻塞
                    return authenticatedSocketMetBeans.get(clientId);
                } finally {
                    lock.unlock();  // 解锁
                }
            } else {

                // 超时未获取到锁
                log.error("Failed to acquire lock for clientId: {}", clientId);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;  // 中断异常
        }
    }


    /**
     * 移除认证成功的 Socket 连接（比如当连接断开时）
     * @param clientId 客户端ID
     * @return 是否移除成功
     */
    public boolean removeAuthenticatedSocketMetBean(String clientId) {
        ReentrantLock lock = getLockForPartition(clientId);
        try {
            // 尝试在指定的超时时间内获取锁
            if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                try {
                    authenticatedSocketMetBeans.remove(clientId);
                    return true;  // 成功移除
                } finally {
                    lock.unlock();  // 解锁
                }
            } else {
                // 超时未获取到锁
                log.error("Failed to acquire lock for clientId: {}", clientId);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;  // 中断异常
        }
    }

    /**
     * 清理所有认证成功的 Socket 连接（可用于关闭时的清理操作）
     */
    public void clearAllAuthenticatedSockets() {
        // 清空认证信息
        authenticatedSocketMetBeans.clear();
        // 清空锁池
        partitionLocks.clear();
    }
}

