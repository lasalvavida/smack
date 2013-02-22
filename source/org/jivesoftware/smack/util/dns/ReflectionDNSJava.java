package org.jivesoftware.smack.util.dns;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReflectionDNSJava extends DNSResolver {
    private static Class<?> lookupClass = null;
    private static Class<?> recordClass = null;
    private static Class<?> srvRecordClass = null;
    private static Class<?> typeClass = null;
    
    private static Method runMethod;
    private static Method getTargetMethod;
    private static Method getWeightMethod;
    private static Method getPriorityMethod;
    private static Method getPortMethod;
    
    private static Constructor<?> lookupConstructor;
    
    private static int typeSRVValue;
    
    private static boolean supported = false;
    
    private static ReflectionDNSJava instance;
    
    private ReflectionDNSJava() {
        
    }
    
    public static DNSResolver maybeGetInstance() {
        if (instance == null && supported) {
            instance = new ReflectionDNSJava();
        }
        return instance;
    }
    
    static {
        boolean initFailed = false;
        
        try {
            lookupClass = Class.forName("org.xbill.DNS.Lookup");
            recordClass = Class.forName("org.xbill.DNS.Record");
            srvRecordClass = Class.forName("org.xbill.DNS.SRVRecord");
            typeClass = Class.forName("org.xbill.DNS.Type");
        } catch (ClassNotFoundException e) {
            initFailed = true;
        }
        
        try {
            runMethod = lookupClass.getMethod("run", null);
            getTargetMethod = srvRecordClass.getMethod("getTarget", null);
            getWeightMethod = srvRecordClass.getMethod("getWeight", null);
            getPriorityMethod = srvRecordClass.getMethod("getPriority", null);
            getPortMethod = srvRecordClass.getMethod("getPort", null);
        } catch (Exception e) {
            initFailed = true;
        }
        
        try {
            lookupConstructor = lookupClass.getConstructor(String.class, int.class);
            Field typeSrv = typeClass.getField("SRV");
            typeSRVValue = typeSrv.getInt(typeClass);
        } catch (Exception e) {
            initFailed = true;
        }
        
        if (!initFailed)
            supported = true;
    }
    
    @Override
    public List<SRVRecord> lookupSRVRecords(String name) {
        List<SRVRecord> res = new ArrayList<SRVRecord>();
        Object[] recs = null;
        try {
            Object lookup = lookupConstructor.newInstance(name, typeSRVValue);
            recs = (Object[]) runMethod.invoke(lookup, null);
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }

        for (Object r : recs) {
            String fqdn;
            int port, priority, weight;
            try {
                fqdn = getTargetMethod.invoke(r, null).toString();
                port = (Integer) getPortMethod.invoke(r, null);
                priority = (Integer) getPriorityMethod.invoke(r, null);
                weight = (Integer) getWeightMethod.invoke(r, null);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            SRVRecord record;
            try {
                record = new SRVRecord(fqdn, port, priority, weight);
            } catch (Exception e) {
                continue;
            }

            res.add(record);
        }

        return res;
    }

}
