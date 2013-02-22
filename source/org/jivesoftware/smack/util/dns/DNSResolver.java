package org.jivesoftware.smack.util.dns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DNSResolver {

    public abstract List<SRVRecord> lookupSRVRecords(String name);

    /**
     * Returns all host addresses discovered by DNS. The default implementation of the DNSResolver superclass simply
     * wraps the provided name in a HostAddress Set without consulting any DNS server.
     * 
     * @param name
     * @return
     */
    public Set<HostAddress> lookupHostnamesRecords(String name) {
        Set<HostAddress> addresses = new HashSet<HostAddress>();

        try {
            HostAddress address = new HostAddress(name);
            addresses.add(address);
        } catch (Exception e) {
        }

        return addresses;
    }
}
