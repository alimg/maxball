package com.xsonsui.maxball.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class NutsKryoFactory implements KryoFactory {

    public static KryoPool pool = new KryoPool.Builder(new NutsKryoFactory())
            .softReferences()
            .build();

    @Override
    public Kryo create() {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        kryo.register(Inet4Address.class, new Serializer() {
            @Override
            public void write(Kryo kryo, Output output, Object object) {
                output.writeBoolean(object == null);
                if (object != null) {
                    output.write(((Inet4Address) object).getAddress());
                }
            }

            @Override
            public Object read(Kryo kryo, Input input, Class type) {
                if (input.readBoolean())
                    return null;
                byte bytes[] = new byte[4];
                input.read(bytes);
                try {
                    return Inet4Address.getByAddress(bytes);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return kryo;
    }
}
