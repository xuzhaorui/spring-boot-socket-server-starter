package org.xuzhaorui.socketcontext;





import java.io.Serializable;


/**
 * @author xzr
 **/
public interface SocketAuthentication extends Serializable {




    Object getPrincipal();

    Object getCredentials();


    boolean isAuthenticated();

    void setAuthenticated(boolean isAuthenticated) ;

}
