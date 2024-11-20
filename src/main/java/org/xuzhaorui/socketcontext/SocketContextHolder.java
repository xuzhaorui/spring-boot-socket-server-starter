package org.xuzhaorui.socketcontext;

import org.xuzhaorui.socketcontext.SocketContext;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
/**
 * @author xzr Socket上下文持有者，参考spring security
 */
public class SocketContextHolder {
    /**
     * 存放SocketContext模式
     * 背景：Socket调用消息上下文可理解为一个徽章
     * 1、MODE_THREADLOCAL 为用一个房间的一个架子上的徽章   独立模式
     * 2、  xxx            为一个房间的一个架子上的多个徽章 继承模式
     * 3、     xxx         为一个房间多个架子上的多个徽章  全局共享
     */
    public static final String MODE_THREADLOCAL = "MODE_THREADLOCAL";
    private static final Logger log = LoggerFactory.getLogger(SocketContextHolder.class);

    /**
     * 暂定线程本地模式
     */
    private static String strategyName = "MODE_THREADLOCAL";

    private static SocketContextHolderStrategy strategy;

    private static int initializeCount = 0;

    static {
        initialize();
    }

    private static void initialize() {
        if (!StringUtils.hasText(strategyName)) {
            // 设置默认值
            strategyName = MODE_THREADLOCAL;
        }
        if (strategyName.equals(MODE_THREADLOCAL)) {
            strategy = new ThreadLocalSocketContextHolderStrategy();
        }
        else {
            // 尝试加载自定义策略
            try {
                Class<?> clazz = Class.forName(strategyName);
                Constructor<?> customStrategy = clazz.getConstructor();
                strategy = (SocketContextHolderStrategy) customStrategy.newInstance();
            }
            catch (Exception ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
        }
        initializeCount++;
    }

    /**
     * Explicitly从当前线程中清除Socket上下文值。
     */
    public static void clearSocketContext() {
        strategy.clearSocketContext();
    }


    public static SocketContext getSocketContext() {
        return strategy.getSocketContext();
    }


    public static int getInitializeCount() {
        return initializeCount;
    }


    public static void setSocketContext(SocketContext context) {
        strategy.setSocketContext(context);
    }

    /**
     更改首选策略。对于给定的 JVM，请勿多次调用此方法，因为它将重新初始化策略并对使用旧策略的任何现有线程产生不利影响。
     * @param strategyName 策略名称
     * used.
     */
    public static void setStrategyName(String strategyName) {
        SocketContextHolder.strategyName = strategyName;
        initialize();
    }

    /**
     * 允许检索上下文策略。
     * @return 存储Socket上下文的配置策略
     */
    public static SocketContextHolderStrategy getSocketContextHolderStrategy() {
        return strategy;
    }

    /**
     * 初始化 SocketContext
     * @return SocketContext
     */
    public static SocketContext createEmptySocketContext() {
        return strategy.createEmptySocketContext();
    }




    @Override
    public String toString() {
        return "SocketContextHolder[strategy='" + strategyName + "'; initializeCount=" + initializeCount + "]";
    }
}
