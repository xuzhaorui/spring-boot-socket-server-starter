package org.xuzhaorui.server;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.exception.SocketExceptionHandler;
import org.xuzhaorui.exception.auth.SocketAuthenticationFailException;
import org.xuzhaorui.exception.pre.MessageSerializationException;
import org.xuzhaorui.filter.*;
import org.xuzhaorui.messageserialization.SocketMessageSerializer;
import org.xuzhaorui.socketcontext.SocketAuthentication;
import org.xuzhaorui.socketcontext.SocketContextHolder;
import org.xuzhaorui.store.SocketMessageInfo;
import org.xuzhaorui.url.AllowedUrlManager;
import org.xuzhaorui.store.FilterChainRegistry;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.prefiltration.PreFiltrationProcessor;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.readingmode.ReadWriteMode;
import org.xuzhaorui.scanner.SocketMappingHandler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * @author xzr
 */

public class SocketServer {


    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    /**
     * socket配置
     */
    private final SocketConfigProperties socketConfigProperties;

    /**
     * socket消息映射方法处理器,用于标记@SocketMapping的方法
     */
    private final SocketMappingHandler socketMappingHandler;
    /**
     * 允许的 url 管理器
     */
    private final AllowedUrlManager allowedUrlManager;

    private final SocketExceptionHandler exceptionHandler;

    private final FilterChainRegistry filterChainRegistry;

    private final ReadWriteMode readWriteMode;
    /**
     * 过滤前处理
     */
    private final PreFiltrationProcessor preFiltrationProcessor;
    /**
     * 处理socket消息的线程池
     */
    private final ThreadPoolTaskExecutor socketThreadPool;

    private final SocketMessageInfoRegistry socketMessageInfoRegistry;

    public SocketServer(SocketConfigProperties socketConfigProperties,  SocketMappingHandler socketMappingHandler, AllowedUrlManager allowedUrlManager, SocketExceptionHandler exceptionHandler, FilterChainRegistry filterChainRegistry, ReadWriteMode readWriteMode, PreFiltrationProcessor preFiltrationProcessor, ThreadPoolTaskExecutor socketThreadPool, SocketMessageInfoRegistry socketMessageInfoRegistry) {
        this.socketConfigProperties = socketConfigProperties;
        this.socketMappingHandler = socketMappingHandler;
        this.allowedUrlManager = allowedUrlManager;
        this.exceptionHandler = exceptionHandler;
        this.filterChainRegistry = filterChainRegistry;
        this.readWriteMode = readWriteMode;
        this.preFiltrationProcessor = preFiltrationProcessor;
        this.socketThreadPool = socketThreadPool;
        this.socketMessageInfoRegistry = socketMessageInfoRegistry;
    }

    /**
     * 热更新Socket服务端口所需属性
     */
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    // 启动 Socket 服务
    public synchronized  void init() {
        if (running) {
            log.warn("Socket 服务已经在运行，无法重复启动！");
            return;
        }

        int port = socketConfigProperties.getPort();
        int length = socketConfigProperties.getLength();
        try {
            serverSocket = new ServerSocket(port);
            int localPort = serverSocket.getLocalPort();
            log.info("服务端已启动，监听端口: {}", localPort);
            running = true;

            // 使用线程池来接收客户端连接
                socketThreadPool.execute(() -> {
                    while (running) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            log.info("客户端已连接: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                            socketThreadPool.execute(new ClientHandler(clientSocket,length));
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

    // 停止 Socket 服务
    public void stopServer() {
//        if (!running) {
//            log.warn("服务端已关闭，无法重复关闭！");
//            return;
//        }
//
//        running = false;
//        try {
//            if (serverSocket != null && !serverSocket.isClosed()) {
//                serverSocket.close();
//                log.info("服务端已关闭成功");
//            }
//        } catch (IOException e) {
//            log.error("关闭ServerSocket时发生异常: {}", e.getMessage());
//            throw new RuntimeException("关闭ServerSocket时发生异常: " + e.getMessage());
//        }

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
            socketThreadPool.shutdown();  // 优雅关闭线程池
//            if (!socketThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
//                socketThreadPool.shutdownNow(); // 强制关闭
//            }
        } catch (IOException e) {
            log.error("关闭ServerSocket或线程池时发生异常: {}", e.getMessage());
            throw new RuntimeException("关闭ServerSocket或线程池时发生异常: " + e.getMessage());
        }
    }
    // 项目启动时自动调用
    @PostConstruct
    public void startServerOnStartup() {
        init();
    }

    // 项目关闭时自动调用，确保服务正确关闭
    @PreDestroy
    public void onShutdown() {
        stopServer();
    }


    /**
     * Socket客户端处理程序
     */

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final int length;

        private ClientHandler(Socket clientSocket, int length) {
            this.clientSocket = clientSocket;
            this.length = length;
        }


        @Override
        public void run() {
            try (
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = clientSocket.getOutputStream();
            ) {

                Object clientMessage;
                while ((clientMessage = readWriteMode.read(inputStream, length)) != null) {

                    //  每个 ClientHandler 都会创建一个新的 SocketRequest 和 SocketResponse 实例。
                    //  这些对象是线程本地的，每个请求都会有独立的实例，通常来说这不会引发并发问题。
                    SocketRequest request = new SocketRequest(clientSocket, clientMessage);
                    SocketResponse response = new SocketResponse(clientSocket, outputStream);
                    try {
                        // 过滤前处理
                        preFiltrationProcessor.process(request, response);
                        // 获取URL对应的过滤链
                        String socketUrl = request.getRequestUrl();
                        // 获取反序列化的命中对象的SocketMessageInfo
                        // 判断是否放行
                        if (allowedUrlManager.isUrlAllowed(socketUrl)) {
                            Object returnValue = socketMappingHandler.handleRequest( request, response);
                            Object serialize = response.getHitSerializer().serialize(returnValue);
                            readWriteMode.write(outputStream,serialize,length);
                        } else {
                            List<SocketFilter> filters = filterChainRegistry.getFilters(socketUrl);
                            SocketFilterChain filterChain = new SocketFilterChain(filters);
                            // 执行过滤链
                            filterChain.doFilter(request, response);

                            SocketAuthentication socketAuthentication = SocketContextHolder.getSocketContext().getSocketAuthentication();
                            if (Objects.nonNull(socketAuthentication) && socketAuthentication.isAuthenticated()) {
                                Object returnValue = socketMappingHandler.handleRequest( request, response);
                                Object serialize = response.getHitSerializer().serialize(returnValue);
                                readWriteMode.write(outputStream,serialize,length);
                            } else {
                                throw  new SocketAuthenticationFailException("未认证");
                            }
                        }
                    } catch (Exception ex) {
                        // 处理异常
                        log.warn("处理Socket请求时发生异常: {}", ex.getMessage());
                        if (ex instanceof MessageSerializationException){
                            readWriteMode.write(response.getOutputStream(), "MessageSerializationException：" +  ex.getMessage(),length);
                        }else {
                            try {
                                exceptionHandler.handleException(request, response, ex);
                            } catch (IOException e) {
                                log.error("处理Socket请求发生异常的消息响应时写回异常: {}", e.getMessage());
                            } catch (Exception e) {
                                log.error("序列化异常: {}", e.getMessage());
                            }
                        }

                    }finally {
                        // 确保清理上下文，防止线程池中线程复用带来的数据污染
                        SocketContextHolder.clearSocketContext();
                    }
                }
            } catch (IOException e) {
                //   如果出现 IOException，应该首先记录日志
                log.error("处理客户端连接时出错: {}", e.getMessage());
            } finally {
                //  执行清理操作。防止资源泄漏，确保了在 finally 中关闭客户端连接。
                if (!clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        log.warn("关闭客户端连接时出错: {}", e.getMessage());
                    }
                }
//                try {
//                    clientSocket.close();
//                    log.info("客户端连接已关闭: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
//                } catch (IOException e) {
//                    log.warn("关闭客户端连接时出错: {}", e.getMessage());
//                }
            }
        }
    }



}
