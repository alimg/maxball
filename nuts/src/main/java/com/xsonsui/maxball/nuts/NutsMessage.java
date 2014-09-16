package com.xsonsui.maxball.nuts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

public class NutsMessage implements Serializable {
    public String message;
    public InetAddress address;
    public int port;

    public Serializable data;

    public NutsMessage(String request, InetAddress address, int port) {
        this.message = request;
        this.address = address;
        this.port = port;
        this.data = null;
    }

    public static NutsMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        return (NutsMessage) new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
    }

    public static byte[] serialize(NutsMessage request) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outStream).writeObject(request);
        return outStream.toByteArray();
    }

}