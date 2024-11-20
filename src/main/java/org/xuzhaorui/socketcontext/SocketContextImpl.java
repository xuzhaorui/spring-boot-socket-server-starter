package org.xuzhaorui.socketcontext;

import org.springframework.util.ObjectUtils;
/**
 * @author xzr
 */
public class SocketContextImpl implements SocketContext{

    // 定义SocketAuthentication类型的socketAuthentication变量
    private  SocketAuthentication socketAuthentication;

    // 无参构造函数
    public SocketContextImpl() {
    }

    // 有参构造函数，传入SocketAuthentication类型的参数
    public SocketContextImpl(SocketAuthentication socketAuthentication) {
        this.socketAuthentication = socketAuthentication;
    }

    // 重写equals方法，判断两个SocketContextImpl对象是否相等
    @Override
    public boolean equals(Object obj) {
        // 判断obj是否为SocketContextImpl类型
        if (obj instanceof SocketContextImpl) {
            // 将obj强制转换为SocketContextImpl类型
            SocketContextImpl other = (SocketContextImpl) obj;
            // 判断两个对象的socketAuthentication是否都为null
            if ((this.getSocketAuthentication() == null) && (other.getSocketAuthentication() == null)) {
                return true;
            }
            // 判断两个对象的socketAuthentication是否都不为null，并且相等
            if ((this.getSocketAuthentication() != null) && (other.getSocketAuthentication() != null)
                    && this.getSocketAuthentication().equals(other.getSocketAuthentication())) {
                return true;
            }
        }
        // 如果以上条件都不满足，则返回false
        return false;
    }

    // 重写hashCode方法，返回socketAuthentication的hashCode
    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.socketAuthentication);
    }
    // 获取socketAuthentication
    @Override
    public SocketAuthentication getSocketAuthentication() {
        return this.socketAuthentication;
    }

    // 设置socketAuthentication
    @Override
    public void setSocketAuthentication(SocketAuthentication authentication) {
        this.socketAuthentication  = authentication;
    }
}
