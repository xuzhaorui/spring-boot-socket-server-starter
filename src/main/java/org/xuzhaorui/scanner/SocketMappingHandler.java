package org.xuzhaorui.scanner;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.annotation.*;
import org.xuzhaorui.exception.pre.SocketMappingException;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.store.SocketMethodMappingRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 映射处理程序
 */
public class SocketMappingHandler {


    private static final Logger log = LoggerFactory.getLogger(SocketMappingHandler.class);
    private final SocketMethodMappingRegistry socketMethodMappingRegistry;

    public SocketMappingHandler(SocketMethodMappingRegistry socketMethodMappingRegistry) {
        this.socketMethodMappingRegistry = socketMethodMappingRegistry;
    }


    public Object handleRequest(SocketRequest socketRequest,SocketResponse
            socketResponse) throws Exception {
        // 获取映射方法
        SocketHandlerMethod handlerMethod = socketMethodMappingRegistry.getHandler(socketRequest.getRequestUrl());

        if (handlerMethod != null) {
            // 直接调用Spring管理的Bean和方法
            Method method = handlerMethod.getMethod();
            Object controllerInstance = handlerMethod.getControllerInstance();
            Parameter[] parameters = handlerMethod.getParameters();
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                // 处理请求参数注入逻辑
                args[i] = resolveParameter(parameters[i], socketRequest, socketResponse);
            }
            return method.invoke(controllerInstance, args);
        } else {
            log.warn("未找到对应的SocketMapping处理方法: {}", socketRequest.getRequestUrl());
            throw new SocketMappingException("未找到对应的SocketMapping处理方法" +  socketRequest.getRequestUrl());
        }
    }
    private Object resolveParameter(Parameter parameter, SocketRequest socketRequest, SocketResponse socketResponse) throws NoSuchFieldException, IllegalAccessException {
        if (parameter.isAnnotationPresent(SocketRequestData.class)) {
            return resolveSocketRequestData(socketRequest, parameter);
        } else if (parameter.isAnnotationPresent(SocketContextKey.class)) {
            return resolveSocketContextKey();
        } else if (parameter.isAnnotationPresent(SocketContextSocket.class)) {
            return socketRequest.getClientSocket();
        } else if (parameter.getType().isInstance(socketRequest)) {
            return socketRequest;
        } else if (parameter.getType().isInstance(socketResponse)) {
            return socketResponse;
        }
        return null;
    }


    private Object resolveSocketRequestData(SocketRequest socketRequest, Parameter parameter) throws NoSuchFieldException, IllegalAccessException {
        SocketRequestData annotation = parameter.getAnnotation(SocketRequestData.class);
        String value = annotation.value();
        if (value.isEmpty()) {
            return socketRequest.getMessageBean();
        } else {
            return getCurrentObject(socketRequest.getMessageBean(), value);
        }
    }

    private Object resolveSocketContextKey() {
//        Object principal = SocketContextHolder.getSocketContext().getSocketAuthentication().getPrincipal();
        return null;
    }


    private static Object getCurrentObject(Object messageBean, String value) throws NoSuchFieldException, IllegalAccessException {
        String[] split ;
        try {
           split  = value.split("\\.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Object currentObject = messageBean;
        for (String fieldName : split) {
            Field declaredField = currentObject.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            currentObject = declaredField.get(currentObject);

        }
        return currentObject;
    }

}
