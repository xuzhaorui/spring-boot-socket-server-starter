package org.xuzhaorui.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.exception.SocketExceptionHandler;
import org.xuzhaorui.exception.auth.SocketAuthenticationFailException;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.prefiltration.PreFiltrationProcessor;
import org.xuzhaorui.readingmode.ReadWriteMode;
import org.xuzhaorui.scanner.SocketMappingHandler;
import org.xuzhaorui.socketcontext.SocketAuthentication;
import org.xuzhaorui.socketcontext.SocketContextHolder;
import org.xuzhaorui.store.FilterChainRegistry;
import org.xuzhaorui.url.AllowedUrlManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import org.xuzhaorui.filter.*;

/**
 * 处理客户端连接或消息的资源类
 */
public class SocketClientProcessor {

    private static final Logger log = LoggerFactory.getLogger(SocketClientProcessor.class);
    private final Socket socket;
    private final PreFiltrationProcessor preFiltrationProcessor;
    private final SocketMappingHandler socketMappingHandler;
    private final AllowedUrlManager allowedUrlManager;
    private final FilterChainRegistry filterChainRegistry;
    private final ReadWriteMode readWriteMode;
    private final SocketExceptionHandler exceptionHandler;
    private final int length;


    public SocketClientProcessor(Socket socket, PreFiltrationProcessor preFiltrationProcessor,
                                 SocketMappingHandler socketMappingHandler, AllowedUrlManager allowedUrlManager,
                                 FilterChainRegistry filterChainRegistry, ReadWriteMode readWriteMode,
                                 SocketExceptionHandler exceptionHandler, int length) {
        this.socket = socket;
        this.preFiltrationProcessor = preFiltrationProcessor;
        this.socketMappingHandler = socketMappingHandler;
        this.allowedUrlManager = allowedUrlManager;
        this.filterChainRegistry = filterChainRegistry;
        this.readWriteMode = readWriteMode;
        this.exceptionHandler = exceptionHandler;
        this.length = length;
    }

    /**
     * 处理客户端请求
     */
    public void process() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {

            Object clientMessage;
            while ((clientMessage = readWriteMode.read(inputStream, length)) != null) {
                // 无需加锁，因为 SocketRequest 和 SocketResponse 是每个线程自带的（线程局部）
                InetAddress inetAddress = socket.getInetAddress();
                int port = socket.getPort();
                SocketRequest request = new SocketRequest(clientMessage,inetAddress,port);
                SocketResponse response = new SocketResponse(outputStream);
                try {
                    preFiltrationProcessor.process(request, response);
                    handleRequest(request, response, outputStream);
                } catch (Exception ex) {
                    handleException(request, response, ex);
                } finally {
                    SocketContextHolder.clearSocketContext(); // 清理上下文，防止线程池中线程复用时污染数据
                }
            }
        } catch (IOException e) {
            log.error("处理客户端连接时出错: {}", e.getMessage());
        } finally {
            closeSocket();
        }
    }

    /**
     * 处理 请求
     * @param request 请求
     * @param response 响应
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    private void handleRequest(SocketRequest request, SocketResponse response, OutputStream outputStream) throws Exception {
        String socketUrl = request.getRequestUrl();
        if (allowedUrlManager.isUrlAllowed(socketUrl)) {
            Object returnValue = socketMappingHandler.handleRequest(request, response);
            Object serialize = response.getHitSerializer().serialize(returnValue);
            readWriteMode.write(outputStream, serialize, length);
        } else {
            List<SocketFilter> filters = filterChainRegistry.getFilters(socketUrl);
            SocketFilterChain filterChain = new SocketFilterChain(filters);
            filterChain.doFilter(request, response);
            handleAuthenticatedRequest(request, response, outputStream);
        }
    }

    /**
     *  处理已验证的请求
     * @param request 请求
     * @param response 响应
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    private void handleAuthenticatedRequest(SocketRequest request, SocketResponse response, OutputStream outputStream) throws Exception {
        SocketAuthentication socketAuthentication = SocketContextHolder.getSocketContext().getSocketAuthentication();
        if (socketAuthentication != null && socketAuthentication.isAuthenticated()) {
            Object returnValue = socketMappingHandler.handleRequest(request, response);
            Object serialize = response.getHitSerializer().serialize(returnValue);
            readWriteMode.write(outputStream, serialize, length);
        } else {
            throw new SocketAuthenticationFailException("未认证");
        }
    }

    /**
     * 处理异常
     * @param request 请求
     * @param response 响应
     * @param ex 异常
     */
    private void handleException(SocketRequest request, SocketResponse response, Exception ex) {
        log.warn("处理Socket请求时发生异常: {}", ex.getMessage());
        try {
            exceptionHandler.handleException(request, response, ex);
        } catch (Exception e) {
            log.error("处理Socket请求时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 关闭Socket连接
     */
    private void closeSocket() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                log.warn("关闭客户端连接时出错: {}", e.getMessage());
            }
        }
    }
}
