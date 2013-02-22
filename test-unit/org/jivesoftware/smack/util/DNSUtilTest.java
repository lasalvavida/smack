package org.jivesoftware.smack.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


import org.jivesoftware.smack.util.dns.DNSJavaResolver;
import org.jivesoftware.smack.util.dns.DNSResolver;
import org.jivesoftware.smack.util.dns.HostAddress;
import org.jivesoftware.smack.util.dns.JavaxResolver;
import org.junit.Test;

public class DNSUtilTest {
    private static final String igniterealtimeDomain = "igniterealtime.org";
    private static final String igniterealtimeXMPPServer = "xmpp." + igniterealtimeDomain;
    private static final int igniterealtimeClientPort = 5222;
    private static final int igniterealtimeServerPort = 5269;
    
    @Test
    public void xmppClientDomainJavaXTest() {
        DNSResolver resolver = JavaxResolver.maybeGetInstance();
        assertNotNull(resolver);
        DNSUtil.setDNSResolver(resolver);
        xmppClientDomainTest();
    }
    
    @Test
    public void xmppServerDomainJavaXTest() {
        DNSResolver resolver = JavaxResolver.maybeGetInstance();
        assertNotNull(resolver);
        DNSUtil.setDNSResolver(resolver);
        xmppServerDomainTest();
    }
    
    @Test
    public void xmppClientDomainDNSJavaTest() {
        DNSResolver resolver = DNSJavaResolver.getInstance();
        assertNotNull(resolver);
        DNSUtil.setDNSResolver(resolver);
        xmppClientDomainTest();
    }
    
    @Test
    public void xmppServerDomainDNSJavaTest() {
        DNSResolver resolver = DNSJavaResolver.getInstance();
        assertNotNull(resolver);
        DNSUtil.setDNSResolver(resolver);
        xmppServerDomainTest();
    }
    
    private void xmppClientDomainTest() {
        HostAddress ha = DNSUtil.resolveXMPPDomain(igniterealtimeDomain);
        assertEquals(ha.getFQDN(), igniterealtimeXMPPServer);
        assertEquals(ha.getPort(), igniterealtimeClientPort);
    }
    
    private void xmppServerDomainTest() {
        HostAddress ha = DNSUtil.resolveXMPPServerDomain(igniterealtimeDomain);
        assertEquals(ha.getFQDN(), igniterealtimeXMPPServer);
        assertEquals(ha.getPort(), igniterealtimeServerPort);
    }

}
