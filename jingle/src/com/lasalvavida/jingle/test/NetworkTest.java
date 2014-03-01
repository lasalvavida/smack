package com.lasalvavida.jingle.test;

import com.lasalvavida.jingle.net.Util;

import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Rob Taglang
 * @date 1/30/14
 */
public class NetworkTest {
    public static void main(String[] args) {
        try {
            for(InetAddress address : Util.filterForIPv4(Util.getAvailableBindAddresses())) {
                System.out.println(address);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
