package com.lasalvavida.jingle.audio;

/**
 * @author Rob Taglang
 * @date 1/20/14
 */
public enum Sample {
    BIT_8(8),
    BIT_16(16),
    BIT_24(24),
    BIT_32(32);

    int value = -1;
    private Sample(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
