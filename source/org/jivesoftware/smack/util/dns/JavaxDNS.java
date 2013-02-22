package org.jivesoftware.smack.util.dns;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class JavaxDNS extends DNSResolver {
    private static Class<?> namingEnumerationClass = null;
    private static Class<?> attributeClass = null;
    private static Class<?> attributesClass = null;
    private static Class<?> dirContextClass = null;
    private static Class<?> initialDirContextClass = null;
    
    private static Method getAttributesMethod;
    private static Method getSRVAttributeMethod;
    private static Method getAllRecordMethod;
    private static Method hasMoreMethod;
    private static Method nextMethod;

    private static boolean supported = false;
    private static Object dirContext;
    
    private static final String[] SRVAttributes = new String[]{"SRV"};
    
    static {
        boolean initFailed = false;
        try {
            namingEnumerationClass = Class.forName("javax.naming.NamingEnumeration");
            attributeClass = Class.forName("javax.naming.directory.Attribute");
            attributesClass = Class.forName("javax.naming.directory.Attributes");
            dirContextClass = Class.forName("javax.naming.directory.DirContext");
            initialDirContextClass = Class.forName("javax.naming.directory.InitialDirContext");
        } catch (ClassNotFoundException e) {
            initFailed = true;
        }
     
        try {
            getAttributesMethod = initialDirContextClass.getMethod("getAttributes", String.class, String[].class);
            getSRVAttributeMethod = attributesClass.getMethod("get", String.class);
            getAllRecordMethod = attributeClass.getMethod("getAll", null);
            hasMoreMethod = namingEnumerationClass.getMethod("hasMore", null);
            nextMethod = namingEnumerationClass.getMethod("next", null);
        } catch (Exception e) {
            initFailed = true;
        }
                
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            Constructor<?> constructor = initialDirContextClass.getConstructor(Hashtable.class);
            dirContext = constructor.newInstance(env);
        }
        catch (Exception e) {
            initFailed = true;
        }
        
        if (!initFailed)
            supported = true;
    }

    public boolean isSupported() {
        return supported;
    }
    
    @Override
    public List<SRVRecord> lookupSRVRecords(String name) {
        List<SRVRecord> res = new ArrayList<SRVRecord>();
        
        try {
            Object attributes = getAttributesMethod.invoke(dirContext, name, SRVAttributes);

            Object attribute = getSRVAttributeMethod.invoke(attributes, "SRV");
            Object namingEnumeration = getAllRecordMethod.invoke(attribute);

            while ((Boolean) hasMoreMethod.invoke(namingEnumeration)) {
                String srvRecord = (String) nextMethod.invoke(namingEnumeration);

                String[] srvRecordEntries = srvRecord.split(" ");

                int priority = Integer.parseInt(srvRecordEntries[srvRecordEntries.length - 4]);
                int port = Integer.parseInt(srvRecordEntries[srvRecordEntries.length - 2]);
                int weight = Integer.parseInt(srvRecordEntries[srvRecordEntries.length - 3]);
                String host = srvRecordEntries[srvRecordEntries.length - 1];

                SRVRecord r;

                try {
                    r = new SRVRecord(host, port, priority, weight);
                } catch (Exception e) {
                    continue;
                }
                res.add(r);
            }
        } catch (IllegalArgumentException e1) {
        } catch (IllegalAccessException e1) {
        } catch (InvocationTargetException e1) {
        }
        
        return res;
    }

    @Override
    public Set<HostAddress> lookupARecords(String name) {
        return null;
    }

    @Override
    public Set<HostAddress> lookupAAAARecords(String name) {
        return null;
    }

}
