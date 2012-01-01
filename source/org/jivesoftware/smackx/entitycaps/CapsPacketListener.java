package org.jivesoftware.smackx.entitycaps;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.CapsExtension;

class CapsPacketListener implements PacketListener {

    private EntityCapsManager manager;

    protected CapsPacketListener(EntityCapsManager manager) {
        this.manager = manager;
    }

    public void processPacket(Packet packet) {
        CapsExtension ext = (CapsExtension) packet.getExtension(
                CapsExtension.NODE_NAME, CapsExtension.XMLNS);

        String nodeVer = ext.getNode() + "#" + ext.getVer();
        String user = packet.getFrom();

        manager.addUserCapsNode(user, nodeVer);
    }
}