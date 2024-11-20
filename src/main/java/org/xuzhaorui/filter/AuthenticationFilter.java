package org.xuzhaorui.filter;

import org.xuzhaorui.annotation.FilterChain;
import org.xuzhaorui.exception.auth.SocketAuthenticationFailException;
import org.xuzhaorui.server.SocketMetBean;
import org.xuzhaorui.socketcontext.*;
import org.xuzhaorui.store.SocketAuthenticationSuccessfulStorage;

import java.net.Socket;
import java.util.Objects;

/**
 * 用于身份验证
 */
@FilterChain(order = 1)
public class AuthenticationFilter implements SocketFilter {
    private final SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage;
    public AuthenticationFilter(SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage) {
        this.socketAuthenticationSuccessfulStorage = socketAuthenticationSuccessfulStorage;
    }
    @Override
    public void doFilter(SocketRequest request, SocketResponse response, SocketFilterChain chain)
        throws Exception {
        // 获取客户端socket
        Socket clientSocket = request.getClientSocket();
        // 获取客户端ip和端口
        String  ipAndPort = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        // 由于认证接口被放行所以，根据ip和端口获取已认证的socketMetBean
        SocketMetBean authenticatedSocketMetBean = socketAuthenticationSuccessfulStorage.getAuthenticatedSocketMetBean(ipAndPort);
        // 如果已认证的socketMetBean不为空且当前socket上下文中没有socket认证信息
        SocketAuthentication socketAuthentication = SocketContextHolder.getSocketContext().getSocketAuthentication();
        if (Objects.nonNull(authenticatedSocketMetBean) && Objects.isNull(socketAuthentication)) {
            // 创建默认socket认证,并设置socket的信息与权限
            DefaultSocketAuthentication defaultSocketAuthentication = new DefaultSocketAuthentication(authenticatedSocketMetBean.getPrincipal());
            // 将默认socket认证设置到socket上下文中
            SocketContextHolder.getSocketContext().setSocketAuthentication(defaultSocketAuthentication);
            // 继续传递到下一个过滤器
            chain.doFilter(request, response);
        } else {
            // 抛出socket认证异常
            throw new SocketAuthenticationFailException("Authentication failed");
        }
    }
}
