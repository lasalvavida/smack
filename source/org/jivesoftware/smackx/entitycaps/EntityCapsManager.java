/**
 * Copyright 2009 Jonas Ådahl.
 * Copyright 2011-2013 Florian Schmaus
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smackx.entitycaps;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.entitycaps.cache.EntityCapsPersistentCache;
import org.jivesoftware.smackx.entitycaps.packet.CapsExtension;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo.Feature;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Keeps track of entity capabilities.
 */
public class EntityCapsManager {

    public static final String NAMESPACE = "http://jabber.org/protocol/caps";
    public static final String ELEMENT = "c";

    public static final String SHA1 = "sha-1";
    public static final String SHA1_CAPS = "SHA-1";

    private static final String ENTITY_NODE = "http://www.igniterealtime.org/projects/smack/";
    private static EntityCapsPersistentCache persistentCache;
    
    private static Map<Connection, EntityCapsManager> instances =
            Collections.synchronizedMap(new WeakHashMap<Connection, EntityCapsManager>());
    
    /**
     * Map of (node + '#" + hash algorithm) to DiscoverInfo data
     */
    private static Map<String,DiscoverInfo> caps =
        new ConcurrentHashMap<String,DiscoverInfo>();

    /**
     * Map of Full JID -&gt; DiscoverInfo/null.
     * In case of c2s connection the key is formed as user@server/resource (resource is required)
     * In case of link-local connection the key is formed as user@host (no resource)
     * In case of a server or component the key is formed as domain
     */
    private static Map<String,String> userCaps =
        new ConcurrentHashMap<String,String>();
    
    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                if (connection instanceof XMPPConnection)
                    new EntityCapsManager(connection);
            }
        });
    }

    private Connection connection;
    private ServiceDiscoveryManager sdm;
    private boolean entityCapsEnabled;
    private String currentCapsVersion;
    private boolean presenceSend = false;

    // CapsVerListeners gets notified when the version string is changed.
    private Set<CapsVerListener> capsVerListeners =
        new CopyOnWriteArraySet<CapsVerListener>();


    /**
     * Add DiscoverInfo to the database.
     *
     * @param node The node name. Could be for example "http://psi-im.org#q07IKJEyjvHSyhy//CH0CxmKi8w=".
     * @param info DiscoverInfo for the specified node.
     */
    public static void addDiscoverInfoByNode(String node, DiscoverInfo info) {
        // Remove the non relevant data
        info.setFrom(null);
        info.setTo(null);
        info.setPacketID(null);

        caps.put(node, info);

        if (persistentCache != null)
            persistentCache.addDiscoverInfoByNodePersistent(node, info);
    }

    private EntityCapsManager(Connection connection) {
        this.connection = connection;
        this.sdm = ServiceDiscoveryManager.getInstanceFor(connection);
        init();
    }

    @SuppressWarnings("static-access")
    private void init() {
        instances.put(connection, this);
        connection.addConnectionListener(new ConnectionListener() {
            public void connectionClosed() {
                // Unregister this instance since the connection has been closed
                instances.remove(connection);
            }

            public void connectionClosedOnError(Exception e) {
                // ignore
            }

            public void reconnectionFailed(Exception e) {
                // ignore
            }

            public void reconnectingIn(int seconds) {
                // ignore
            }

            public void reconnectionSuccessful() {
                // ignore
            }
        });
        
        sdm.setEntityCapsManager(this);
        
        calculateEntityCapsVersion(getOwnDiscoverInfo(), sdm.getIdentityType(), sdm.getIdentityName(),
                sdm.getExtendedInfo());
        addCapsVerListener(new CapsVerListener() {
            @Override
            public void capsVerUpdated(String capsVer) {
                // Send an empty presence, and let the packet intercepter
                // add a <c/> node to it.
                // See http://xmpp.org/extensions/xep-0115.html#advertise
                // We only send a presence packet if there was already one send
                // to respect ConnectionConfiguration.isSendPresence()
                if (connection.isAuthenticated() && presenceSend) {
                    Presence presence = new Presence(Presence.Type.available);
                    connection.sendPacket(presence);
                }
            }
        });


        if (SmackConfiguration.autoEnableEntityCaps())
            enableEntityCaps();
        
        PacketFilter packetFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PacketExtensionFilter(
                ELEMENT, NAMESPACE));
        connection.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                CapsExtension ext = (CapsExtension) packet.getExtension(EntityCapsManager.ELEMENT,
                        EntityCapsManager.NAMESPACE);

                String user = packet.getFrom();
                String nodeVer = ext.getNode() + "#" + ext.getVer();

                userCaps.put(user, nodeVer);
            }

        }, packetFilter);
        
        packetFilter = new PacketTypeFilter(Presence.class);
        connection.addPacketSendingListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                presenceSend = true;
            }
            
        }, packetFilter);
        
        // Intercept presence packages and add caps data when intended.
        // XEP-0115 specifies that a client SHOULD include entity capabilities
        // with every presence notification it sends.
        PacketFilter capsPacketFilter = new PacketTypeFilter(Presence.class);
        PacketInterceptor packetInterceptor = new PacketInterceptor() {
            public void interceptPacket(Packet packet) {
                if (entityCapsEnabled) {
                    String ver = getCapsVersion();
                    CapsExtension caps = new CapsExtension(getNode(), ver, "sha-1");
                    packet.addExtension(caps);
                }
            }
        };
        connection.addPacketInterceptor(packetInterceptor, capsPacketFilter);
    }
    
    public static synchronized EntityCapsManager getInstanceFor(Connection connection) {
        // For testing purposed forbid EntityCaps for non XMPPConnections
        // it may work on BOSH connections too
        if (! (connection instanceof XMPPConnection))
            return null;
        
        EntityCapsManager entityCapsManager = instances.get(connection);

        if (entityCapsManager == null) {
            entityCapsManager = new EntityCapsManager(connection);
        }

        return entityCapsManager;
    }
    
    public void enableEntityCaps() {
        // Add Entity Capabilities (XEP-0115) feature node.
        sdm.addFeature(NAMESPACE);
        entityCapsEnabled = true;
    }
    
    public void disableEntityCaps() {
        entityCapsEnabled = false;
        sdm.removeFeature(NAMESPACE);
    }
    
    public boolean entityCapsEnabled() {
        return entityCapsEnabled;
    }

    /**
     * Remove a record telling what entity caps node a user has.
     *
     * @param user the user (Full JID)
     */
    public void removeUserCapsNode(String user) {
        userCaps.remove(user);
    }

    /**
     * Get the Node version (node#ver) of a user.
     * Returns a String or null if EntiyCapsManager does not have any information.
     *
     * @param user the user (Full JID)
     * @return the node version (node + '#' + ver) or null
     */
    public static String getNodeVersionByUser(String user) {
        return userCaps.get(user);
    }

    /**
     * Get the discover info given a user name. The discover
     * info is returned if the user has a node#ver associated with
     * it and the node#ver has a discover info associated with it.
     *
     * @param user user name (Full JID)
     * @return the discovered info
     */
    public static DiscoverInfo getDiscoverInfoByUser(String user) {
        String capsNode = userCaps.get(user);
        if (capsNode == null)
            return null;

        return getDiscoverInfoByNode(capsNode);
    }

    /**
     * Get our own caps version. The version depends on the enabled features.
     * A caps version looks like '66/0NaeaBKkwk85efJTGmU47vXI='
     *
     * @return our own caps version
     */
    public String getCapsVersion() {
        return currentCapsVersion;
    }

    /**
     * Get our own entity node.
     * This is the hard coded String "http://www.igniterealtime.org/projects/smack/"
     *
     * @return our own entity node.
     */
    public String getNode() {
        return ENTITY_NODE;
    }

    /**
     * Retrieve DiscoverInfo for a specific node.
     *
     * @param node The node name.
     * @return The corresponding DiscoverInfo or null if none is known.
     */
    public static DiscoverInfo getDiscoverInfoByNode(String node) {
        return caps.get(node);
    }
    
    /**
     * Returns the discovered information of a given XMPP entity addressed by its JID
     * if it's known by the entity caps manager.
     *
     * @param entityID the address of the XMPP entity
     * @return the disovered info or null if no such info is available from the
     * entity caps manager.
     * @throws XMPPException if the operation failed for some reason.
     */
    public static DiscoverInfo discoverInfoByCaps(String entityID) throws XMPPException {
        DiscoverInfo info = EntityCapsManager.getDiscoverInfoByUser(entityID);

        if (info != null) {
            DiscoverInfo newInfo = info.clone();
            newInfo.setFrom(entityID);
            return newInfo;
        }
        else {
            return null;
        }
    }

    /**
     * Set the persistent cache implementation
     * 
     * @param cache
     * @throws IOException
     */
    public static void setPersistentCache(EntityCapsPersistentCache cache) throws IOException {
        if (persistentCache != null)
            throw new IllegalStateException("Entity Caps Persistent Cache was already set");
        persistentCache = cache;
        persistentCache.replay();
    }

    /**
     * Get a DiscoverInfo for the current entity caps node.
     *
     * @return a DiscoverInfo for the current entity caps node
     */
    public DiscoverInfo getOwnDiscoverInfo() {
        DiscoverInfo di = new DiscoverInfo();
        di.setType(IQ.Type.RESULT);
        di.setNode(getNode() + "#" + getCapsVersion());

        // Add discover info
        sdm.addDiscoverInfoTo(di);

        return di;
    }

     void addCapsVerListener(CapsVerListener listener) {
        capsVerListeners.add(listener);

        if (currentCapsVersion != null)
            listener.capsVerUpdated(currentCapsVersion);
    }

    public void removeCapsVerListener(CapsVerListener listener) {
        capsVerListeners.remove(listener);
    }

    private void notifyCapsVerListeners() {
        for (CapsVerListener listener : capsVerListeners) {
            listener.capsVerUpdated(currentCapsVersion);
        }
    }


    /**
     * Calculates the entity caps version of the current connection with information from the ServiceDiscoveryManager
     * and notifies the listeners about a Caps version change.
     * 
     * 
     * @param discoverInfo
     * @param identityType
     * @param identityName
     * @param extendedInfo
     */
    public void calculateEntityCapsVersion(DiscoverInfo discoverInfo,
            String identityType,
            String identityName,
            DataForm extendedInfo) {

        String capsVersionHashed = generateVerificationString(discoverInfo, identityType, identityName, extendedInfo);
        addDiscoverInfoByNode(getNode() + '#' + capsVersionHashed, discoverInfo);
        currentCapsVersion = capsVersionHashed;
        notifyCapsVerListeners();
    }
    
    /**
     * @see <a href="http://xmpp.org/extensions/xep-0115.html#ver">XEP-115 Verification String</a>
     *
     * @param discoverInfo
     * @param identityType
     * @param identityName
     * @param extendedInfo
     * @return
     */
    private static String generateVerificationString(DiscoverInfo discoverInfo,
            String identityType,
            String identityName,
            DataForm extendedInfo) {

        // Initialize an empty string S.
        String s = "";

        // Add identity
        s += "client/" + identityType + "//" + identityName + "<";

        // Add features
        SortedSet<String> features = new TreeSet<String>();
        for (Iterator<Feature> it = discoverInfo.getFeatures(); it.hasNext();)
            features.add(it.next().getVar());

        for (String f : features) {
            s += f + "<";
        }

        if (extendedInfo != null) {
            synchronized (extendedInfo) {
                SortedSet<FormField> fs = new TreeSet<FormField>(
                        new Comparator<FormField>() {
                            public int compare(FormField f1, FormField f2) {
                                return f1.getVariable().compareTo(f2.getVariable());
                            }
                        });

                FormField ft = null;

                for (Iterator<FormField> i = extendedInfo.getFields(); i.hasNext();) {
                    FormField f = i.next();
                    if (!f.getVariable().equals("FORM_TYPE")) {
                        fs.add(f);
                    }
                    else {
                        ft = f;
                    }
                }

                // Add FORM_TYPE values
                if (ft != null) {
                    s += formFieldValuesToCaps(ft.getValues());
                }

                // Add the other values
                for (FormField f : fs) {
                    s += f.getVariable() + "<";
                    s += formFieldValuesToCaps(f.getValues());
                }
            }
        }

        try {
            MessageDigest md = MessageDigest.getInstance(SHA1_CAPS);
            byte[] digest = md.digest(s.getBytes());
            return Base64.encodeBytes(digest);
        }
        catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }

    private static String formFieldValuesToCaps(Iterator<String> i) {
        String s = "";
        SortedSet<String> fvs = new TreeSet<String>();
        for (; i.hasNext();) {
            fvs.add(i.next());
        }
        for (String fv : fvs) {
            s += fv + "<";
        }
        return s;
    }
}
