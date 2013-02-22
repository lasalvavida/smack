package org.jivesoftware.smack.util.dns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class DNSJavaResolver extends DNSResolver {
    
    private static DNSJavaResolver instance;
    
    private DNSJavaResolver() {
        
    }
    
    public static DNSResolver getInstance() {
        if (instance == null) {
            instance = new DNSJavaResolver();
        }
        return instance;
    }

    @Override
    public List<SRVRecord> lookupSRVRecords(String name) {
        List<SRVRecord> res = new ArrayList<SRVRecord>();

        try {
            Lookup lookup = new Lookup(name, Type.SRV);
            Record recs[] = lookup.run();
            if (recs == null)
                return res;

            for (Record record : recs) {
                org.xbill.DNS.SRVRecord srvRecord = (org.xbill.DNS.SRVRecord) record;
                if (srvRecord != null && srvRecord.getTarget() != null) {
                    String host = srvRecord.getTarget().toString();
                    int port = srvRecord.getPort();
                    int priority = srvRecord.getPriority();
                    int weight = srvRecord.getWeight();

                    SRVRecord r;
                    try {
                        r = new SRVRecord(host, port, priority, weight);
                    } catch (Exception e) {
                        continue;
                    }
                    res.add(r);
                }
            }

        } catch (Exception e) {
        }
        return res;
    }
    
//    @Override
//    public Set<HostAddress> lookupHostnamesRecords(String name) {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
