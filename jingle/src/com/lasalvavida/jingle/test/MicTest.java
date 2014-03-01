package com.lasalvavida.jingle.test;

import com.lasalvavida.jingle.audio.AudioManager;
import com.lasalvavida.jingle.audio.AudioReceiver;
import com.lasalvavida.jingle.audio.PCMAudio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author Rob Taglang
 * @date 3/1/14
 */
public class MicTest implements AudioReceiver{
    private PCMAudio audio;
    public MicTest(PCMAudio audio) {
        this.audio = audio;
    }

    public static void main(String[] args) {
        PCMAudio audio = AudioManager.getPCMAudio();
        MicTest test = new MicTest(audio);
        audio.startRecording(test);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        audio.stopRecording(test);
    }

    @Override
    public void startReceiving() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void acceptBytes(byte[] bytes) {
        try {
            audio.play(bytes);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (LineUnavailableException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void stopReceiving() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
