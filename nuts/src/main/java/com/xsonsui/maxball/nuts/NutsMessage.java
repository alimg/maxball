package com.xsonsui.maxball.nuts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public NutsMessage(String request, InetAddress address, int port, Serializable data) {
        this(request, address, port);
        this.data = data;
    }

    public static NutsMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try {
            return (NutsMessage) new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data))).readObject();
        } catch (ArrayIndexOutOfBoundsException e){
            throw new IOException(e);
        }
    }

    public static byte[] serialize(NutsMessage request) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipStream = new GZIPOutputStream(outStream);
        new ObjectOutputStream(gzipStream).writeObject(request);
        gzipStream.close();
        return outStream.toByteArray();
    }

}