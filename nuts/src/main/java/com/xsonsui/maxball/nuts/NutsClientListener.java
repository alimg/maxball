package com.xsonsui.maxball.nuts;

import com.xsonsui.maxball.nuts.model.NetAddress;
import com.xsonsui.maxball.nuts.model.NutsMessage;

import java.net.InetAddress;

public interface NutsClientListener {

    void onConnected(InetAddress publicAddress, int publicPort);

    void onDisconnected();

    public void onResponse(NutsMessage response, NetAddress address, boolean p2p);

}
