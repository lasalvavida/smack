package com.lasalvavida.jingle.audio;

import com.lasalvavida.jingle.audio.codec.AudioCodec;
import com.lasalvavida.jingle.audio.codec.AudioCodecManager;
import com.lasalvavida.jingle.audio.imp.PCMAudioJavax;
import com.lasalvavida.jingle.util.OS;

//TODO: Create global exception so that these can be tucked away
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author Rob Taglang
 * @date 1/18/14
 */
public final class AudioManager {
    public static AudioCodec.Info codec = null;

    /**
     * Play audio bytes through the current active codec
     * @param bytes
     */
    public static void playBytes(byte[] bytes) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        PCMAudio audio = getPCMAudio();
        if(codec != null && AudioCodecManager.getCodec(codec) != null) {
            bytes = AudioCodecManager.getCodec(codec).decode(bytes);
            audio.setChannels(codec.getChannels());
            audio.setSampleRate(codec.getClockrate());
        }
        audio.play(bytes);
    }
    /**
     * @return a working implementation of PCMAudio for your platform,
     * or null if none can be found
     */
    public static PCMAudio getPCMAudio() {
        OS os = OS.getOS();
        if(os == OS.Android) {
            //create android track player
        }
        else {
            return new PCMAudioJavax();
        }
        return null;
    }
}
