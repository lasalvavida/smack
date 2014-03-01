package com.lasalvavida.jingle.test;

import com.lasalvavida.jingle.audio.AudioManager;
import com.lasalvavida.jingle.audio.PCMAudio;
import com.lasalvavida.jingle.audio.Sample;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author Rob Taglang
 * @date 1/20/14
 */
public class AudioTest {
    public static void main(String[] args) {
        int sampleRate = 44100;  //hz
        PCMAudio audio = AudioManager.getPCMAudio();
        audio.setSampleRate(sampleRate);
        audio.setChannels(1);
        audio.setSampleSize(Sample.BIT_8);
        try {
            Notes[] notes = {
                    Notes.G4, Notes.G4, Notes.G4,
                    Notes.C4, Notes.C4, Notes.C4,
                    Notes.E4F, Notes.F4, Notes.G4, Notes.G4,
                    Notes.C4, Notes.C4, Notes.E4F, Notes.F4,
                    Notes.D4, Notes.D4, Notes.D4,
                    Notes.D4, Notes.D4, Notes.D4,
                    Notes.D4, Notes.D4, Notes.D4,
                    Notes.D4, Notes.D4, Notes.D4,
                    Notes.F4, Notes.F4, Notes.F4,
                    Notes.B3F, Notes.B3F, Notes.B3F,
                    Notes.D4, Notes.E4F, Notes.F4, Notes.F4,
                    Notes.B3F, Notes.B3F, Notes.E4F, Notes.D4,
                    Notes.C4, Notes.C4, Notes.C4,
                    Notes.C4, Notes.C4, Notes.C4, Notes.C4
            };
            //one, a two, a one two three four
            for(Notes note : notes) {
                byte[] data = generate8BitSineWave(sampleRate, note.getValue(), 200);
                audio.play(data);
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (LineUnavailableException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static byte[] generate8BitSineWave(int sampleRate, int frequency, int duration) {
        byte[] output = new byte[(int)((duration/1000.0)*sampleRate)];
        double amplitude = 256;
        for(int x=0; x<(duration/1000.0)*sampleRate; x++) {
            double time = x/(double)sampleRate;
            double angle = 2.0*Math.PI*frequency*time;
            output[x] = (byte)(amplitude*(Math.sin(angle)));
        }
        return output;
    }

    private enum Notes {
        G3S(207),
        A3F(207),
        A3(220),
        A3S(233),
        B3F(233),
        B3(246),
        C4(261),
        C4S(277),
        D4F(277),
        D4(293),
        D4S(311),
        E4F(311),
        E4(329),
        F4(349),
        F4S(369),
        G4F(369),
        G4(392),
        G4S(415),
        A4F(415),
        A4(440),
        A4S(466),
        B4F(466),
        B4(493),
        C5(523);

        int frequency = -1;
        private Notes(int frequency) {
            this.frequency = frequency;
        }
        public int getValue() {
            return this.frequency;
        }
    }
}
