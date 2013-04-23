package org.jivesoftware.smack.keepalive;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

public class KeepAliveManager {

    private static ScheduledExecutorService periodicKeepAliveExecutorService;
    
    static {
        if (SmackConfiguration.getKeepAliveInterval() > 0) {
            Connection.addConnectionCreationListener(new ConnectionCreationListener() {
                public void connectionCreated(Connection connection) {
                    KeepAliveManager.getInstance(connection);
                }
            });
        }
    }
    
    private static Map<Connection, KeepAliveManager> instances = Collections.synchronizedMap(new WeakHashMap<Connection, KeepAliveManager>());
    
    public static synchronized KeepAliveManager getInstance(Connection connection) {
        KeepAliveManager keepAliveManager = instances.get(connection);
        
        if (keepAliveManager == null) {
            keepAliveManager = new KeepAliveManager(connection);
        }
        
        return keepAliveManager;
    }
    
    private WeakReference<Connection> weakRefConnection;
    private int keepAliveInterval = -1;
    
    private KeepAliveManager(Connection connection) {
        this.weakRefConnection = new WeakReference<Connection>(connection);
        init();
    }
    
    private void init() {
        if (periodicKeepAliveExecutorService == null) {
            periodicKeepAliveExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread keepAliveThread = new Thread(runnable, "Smack KeepAlive");
                    keepAliveThread.setDaemon(true);
                    return keepAliveThread;
                }
            });
        }
        setKeepAliveInterval(SmackConfiguration.getKeepAliveInterval());
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;

        // Schedule a new keep alive if it was previously disabled
        if (this.keepAliveInterval <= 0 && keepAliveInterval > 0) {
            maybeScheduleKeepAlive(keepAliveInterval);
        }
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    private synchronized void maybeScheduleKeepAlive(long interval) {
        if (keepAliveInterval > 0) {
            periodicKeepAliveExecutorService.schedule(new Runnable() {
                
                @Override
                public void run() {
                    Connection connection = weakRefConnection.get();
                    if (connection == null)
                        return;
                    
                    if (!connection.isEstablished()) {
                        // TODO This is not really ideal, we should not schedule a new instead
                        // instead we should use something like a connection established (and auth) method
                        // to re-call maybeScheduleKeepAlive() in case the connection was/is disconnected
                        maybeScheduleKeepAlive(keepAliveInterval);
                        return;
                    }

                    if (connection instanceof XMPPConnection) {
                        long lastWriteActivity = ((XMPPConnection)connection).getLastReceivedTimestamp();
                        long delta = System.currentTimeMillis() - lastWriteActivity;
                        if (delta < keepAliveInterval) {
                            maybeScheduleKeepAlive(delta);
                            return;
                        }
                    }

                    IQ keepAlivePing = new IQ() {
                        @Override
                        public String getChildElementXML() {
                            return "<ping xmlns='urn:xmpp:ping/>";
                        }
                        
                    };
                    keepAlivePing.setFrom(connection.getUser());
                    keepAlivePing.setTo(connection.getServiceName());
                    keepAlivePing.setType(IQ.Type.GET);
                    keepAlivePing.setPacketID(Packet.nextID());
                    connection.sendPacket(keepAlivePing);
                    maybeScheduleKeepAlive(keepAliveInterval);
                }

            }, interval, TimeUnit.MILLISECONDS);
        }
    }
}
