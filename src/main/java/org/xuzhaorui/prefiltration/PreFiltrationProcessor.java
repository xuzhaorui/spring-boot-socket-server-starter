package org.xuzhaorui.prefiltration;

import org.xuzhaorui.annotation.SocketUrl;
import org.xuzhaorui.exception.pre.SocketMappingException;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.messageserialization.SocketMessageInterceptor;
import org.xuzhaorui.store.SocketMessageInfo;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.store.SocketMethodMappingRegistry;
import org.xuzhaorui.utils.AnnotationUtils;
import org.xuzhaorui.utils.ValidUtil;

/**
 * 过滤前处理器
 */
public class PreFiltrationProcessor {
    private final SocketMessageInterceptor socketMessageInterceptor;
    private final ValidUtil validUtil;
    private final SocketMethodMappingRegistry socketMethodMappingRegistry;
    private final SocketMessageInfoRegistry socketMessageInfoRegistry;

    public PreFiltrationProcessor(SocketMessageInterceptor socketMessageInterceptor, ValidUtil validUtil, SocketMethodMappingRegistry socketMethodMappingRegistry, SocketMessageInfoRegistry socketMessageInfoRegistry) {
        this.socketMessageInterceptor = socketMessageInterceptor;
        this.validUtil = validUtil;
        this.socketMethodMappingRegistry = socketMethodMappingRegistry;
        this.socketMessageInfoRegistry = socketMessageInfoRegistry;
    }






    public void process(SocketRequest request, SocketResponse response) throws Exception {
        // 反序列化并设置消息
        socketMessageInterceptor.deserialize(request,response);

        // 消息验证
        messageValidation(request);

        // url提取
        urlExtraction(request,response);
    }



    private void urlExtraction(SocketRequest request,SocketResponse response) throws RuntimeException {
        // 从消息中提取URL
        Object messageBean = request.getMessageBean();
        SocketMessageInfo socketMessageInfo = socketMessageInfoRegistry.getSocketMessageInfo(messageBean.getClass());
        String socketUrl =  AnnotationUtils.findValue(messageBean, SocketUrl.class,socketMessageInfo.getSocketUrlPaths());

        if (socketMethodMappingRegistry.containsMapping(socketUrl)){
            // 设置请求URL
            request.setRequestUrl(socketUrl);
            response.setRequestUrl(socketUrl);
        }else {
            throw new SocketMappingException("未找到对应的SocketMapping处理方法: "+ socketUrl);
        }

    }

    private void messageValidation(SocketRequest request)  throws RuntimeException{
        // 从请求中获取反序列化后的消息
        Object messageBean = request.getMessageBean();
        // 进行消息验证
        validUtil.validateAndProcessResponse(messageBean);
    }

}
