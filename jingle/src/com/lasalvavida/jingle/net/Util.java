package com.lasalvavida.jingle.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * @author Rob Taglang
 * @date 1/30/14
 */
public final class Util {
    public static InetAddress[] getAvailableBindAddresses() throws SocketException {
        HashSet<InetAddress> bindAddresses = new HashSet<InetAddress>();
        Enumeration infs = NetworkInterface.getNetworkInterfaces();
        while(infs.hasMoreElements()) {
            NetworkInterface inf = (NetworkInterface)infs.nextElement();
            Enumeration addresses = inf.getInetAddresses();
            while(addresses.hasMoreElements()) {
                bindAddresses.add((InetAddress)addresses.nextElement());
            }
        }
        return bindAddresses.toArray(new InetAddress[0]);
    }

    public static InetAddress[] filterForIPv4(InetAddress[] addresses) {
        HashSet<InetAddress> ipv4 = new HashSet<InetAddress>();
        for(InetAddress address : addresses) {
            if(address instanceof Inet4Address) {
                ipv4.add(address);
            }
        }
        return ipv4.toArray(new InetAddress[0]);
    }
}
