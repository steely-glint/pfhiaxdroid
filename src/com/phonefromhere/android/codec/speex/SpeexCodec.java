/*
 * Copyright 2011 Voxeo Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonefromhere.android.codec.speex;

import com.phonefromhere.android.AndroidLog;
import com.phonefromhere.android.codec.CodecFace;
import com.phonefromhere.android.codec.DecoderFace;
import com.phonefromhere.android.codec.EncoderFace;
import com.phonefromhere.android.codec.MediaFormat;
import java.io.StreamCorruptedException;

import org.xiph.speex.SpeexDecoder;
import org.xiph.speex.SpeexEncoder;

/**
 *
 * @author tim
 */
public class SpeexCodec implements CodecFace, EncoderFace, DecoderFace {

    SpeexEncoder _spxe;
    SpeexDecoder _spxd;
    int _sampleRate;
    MediaFormat _iaxcn;
    String _name;
    int _aframesz;
    int _speexmode;

    public SpeexCodec() {
        boolean wide = false;
        _speexmode = 0;
        _sampleRate = 8000;
        _iaxcn = MediaFormat.SPEEX ;
        _name ="SPEEX";
        _spxe = new SpeexEncoder();
        _spxe.init(_speexmode, 1, _sampleRate, 1);// _mode, _quality, _sampleRate, _channels);
        _spxe.getEncoder().setComplexity(1);

        _spxd = new SpeexDecoder();
        _spxd.init(_speexmode, _sampleRate, 1, false);// _mode, _sampleRate, _channels, false);
        _aframesz =  160; // number of shorts in an audio frame;
    }


    @Override
    public int getFrameSize() {
        return -1; // we don't know - it is officailly a vbr codec.
    }

    @Override
    public int getFrameInterval() {
        return 20;
    }


    @Override
    public DecoderFace getDecoder() {
        return this;
    }

    @Override
    public EncoderFace getEncoder() {
        return this;
    }

    public String getName() {
        return _name;
    }

    @Override
    public float getSampleRate() {
        return _sampleRate;
    }

    @Override
    public byte[] encode_frame(short[] audio) {
        _spxe.processData(audio, 0, audio.length);
        int sz = _spxe.getProcessedDataByteSize();
        byte[] wireOut = new byte[sz];
        int got = _spxe.getProcessedData(wireOut, 0);
        return wireOut;
    }

    @Override
    public short[] decode_frame(byte[] bytes) {
        try {
            _spxd.processData(bytes, 0, bytes.length);
        } catch (StreamCorruptedException ex) {
            AndroidLog.getLog().error("Speex Decoder error " + ex.getMessage());
        }
        short audioOut[] = new short[_aframesz];
        int decsize = _spxd.getProcessedData(audioOut, 0);
        return audioOut;
    }

    @Override
    public byte[] lost_frame(byte[] bytes, byte[] bytes1) {
        // only gives us a _decoded_ frame - we don;t know what to do with that....
        // todo....
        return null;
    }

    @Override
    public MediaFormat getCodec() {
        return this._iaxcn;
    }
}
