package org.jivesoftware.smack.util.dns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DNSResolver {
    
    public abstract List<SRVRecord> lookupSRVRecords(String name);
    
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
