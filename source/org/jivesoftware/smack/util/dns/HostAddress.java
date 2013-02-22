package org.jivesoftware.smack.util.dns;

public class HostAddress {
    private String fqdn;
    private int port;
    
    HostAddress(String fqdn) throws Exception {
        if (fqdn == null)
            throw new Exception("DNS SRV records must come with a FQDN");
        if (fqdn.charAt(fqdn.length() - 1) == '.') {
            this.fqdn = fqdn.substring(0, fqdn.length() - 1);
        } else {
            this.fqdn = fqdn;
        }
        this.port = -1;
    }
    
    HostAddress(String fqdn, int port) throws Exception {
        this(fqdn);
        if (port < 0 || port > 65535)
            throw new Exception(
                    "DNS SRV records weight must be a 16-bit unsiged integer (i.e. between 0-65535. Port was: " + port);
        
        this.port = port;
    }
    
    public String getFQDN() {
        return fqdn;
    }
    
    public int getPort() {
        return port;
    }
    
    public String toString() {
        if (port >= 0) {
            return fqdn + ":" + port;
        } else {
            return fqdn;
        }
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HostAddress)) {
            return false;
        }

        final HostAddress address = (HostAddress) o;

        if (!fqdn.equals(address.fqdn)) {
            return false;
        }
        return port == address.port;
    }
}
