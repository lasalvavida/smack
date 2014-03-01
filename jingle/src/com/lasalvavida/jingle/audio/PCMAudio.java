package com.lasalvavida.jingle.audio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author Rob Taglang
 * @date 1/12/14
 */
public abstract class PCMAudio {
    private int sampleRate = 44100, channels = 1;
    private Sample sampleSize = Sample.BIT_16;
    public int getSampleRate() {
        return this.sampleRate;
    }
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
    public Sample getSampleSize() {
        return this.sampleSize;
    }
    public void setSampleSize(Sample sampleSize) {
        this.sampleSize = sampleSize;
    }
    public int getChannels() {
        return this.channels;
    }
    public void setChannels(int channels) {
        this.channels = channels;
    }
    public abstract void play(byte[] data) throws IOException, UnsupportedAudioFileException, LineUnavailableException;
    public abstract void startRecording(AudioReceiver receiver);
    public abstract void stopRecording(AudioReceiver receiver);
}
