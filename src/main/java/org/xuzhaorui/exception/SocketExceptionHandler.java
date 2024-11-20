package org.xuzhaorui.exception;

import org.xuzhaorui.annotation.SocketCode;
import org.xuzhaorui.annotation.SocketMsg;
import org.xuzhaorui.exception.auth.SocketAuthenticationException;
import org.xuzhaorui.exception.auth.SocketAuthenticationFailureHandler;
import org.xuzhaorui.exception.pre.PreAuthenticationException;
import org.xuzhaorui.exception.pre.PreAuthenticationFailureHandler;
import org.xuzhaorui.exception.soc.SocketNativeException;
import org.xuzhaorui.exception.soc.SocketNativeHandler;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.store.SocketMessageInfo;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.readingmode.ReadWriteMode;
import org.xuzhaorui.utils.AnnotationUtils;

import java.io.IOException;

public class SocketExceptionHandler {
    private final ReadWriteMode readWriteMode;
    private final SocketConfigProperties socketConfigProperties;

    private final SocketAuthenticationFailureHandler failureHandler;
    private final PreAuthenticationFailureHandler preAuthenticationFailureHandler;
//    private final SocketAccessDeniedHandler accessDeniedHandler;
    private final SocketNativeHandler socketNativeHandler;
    private final SocketMessageInfoRegistry socketMessageInfoRegistry;

    public SocketExceptionHandler(ReadWriteMode readWriteMode, SocketConfigProperties socketConfigProperties, SocketAuthenticationFailureHandler failureHandler, PreAuthenticationFailureHandler preAuthenticationFailureHandler, SocketNativeHandler socketNativeHandler, SocketMessageInfoRegistry socketMessageInfoRegistry) {
        this.readWriteMode = readWriteMode;
        this.socketConfigProperties = socketConfigProperties;
        this.failureHandler = failureHandler;
        this.preAuthenticationFailureHandler = preAuthenticationFailureHandler;
        this.socketNativeHandler = socketNativeHandler;
        this.socketMessageInfoRegistry = socketMessageInfoRegistry;
    }




    public void handleException(SocketRequest request, SocketResponse response, Exception exception) throws IOException {

            Throwable targetException = exception.getCause();
            if (targetException instanceof Exception) {
                exception = (Exception) targetException;
            }
            if (exception instanceof SocketAuthenticationException) {
                // 认证异常
                failureHandler.handleAuthenticationFailure(request,response,(SocketAuthenticationException) exception);
            } else if (exception instanceof SocketNativeException) {
                // socket native 异常
                socketNativeHandler.handleSocketNative(request,response,(SocketNativeException) exception);
            } else if (exception instanceof PreAuthenticationException) {
                //  预认证异常
                preAuthenticationFailureHandler.handlePreAuthenticationFailure(request,response,(PreAuthenticationException)exception);
            } else {
                response.setCode(601);
                response.setMessage("Unknown exception: " + exception.getMessage());
            }

            Object messageBean = request.getMessageBean();
            Class<?> hitBeanClass = messageBean.getClass();
            SocketMessageInfo socketMessageInfo = socketMessageInfoRegistry.getSocketMessageInfo(hitBeanClass);
            AnnotationUtils.modifyMarkAnnotationValue(messageBean, SocketCode.class,socketMessageInfo.getSocketCodePaths(),response.getCode());
            AnnotationUtils.modifyMarkAnnotationValue(messageBean, SocketMsg.class,socketMessageInfo.getSocketMsgPaths(),response.getMessage());
            Object serialize = socketMessageInfo.getSocketMessageSerializer().serialize(messageBean);
            readWriteMode.write(response.getOutputStream(), serialize,socketConfigProperties.getLength());
        }

}
