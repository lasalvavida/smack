package com.lasalvavida.jingle.audio;

/**
 * @author Rob Taglang
 * @date 3/1/14
 */
public interface AudioReceiver {
    public void startReceiving();
    public void acceptBytes(byte[] bytes);
    public void stopReceiving();
}
