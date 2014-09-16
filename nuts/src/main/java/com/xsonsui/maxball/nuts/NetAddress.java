package com.xsonsui.maxball.nuts;

import java.net.InetAddress;

public class NetAddress {
    public InetAddress srcAddress;
    public int srcPort;
    public NetAddress(InetAddress srcAddress, int srcPort){
        this.srcAddress = srcAddress;
        this.srcPort = srcPort;
    }

    @Override
    public int hashCode() {
        return srcAddress.hashCode()+srcPort;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NetAddress) {
            if (srcAddress.equals(((NetAddress) obj).srcAddress) && srcPort == ((NetAddress) obj).srcPort)
                return true;
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        return srcAddress.toString()+":"+srcPort;
    }
}