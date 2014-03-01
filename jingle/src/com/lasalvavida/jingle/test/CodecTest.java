package com.lasalvavida.jingle.test;

import com.lasalvavida.jingle.audio.codec.AudioCodec;
import com.lasalvavida.jingle.audio.codec.AudioCodecManager;

/**
 * @author Rob Taglang
 * @date 1/20/14
 */
public class CodecTest {
    public static void main(String[] args) {
        AudioCodecManager.init();
        printInfo(AudioCodecManager.getAvailableCodecs());
    }

    private static void printInfo(AudioCodec.Info[] arr) {
        for(AudioCodec.Info info : arr) {
            System.out.println(info);
        }
    }
}
