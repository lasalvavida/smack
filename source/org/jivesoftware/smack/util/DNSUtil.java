/**
 * $Revision: 1456 $
 * $Date: 2005-06-01 22:04:54 -0700 (Wed, 01 Jun 2005) $
 *
 * Copyright 2003-2005 Jive Software.
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

package org.jivesoftware.smack.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.util.dns.ReflectionDNSJava;
import org.jivesoftware.smack.util.dns.DNSResolver;
import org.jivesoftware.smack.util.dns.HostAddress;
import org.jivesoftware.smack.util.dns.ReflectionJavaxDNS;
import org.jivesoftware.smack.util.dns.SRVRecord;


/**
 * Utilty class to perform DNS lookups for XMPP services.
 *
 * @author Matt Tucker
 */
public class DNSUtil {

    /**
     * Create a cache to hold the 100 most recently accessed DNS lookups for a period of
     * 10 minutes.
     */
    private static Map<String, List<HostAddress>> cache = new Cache<String, List<HostAddress>>(100, 1000*60*10);

    private static DNSResolver dnsResolver = null;

    public static void setDNSResolver(DNSResolver resolver) {
        dnsResolver = resolver;
    }

    public static DNSResolver getDNSResolver() {
        return dnsResolver;
    }

    /**
     * Returns the host name and port that the specified XMPP server can be
     * reached at for client-to-server communication. A DNS lookup for a SRV
     * record in the form "_xmpp-client._tcp.example.com" is attempted, according
     * to section 14.4 of RFC 3920. If that lookup fails, a lookup in the older form
     * of "_jabber._tcp.example.com" is attempted since servers that implement an
     * older version of the protocol may be listed using that notation. If that
     * lookup fails as well, it's assumed that the XMPP server lives at the
     * host resolved by a DNS lookup at the specified domain on the default port
     * of 5222.<p>
     *
     * As an example, a lookup for "example.com" may return "im.example.com:5269".
     * 
     * Note on SRV record selection.
     * We now check priority and weight, but we still don't do this correctly.
     * The missing behavior is this: if we fail to reach a host based on its SRV
     * record then we need to select another host from the other SRV records.
     * In Smack 3.1.1 we're not going to be able to do the major system redesign to
     * correct this.
     *
     * @param domain the domain.
     * @return a HostAddress, which encompasses the hostname and port that the XMPP
     *      server can be reached at for the specified domain.
     */
    public static HostAddress resolveXMPPDomain(String domain) {
        List<HostAddress> addresses = resolveDomain("_xmpp-client._tcp." + domain, 'c');
        return addresses.get(0);
    }

    /**
     * Returns the host name and port that the specified XMPP server can be
     * reached at for server-to-server communication. A DNS lookup for a SRV
     * record in the form "_xmpp-server._tcp.example.com" is attempted, according
     * to section 14.4 of RFC 3920. If that lookup fails, a lookup in the older form
     * of "_jabber._tcp.example.com" is attempted since servers that implement an
     * older version of the protocol may be listed using that notation. If that
     * lookup fails as well, it's assumed that the XMPP server lives at the
     * host resolved by a DNS lookup at the specified domain on the default port
     * of 5269.<p>
     *
     * As an example, a lookup for "example.com" may return "im.example.com:5269".
     *
     * @param domain the domain.
     * @return a HostAddress, which encompasses the hostname and port that the XMPP
     *      server can be reached at for the specified domain.
     */
    public static HostAddress resolveXMPPServerDomain(String domain) {
        List<HostAddress> addresses = resolveDomain("_xmpp-server._tcp." + domain, 's');
        return addresses.get(0);
    }

    private static List<HostAddress> resolveDomain(String domain, char keyPrefix) {
        // Prefix the key with 's' to distinguish him from the client domain lookups
        String key = keyPrefix + domain;
        // Return item from cache if it exists.
        if (cache.containsKey(key)) {
            List<HostAddress> addresses = cache.get(key);
            if (addresses != null) {
                return addresses;
            }
        }

        if (dnsResolver == null)
            throw new IllegalStateException("No supported DNS resolver found");

        List<HostAddress> addresses = new ArrayList<HostAddress>();

        // Step one: Do SRV lookups
        List<SRVRecord> srvRecords = dnsResolver.lookupSRVRecords("_xmpp-server._tcp." + domain);
        Collections.sort(srvRecords);
        addresses.addAll(srvRecords);

        // Step two: Add hostname records to the end of the list
        Set<HostAddress> fqdns = dnsResolver.lookupHostnamesRecords(domain);
        addresses.

        // Add item to cache.
        cache.put(key, addresses);

        return addresses;
    }
}