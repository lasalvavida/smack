package com.lasalvavida.jingle.audio.codec;

/**
 * Create subclasses of AudioCodec in order to add support for other rdp
 * formats to Jingle.
 *
 * An AudioCodec has three important methods that must be implemented
 * for your codec to function:
 * Info[] getAvailableDescriptions()  CodecManager uses these descriptions for protocol negotiation
 * byte[] encode(byte[])              Encode outgoing data
 * byte[] decode(byte[])              Decode incoming data
 *
 * Info objects are used to create XML descriptions as seen in:
 * http://xmpp.org/extensions/xep-0166.html
 *
 * Encode takes in PCM16 data and Decode produces PCM16 data
 *
 * Codecs should only ever be instantiated by CodecManager. Do not create
 * instances of them yourself.
 *
 * @author Rob Taglang
 * @date 1/20/14
 */
public abstract class AudioCodec {
    private Info active;
    public void setActiveInfo(Info info) {
        active = info;

    }
    public Info getActiveInfo() {
        return active;
    }

    /**
     * Codec implementations should override this method.
     *
     * @return An array of Info objects describing what this particular codec is capable of handling.
     * For example, a Speex codec could return something like this:
     * [new Info("speex", 16000, 1), new Info("speex", 8000, 1)]
     *
     * This information is used to generate the Jingle XML description:
     * <payload-type id='96' name='speex' clockrate='16000'/>
     * <payload-type id='97' name='speex' clockrate='8000'/>
     *
     * The identification numbers are handled by CodecManager.
     */
    public abstract Info[] getAvailableDescriptions();

    /**
     * Transform incoming encoded data into playable PCM16 bytes
     * @param data
     * @return decoded data
     */
    public abstract byte[] decode(byte[] data);

    /**
     * Transform recorded PCM16 bytes into encoded data
     * @param data
     * @return encoded data
     */
    public abstract byte[] encode(byte[] data);

    /**
     * A description class for Codecs
     * The codec writer is responsible for instantiating these in getAvailableDescriptions()
     */
    public static class Info {
        private String name = null;
        private int clockrate = -1, channels = -1;

        public Info(String name, int clockrate, int channels) {
            this.name = name;
            this.clockrate = clockrate;
            this.channels = channels;
        }

        public String getName() {
            return name;
        }

        public int getClockrate() {
            return clockrate;
        }

        public int getChannels() {
            return channels;
        }

        public boolean equals(Info info) {
            return getName().equals(info.getName()) && getClockrate() == info.getClockrate() && getChannels() == info.getChannels();
        }

        public String toString() {
            return "Name: " + name + " Clock Rate: " + clockrate + " Channels: " + channels;
        }
    }
}