package com.xsonsui.maxball.nuts;

import com.xsonsui.maxball.nuts.model.NutsMessage;
import com.xsonsui.maxball.nuts.model.NutsPacket;

import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by alim on 9/19/14.
 */
public class ReceiverThread extends Thread {

    private final DatagramSocket mSocket;
    private boolean running = true;
    private LinkedBlockingQueue<NutsMessage> mQueue = new LinkedBlockingQueue<NutsMessage>();
    private LRUMap<Long, ArrayList<NutsPacket>> packetCache = new LRUMap<Long, ArrayList<NutsPacket>>(150);

    public ReceiverThread(DatagramSocket socket) {
        mSocket = socket;
    }
    @Override
    public void run() {
        setName("ReceiverThread");
        NutsPacket header;
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[576], 576);
                mSocket.receive(packet);

                header = NutsPacket.processHeader(packet.getData());
                long seqHash = header.seqNo;
                seqHash = seqHash*100293971 + packet.getAddress().hashCode();
                seqHash = seqHash*100293971 + packet.getPort();
                if (!packetCache.containsKey(seqHash)) {
                    packetCache.put(seqHash, new ArrayList<NutsPacket>(header.parts));
                }
                ArrayList<NutsPacket> parts = packetCache.get(seqHash);
                parts.add(header);
                if (parts.size() == header.parts) {
                    int totalBytes = 0;
                    for (NutsPacket p: parts) {
                        totalBytes+=p.length;
                    }
                    ByteBuffer byteBuffer= ByteBuffer.allocate(totalBytes);
                    for (NutsPacket p: parts) {
                        byteBuffer.position(p.index*NutsConstants.MAX_PACKET_SIZE);
                        byteBuffer.put(p.data, NutsConstants.HEADER_SIZE, p.length);
                    }

                    NutsMessage message = NutsMessage.deserialize(byteBuffer.array());
                    message.srcAddress = packet.getAddress();
                    message.srcPort = packet.getPort();
                    message.sequenceNo = header.seqNo;
                    mQueue.offer(message);
                    packetCache.remove(seqHash);
                }
            } catch (SocketTimeoutException e) {
                interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NutsPacket.PacketIntegrityException e) {
                e.printStackTrace();
            }
        }
    }

    public NutsMessage receive(int timeout) throws SocketTimeoutException {
        try {
            NutsMessage packet = mQueue.poll(timeout, TimeUnit.SECONDS);
            if (packet == null) {
                throw new SocketTimeoutException("");
            }
            return packet;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new SocketTimeoutException("");
    }

    public void stopThread() {
        running = false;
    }

}
