package org.jivesoftware.smack.util.dns;

/**
 * @see <a href="http://tools.ietf.org/html/rfc2782>RFC 2782: A DNS RR for specifying the location of services (DNS
 * SRV)<a>
 * @author Florian Schmaus
 * 
 */
public class SRVRecord extends HostAddress implements Comparable<SRVRecord> {
    
    private int weight;
    private int priority;
    
    private int randomWeight;
    
    SRVRecord(String fqdn, int port, int priority, int weight) throws Exception {
        super(fqdn, port);
        if (weight < 0 || weight > 65535)
            throw new Exception(
                    "DNS SRV records weight must be a 16-bit unsiged integer (i.e. between 0-65535. Weight was: "
                            + weight);

        if (priority < 0 || priority > 65535)
            throw new Exception(
                    "DNS SRV records priority must be a 16-bit unsiged integer (i.e. between 0-65535. Priority was: "
                            + priority);

        this.priority = priority;
        this.weight = weight;

        // Add at least 1 to the weight when randomizing the weight,
        // so that also weights with the value 0 are randomized
        this.randomWeight = (int) ((this.weight + 1) * Math.random()) * 100;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public int getWeight() {
        return weight;
    }

    public int compareTo(SRVRecord other) {
        // According to RFC2782,
        // "[a] client MUST attempt to contact the target host with the lowest-numbered priority it can reach".
        // This means that a SRV record with a higher priority is 'less' then one with a lower.
        int res = other.priority - this.priority;
        if (res == 0) {
            res = this.randomWeight - other.randomWeight;
        }
        return res;
    }
}
