package com.lasalvavida.jingle.audio.codec;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This is the Application access point for codecs.
 *
 * The init() method must be called before using AudioCodecManager
 *
 * Jingle does load some default codecs, however if any are added,
 * applications are responsible for registering their codecs with the CodecManager using:
 * void registerCodec(String className)
 *
 * @author Rob Taglang
 * @date 1/29/14
 */
public final class AudioCodecManager {
    private static HashMap<AudioCodec.Info, Integer> codecDescriptions = new HashMap<AudioCodec.Info, Integer>();
    private static HashMap<Integer, AudioCodec> codecs = new HashMap<Integer, AudioCodec>();

    private static String[] defaultCodecs = {
            "com.lasalvavida.jingle.audio.codec.imp.L16"
    };

    private static Integer availableId = 96; //marks the current available Codec ID #

    public static void init() {
        addRTPDescriptions();
        addDefaultCodecs();
    }

    private static void addRTPDescriptions() {
        codecDescriptions.put(new AudioCodec.Info("PCMU", 8000, 1), 0);
        codecDescriptions.put(new AudioCodec.Info("GSM", 8000, 1), 3);
        codecDescriptions.put(new AudioCodec.Info("G723", 8000, 1), 4);
        codecDescriptions.put(new AudioCodec.Info("DVI4", 8000, 1), 5);
        codecDescriptions.put(new AudioCodec.Info("DVI4", 16000, 1), 6);
        codecDescriptions.put(new AudioCodec.Info("LPC", 8000, 1), 7);
        codecDescriptions.put(new AudioCodec.Info("PCMA", 8000, 1), 8);
        codecDescriptions.put(new AudioCodec.Info("G722", 8000, 1), 9);
        codecDescriptions.put(new AudioCodec.Info("L16", 44100, 2), 10);
        codecDescriptions.put(new AudioCodec.Info("L16", 44100, 1), 11);
        codecDescriptions.put(new AudioCodec.Info("QCELP", 8000, 1), 12);
        codecDescriptions.put(new AudioCodec.Info("CN", 8000, 1), 13);
        codecDescriptions.put(new AudioCodec.Info("MPA", 90000, 1), 14);
        codecDescriptions.put(new AudioCodec.Info("G728", 8000, 1), 15);
        codecDescriptions.put(new AudioCodec.Info("DVI4", 11025, 1), 16);
        codecDescriptions.put(new AudioCodec.Info("DVI4", 22050, 1), 17);
        codecDescriptions.put(new AudioCodec.Info("G729", 8000, 1), 18);
    }

    private static void addDefaultCodecs() {
        for(String codec : defaultCodecs) {
            try {
                registerCodec(codec);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void registerCodec(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class c = Class.forName(className);
        if(c != null) {
            boolean valid = false;
            //check if the class is an AudioCodec
            Class superClass = c.getSuperclass();
            while(!superClass.equals(Object.class)) {
                if(superClass.equals(AudioCodec.class)) {
                    valid = true;
                    break;
                }
                superClass = superClass.getSuperclass();
            }
            if(valid) {
                AudioCodec codec = (AudioCodec)c.newInstance();
                for(AudioCodec.Info info : codec.getAvailableDescriptions()) {
                    Integer id = codecDescriptions.get(info);
                    if(id == null) {
                        id = availableId;
                        codecDescriptions.put(info, availableId);
                        availableId = availableId + 1;
                    }
                    codecs.put(id, codec);
                }
            }
        }
    }

    public static AudioCodec.Info[] getAvailableCodecs() {
        HashSet<AudioCodec.Info> descriptions = new HashSet<AudioCodec.Info>();
        for(AudioCodec.Info info : codecDescriptions.keySet()) {
            for(Integer i : codecs.keySet()) {
                if(i.equals(codecDescriptions.get(info)))
                    descriptions.add(info);
            }
        }
       return descriptions.toArray(new AudioCodec.Info[0]);
    }

    public static AudioCodec getCodec(AudioCodec.Info info) {
        return codecs.get(codecDescriptions.get(info));
    }
}
