package org.xuzhaorui.socketcontext;

public abstract class AbstractSocketAuthentication implements SocketAuthentication{

    private boolean authenticated = false;


    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
            this.authenticated = isAuthenticated;
    }
}
