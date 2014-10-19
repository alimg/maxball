package com.xsonsui.maxball.nuts;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.xsonsui.maxball.serialization.NutsKryoFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NutsMessage implements KryoSerializable {
    public String message;
    public InetAddress address;
    public int port;

    public Object data;

    public transient InetAddress srcAddress;
    public transient int srcPort;
    public transient int sequenceNo;

    public NutsMessage(String request, InetAddress address, int port) {
        this.message = request;
        this.address = address;
        this.port = port;
        this.data = null;
    }

    public NutsMessage(String request, InetAddress address, int port, Object data) {
        this(request, address, port);
        this.data = data;
    }


    public static NutsMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bstream = null;
        GZIPInputStream gzstream = null;
        Input input = null;
        Kryo kryo = NutsKryoFactory.pool.borrow();
        try {
            bstream = new ByteArrayInputStream(data);
            gzstream = new GZIPInputStream(bstream);
            input = new Input(gzstream);
            NutsMessage message = kryo.readObject(input, NutsMessage.class);
            return message;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(e);
        } finally {
            if (bstream != null)
                bstream.close();
            if (gzstream != null)
                gzstream.close();
            if (input != null)
                input.close();
            NutsKryoFactory.pool.release(kryo);
        }
    }

    public static byte[] serialize(NutsMessage request) throws IOException, ClassNotFoundException {
        Kryo kryo = NutsKryoFactory.pool.borrow();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipStream = new GZIPOutputStream(outStream);
        Output output = new Output(gzipStream);
        kryo.writeObject(output, request);
        output.close();
        gzipStream.close();
        NutsKryoFactory.pool.release(kryo);
        return outStream.toByteArray();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(message);
        output.writeInt(port);
        kryo.writeClassAndObject(output, address);

        output.writeBoolean(data==null);
        if (data!=null) {
            kryo.writeClassAndObject(output, data);
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        message = input.readString();
        port = input.readInt();
        address = (InetAddress) kryo.readClassAndObject(input);
        if(!input.readBoolean()) {
            data = kryo.readClassAndObject(input);
        }
    }
}