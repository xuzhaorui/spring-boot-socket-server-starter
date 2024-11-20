package org.xuzhaorui.socketcontext;

/**
 * 线程本地socket上下文持有者策略
 * @author xzr
 */
public final class ThreadLocalSocketContextHolderStrategy implements SocketContextHolderStrategy{


    private static final ThreadLocal<SocketContext> LOCAL_CONTEXT = new ThreadLocal<>();


    @Override
    public void clearSocketContext() {
        LOCAL_CONTEXT.remove();
    }

    @Override
    public SocketContext getSocketContext() {
        SocketContext ctx = LOCAL_CONTEXT.get();;
        if (ctx == null) {
            ctx = createEmptySocketContext();
            LOCAL_CONTEXT.set(ctx);
        }
        return ctx;
    }

    @Override
    public void setSocketContext(SocketContext context) {
        if (context == null){
            clearSocketContext();
        }else{
            LOCAL_CONTEXT.set(context);
        }
    }

    @Override
    public SocketContext createEmptySocketContext() {
        return new SocketContextImpl();
    }
}
