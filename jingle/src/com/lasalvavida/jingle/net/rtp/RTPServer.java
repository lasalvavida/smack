package com.lasalvavida.jingle.net.rtp;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Rob Taglang
 * @date 1/30/14
 */
public class RTPServer extends Thread {
    private HashSet<StreamHandler> handlers = new HashSet<StreamHandler>();
    private DatagramSocket socket = null;
    private AtomicBoolean killSwitch = new AtomicBoolean(false);
    private static final int BUFFER_SIZE = 25565;
    public RTPServer(InetAddress address, int port) throws IOException {
        socket = new DatagramSocket();
        socket.bind(new InetSocketAddress(address, port));
    }

    public void run() {
        if(socket != null) {
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
            while(!killSwitch.get()) {
                try {
                    socket.receive(receive);
                    byte[] bytes = receive.getData();
                    synchronized(handlers)
                    {
                        for(StreamHandler handler : handlers)
                        {
                            handler.acceptBytes(bytes);
                        }
                    }
                } catch (IOException e){
                    close();
                    e.printStackTrace();
                }
            }
        }
    }

    public void addStreamHandler(StreamHandler handler) {
        synchronized(handlers)
        {
            handlers.add(handler);
        }
    }

    public boolean removeStreamHandler(StreamHandler handler) {
        synchronized(handlers)
        {
            return handlers.remove(handler);
        }
    }

    public void close() {
        killSwitch.set(true);
        if(socket != null) {
            socket.close();
        }
    }
}
