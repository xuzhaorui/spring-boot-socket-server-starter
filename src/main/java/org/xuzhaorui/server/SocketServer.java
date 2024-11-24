package org.xuzhaorui.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.xuzhaorui.exception.SocketExceptionHandler;
import org.xuzhaorui.prefiltration.PreFiltrationProcessor;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.readingmode.ReadWriteMode;
import org.xuzhaorui.scanner.SocketMappingHandler;
import org.xuzhaorui.store.FilterChainRegistry;
import org.xuzhaorui.url.AllowedUrlManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * SocketServer（核心服务类，用线程池处理 Socket 连接）
 */
public class SocketServer {
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    private final SocketConfigProperties socketConfigProperties;
    private final SocketMappingHandler socketMappingHandler;
    private final AllowedUrlManager allowedUrlManager;
    private final SocketExceptionHandler exceptionHandler;
    private final FilterChainRegistry filterChainRegistry;
    private final ReadWriteMode readWriteMode;
    private final PreFiltrationProcessor preFiltrationProcessor;
    private final ThreadPoolTaskExecutor socketThreadPool;


    public SocketServer(SocketConfigProperties socketConfigProperties, SocketMappingHandler socketMappingHandler,
                        AllowedUrlManager allowedUrlManager, SocketExceptionHandler exceptionHandler,
                        FilterChainRegistry filterChainRegistry, ReadWriteMode readWriteMode,
                        PreFiltrationProcessor preFiltrationProcessor, ThreadPoolTaskExecutor socketThreadPool) {
        this.socketConfigProperties = socketConfigProperties;
        this.socketMappingHandler = socketMappingHandler;
        this.allowedUrlManager = allowedUrlManager;
        this.exceptionHandler = exceptionHandler;
        this.filterChainRegistry = filterChainRegistry;
        this.readWriteMode = readWriteMode;
        this.preFiltrationProcessor = preFiltrationProcessor;
        this.socketThreadPool = socketThreadPool;
    }


    private ServerSocket serverSocket;
    /**
     * 保证serverSocket的状态在多线程之间的可见性
     */
    private volatile boolean running = false;
    @PostConstruct
    public synchronized void init() {
        if (running) {
            log.warn("Socket 服务已经在运行，无法重复启动！");
            return;
        }
        int port = socketConfigProperties.getPort();
        int length = socketConfigProperties.getLength();
        try {
            // 提供500个最大连接
            serverSocket = new ServerSocket(port,500);
            running = true;
            log.info("服务端已启动，监听端口: {}", port);

            socketThreadPool.execute(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        log.info("客户端已连接: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                        //为每个客户端新建资源类
                        SocketClientProcessor clientProcessor = new SocketClientProcessor(
                                clientSocket, preFiltrationProcessor, socketMappingHandler,
                                allowedUrlManager, filterChainRegistry, readWriteMode, exceptionHandler, length);
                        //使用线程池执行操纵资源类
                        socketThreadPool.execute(clientProcessor::process);
                    } catch (IOException e) {
                        if (running) {
                            log.error("客户端异常断开: {}", e.getMessage());
                        } else {
                            log.info("服务端已关闭，停止接收连接！");
                        }
                    }
                }
            });
        } catch (IOException e) {
            log.error("Socket 启动服务端时出错: {}", e.getMessage());
            throw new RuntimeException("Socket 启动服务端时出错: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopServer() {
        if (!running) {
            log.warn("服务端已关闭，无法重复关闭！");
            return;
        }

        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log.info("服务端已关闭成功");
            }
            socketThreadPool.shutdown(); // 优雅关闭线程池
        } catch (IOException e) {
            log.error("关闭ServerSocket时发生异常: {}", e.getMessage());
            throw new RuntimeException("关闭ServerSocket时发生异常: " + e.getMessage());
        }
    }
}
