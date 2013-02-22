package org.jivesoftware.smack.util.dns;

import java.util.List;
import java.util.Set;

public abstract class DNSResolver {
    
    public abstract boolean isSupported();
    
    public abstract List<SRVRecord> lookupSRVRecords(String name);
    
    public abstract Set<HostAddress> lookupARecords(String name);
    
    public abstract Set<HostAddress> lookupAAAARecords(String name);
}
