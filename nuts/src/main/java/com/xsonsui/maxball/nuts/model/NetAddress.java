package com.xsonsui.maxball.nuts.model;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;

public class NetAddress implements Serializable {
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