package org.xuzhaorui.scanner;


import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class SocketHandlerMethod {
    private final Object controllerInstance;
    private final Method method;
    private final Parameter[] parameters;

    public SocketHandlerMethod(Object controllerInstance, Method method,Parameter[] parameters) {
        this.controllerInstance = controllerInstance;
        this.method = method;
        this.parameters = parameters;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }

    public Method getMethod() {
        return method;
    }

    public Parameter[] getParameters() {
        return parameters;
    }
}
