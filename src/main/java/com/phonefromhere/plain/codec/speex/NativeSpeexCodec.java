/*
 * Copyright 2011 Westhawk Ltd.
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
package com.phonefromhere.plain.codec.speex;


import com.phonefromhere.plain.codec.CodecFace;
import com.phonefromhere.plain.codec.DecoderFace;
import com.phonefromhere.plain.codec.EncoderFace;
import com.phonefromhere.plain.codec.MediaFormat;

/**
 *
 * @author tim
 */
public class NativeSpeexCodec implements CodecFace, EncoderFace, DecoderFace {

    int _sampleRate;
    MediaFormat _iaxcn;
    String _name;
    int _aframesz;
    int _speexmode;
    short[] _adataOut;
    byte[] _wireOut;
    byte[] _codec;
    byte[] _outW;

    private native byte[] initCodec();

    private native int speexEncode(byte[] codec, short[] a, byte[] w);

    private native void speexDecode(byte[] codec, byte[] w, short[] a);

    public native void freeCodec(byte[] codec);

    static {
        System.loadLibrary("speex");
    }

    public NativeSpeexCodec() {

        _speexmode = 0;
        _sampleRate = 8000;
        _iaxcn = MediaFormat.SPEEX;
        _name = "SPEEX";
        _aframesz = 160; // number of shorts in an audio frame;
        _adataOut = new short[_aframesz];
        _wireOut = new byte[_aframesz]; // that _has to be big enough
        _codec = initCodec();
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
        int retsz = speexEncode(_codec, audio, _wireOut);
        if ((_outW == null) || (_outW.length != retsz)){
            _outW = new byte[retsz];
        }
        System.arraycopy(_wireOut, 0, _outW, 0, retsz);
        return _outW;
    }

    @Override
    public short[] decode_frame(byte[] bytes) {
        speexDecode(_codec, bytes, _adataOut);
        return _adataOut;
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
