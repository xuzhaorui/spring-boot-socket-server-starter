package org.xuzhaorui.socketcontext;

/**
 * @author xzr
 **/
public interface SocketContextHolderStrategy {


    void clearSocketContext();


    SocketContext getSocketContext();


    void setSocketContext(SocketContext context);


    SocketContext createEmptySocketContext();
}
