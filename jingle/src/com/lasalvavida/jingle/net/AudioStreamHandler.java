package com.lasalvavida.jingle.net;

import com.lasalvavida.jingle.audio.AudioManager;
import com.lasalvavida.jingle.net.rtp.StreamHandler;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author Rob Taglang
 * @date 1/30/14
 */
public class AudioStreamHandler implements StreamHandler {
    @Override
    public void acceptBytes(byte[] bytes) {
        try {
            AudioManager.playBytes(bytes);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
