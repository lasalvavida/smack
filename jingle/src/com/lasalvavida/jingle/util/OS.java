package com.lasalvavida.jingle.util;

/**
 * @author Rob Taglang
 * @date 1/18/14
 */
public enum OS {
    Windows,
    Mac,
    Unix,
    Android,
    Solaris,
    Unknown;

    public static OS getOS() {
        String prop = System.getProperty("os.name").toLowerCase();
        if(prop.contains("win")) {
            return OS.Windows;
        }
        else if(prop.contains("mac")) {
            return OS.Mac;
        }
        else if(prop.contains("nix") || prop.contains("nux") || prop.contains("aix")) {
            String vm = System.getProperty("java.vm.name").toLowerCase();
            if(vm.contains("dalvik")) {
                return OS.Android;
            }
            else {
                return OS.Unix;
            }
        }
        else if(prop.contains("sunos")) {
            return OS.Solaris;
        }
        else {
            return OS.Unknown;
        }
    }
}
