package com.lasalvavida.jingle.audio.imp;

import com.lasalvavida.jingle.audio.AudioReceiver;
import com.lasalvavida.jingle.audio.PCMAudio;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Rob Taglang
 * @date 1/18/14
 */
public class PCMAudioJavax extends PCMAudio {
    private SourceDataLine sourceLine = null;
    private TargetDataLine targetLine = null;
    private boolean initialized = false, lock = false;
    private AtomicBoolean recording = new AtomicBoolean(false);
    private ArrayList<ByteArrayWrapper> data = new ArrayList<ByteArrayWrapper>();
    public void init() {
        AudioFormat audioFormat = new AudioFormat((float)this.getSampleRate(), this.getSampleSize().getValue(), this.getChannels(), true, false);
        try {
            this.sourceLine = (SourceDataLine)AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]).getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
            this.sourceLine.open(audioFormat);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            if(AudioSystem.isLineSupported(info)) {
                targetLine = (TargetDataLine)AudioSystem.getLine(info);
                targetLine.open(audioFormat);
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    @Override
    public void play(byte[] data) throws LineUnavailableException {
        if(!initialized) {
            init();
            initialized = true;
        }
        if(!sourceLine.isRunning()) {
            sourceLine.start();
        }
        if(!lock) {
            lock = true;
            playImp(data);
            lock = false;
        }
        else {
            this.data.add(new ByteArrayWrapper(data));
        }
    }

    @Override
    public void startRecording(final AudioReceiver receiver) {
        if(!initialized) {
            init();
            initialized = true;
        }
        recording.set(true);
        receiver.startReceiving();
        Thread spinoff = new Thread() { public void run() {
            int numBytesRead;
            byte[] data = new byte[targetLine.getBufferSize() / 5];
            byte[] outbytes;
            targetLine.start();

            while (recording.get()) {
                numBytesRead =  targetLine.read(data, 0, data.length);
                outbytes = new byte[numBytesRead];
                System.arraycopy(data, 0, outbytes, 0, numBytesRead);
                receiver.acceptBytes(outbytes);
            }
        }};
        spinoff.start();
    }

    @Override
    public void stopRecording(AudioReceiver receiver) {
        receiver.stopReceiving();
        recording.set(false);
    }

    public void playImp(byte[] data) throws LineUnavailableException {
        this.sourceLine.write(data, 0, data.length);
        if(this.data.size() > 0) {
            ByteArrayWrapper next = this.data.remove(0);
            playImp(next.getBytes());
            return;
        }
        if(sourceLine.isRunning()) {
            sourceLine.stop();
        }
    }

    private class ByteArrayWrapper {
        private byte[] bytes;
        public ByteArrayWrapper(byte[] bytes) {
            this.bytes = bytes;
        }
        public byte[] getBytes() {
            return bytes;
        }
    }
}
