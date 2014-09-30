package com.xsonsui.maxball.nuts;

import java.net.InetAddress;

/**
 * Created by alim on 9/11/14.
 */
public class HostModeClientMain {

    public static void main(String args[]) {
        new NutsHostModeClient(new NutsClientListener() {
            @Override
            public void onConnected(InetAddress publicAddress, int publicPort) {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onResponse(NutsMessage response, NetAddress address, boolean b) {

            }
        }).start();
    }
}
