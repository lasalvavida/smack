package com.lasalvavida.jingle.audio.codec.imp;

import com.lasalvavida.jingle.audio.PCMAudio;
import com.lasalvavida.jingle.audio.codec.AudioCodec;

/**
 * This is a working example of how to write an AudioCodec implementation.
 * Since L16 is already PCM16 audio, no transforms are required.
 *
 * For other implementations, encode and decode generally call external
 * library functions
 *
 * @author Rob Taglang
 * @date 1/30/14
 */
public class L16 extends AudioCodec {
    @Override
    public Info[] getAvailableDescriptions() {
        return new Info[] {
                new Info("L16", 44100, 2),
                new Info("L16", 44100, 1)
        };
    }

    @Override
    public byte[] decode(byte[] data) {
        return data;
    }

    @Override
    public byte[] encode(byte[] data) {
        return data;
    }
}
