package com.xsonsui.maxball.nuts;

import java.net.InetAddress;

public interface NutsClientListener {

    void onConnected(InetAddress publicAddress, int publicPort);

    void onDisconnected();

    public void onResponse(NutsMessage response, NetAddress address, boolean p2p);

}
