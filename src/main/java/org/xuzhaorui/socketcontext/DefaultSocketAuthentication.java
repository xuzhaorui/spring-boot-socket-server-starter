package org.xuzhaorui.socketcontext;




/**
 * @author xzr
 * 每次Socket连接时，调用链相关数据
 */
public class DefaultSocketAuthentication extends AbstractSocketAuthentication{

    /**
     * 认证用户名 或者 用户信息与权限
     */
    private final Object principal;

    /**
     * 认证密码
     */
    private Object credentials;

    public DefaultSocketAuthentication(Object principal, Object credentials) {
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(false);

    }

    public DefaultSocketAuthentication(Object principal)  {
        this.principal =principal;
        super.setAuthenticated(true);
    }


    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }


}
