package org.jivesoftware.smackx.ping;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.DummyConnection;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.TestUtils;
import org.jivesoftware.smack.ThreadedDummyConnection;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.junit.Test;

public class ServerPingTest {
    private static final String TO = "juliet@capulet.lit/balcony";
    private static final String FROM = "romeo@example.com/resource";
    private static final String ID = "s2c1";

    private static Properties outputProperties = new Properties();
    {
        outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
    }

    /*
     * Stanza copied from spec
     */
    @Test
    public void validatePingStanzaXML() throws Exception {
        // @formatter:off
        String control = "<iq from='romeo@example.com/resource' to='juliet@capulet.lit/balcony' id='s2c1' type='get'>"
                + "<ping xmlns='urn:xmpp:ping'/>" + "</iq>";
        // @formatter:on

        Ping ping = new Ping(FROM, TO);
        ping.setPacketID(ID);

        assertXMLEqual(control, ping.toXML());
    }

//    @Test
//    public void checkProvider() throws Exception {
//        // @formatter:off
//        String control = "<iq from='capulet.lit' to='juliet@capulet.lit/balcony' id='s2c1' type='get'>"
//                + "<ping xmlns='urn:xmpp:ping'/>" + "</iq>";
//        // @formatter:on
//        DummyConnection con = new DummyConnection();
//        IQ pingRequest = PacketParserUtils.parseIQ(TestUtils.getIQParser(control), con);
//
//        assertTrue(pingRequest instanceof Ping);
//
//        con.processPacket(pingRequest);
//
//        Packet pongPacket = con.getSentPacket();
//        assertTrue(pongPacket instanceof IQ);
//
//        IQ pong = (IQ) pongPacket;
//        assertEquals("juliet@capulet.lit/balcony", pong.getFrom());
//        assertEquals("capulet.lit", pong.getTo());
//        assertEquals("s2c1", pong.getPacketID());
//        assertEquals(IQ.Type.RESULT, pong.getType());
//    }
}
