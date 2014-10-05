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

    public transient InetAddress srcAddress;
    public transient int srcPort;
    public transient int sequenceNo;

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
        ByteArrayInputStream bstream = null;
        GZIPInputStream gzstream = null;
        ObjectInputStream obstream = null;
        try {

            bstream = new ByteArrayInputStream(data);
            gzstream = new GZIPInputStream(bstream);
            obstream = new ObjectInputStream(gzstream);
            NutsMessage message = (NutsMessage) obstream.readObject();
            return message;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(e);
        } finally {
            if (bstream != null)
                bstream.close();
            if (gzstream != null)
                gzstream.close();
            if (obstream != null)
                obstream.close();
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