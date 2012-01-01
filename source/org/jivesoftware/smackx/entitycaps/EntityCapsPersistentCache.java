package org.jivesoftware.smackx.entitycaps;

import org.jivesoftware.smackx.packet.DiscoverInfo;

public interface EntityCapsPersistentCache {
    /**
     * Add an DiscoverInfo to the persistent Cache
     * 
     * @param node
     * @param info
     */
    abstract void addDiscoverInfoByNodePersistent(String node, DiscoverInfo info);

    /**
     * Replay the Caches data into EntityCapsManager
     */
    abstract void replay();

    /**
     * Empty the Cache
     */
    abstract void emptyCache();
}
