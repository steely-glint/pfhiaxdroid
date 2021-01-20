/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phonefromhere.plain.iax;

import com.phonefromhere.plain.codec.CodecFace;
import com.phonefromhere.plain.codec.DecoderFace;
import com.phonefromhere.plain.codec.EncoderFace;
import com.phonefromhere.plain.codec.MediaFormat;

/**
 *
 * @author thp
 */
class DummyOpusCodec implements CodecFace {

    @Override
    public MediaFormat getCodec() {
        return MediaFormat.OPUS;
    }

    @Override
    public int getFrameSize() {
        return -1;
    }

    @Override
    public int getFrameInterval() {
        return 20;
    }

    @Override
    public float getSampleRate() {
        return 48000.0F;
    }

    @Override
    public DecoderFace getDecoder() {
        return null;
    }

    @Override
    public EncoderFace getEncoder() {
        return null;
    }

}
