/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phonefromhere.plain.iax;

import com.phonefromhere.plain.codec.CodecFace;
import com.phonefromhere.plain.codec.MediaFormat;
import com.phonefromhere.plain.iax.frames.VoiceFrame;
import com.phonefromhere.softphone.AudioException;
import com.phonefromhere.softphone.AudioFace;
import com.phonefromhere.softphone.AudioReceiver;
import com.phonefromhere.softphone.NetStatsFace;
import com.phonefromhere.softphone.StampedAudio;
import com.phonefromhere.softphone.StampedAudioImpl;
import com.phono.srtplight.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author thp
 */
public abstract class AbstractAudio implements AudioFace {

    protected Properties audioProps;
    protected AudioReceiver audioReceiver;
    protected CodecFace codec;
    protected HashMap<MediaFormat,CodecFace> _codecMap;

    public AbstractAudio() {
        _codecMap = new LinkedHashMap();
        this.fillCodecMap();
    }

    protected void fillCodecMap() {
        // add all the supported Codecs, in the order of preference
        DummyOpusCodec opusCodec = new DummyOpusCodec();
        _codecMap.put(MediaFormat.OPUS, opusCodec);

    }

    public Properties getAudioProperties() {
        return audioProps;
    }

    public StampedAudio getCleanStampedAudio() {
        return new StampedAudioImpl();
    }

    public long getCodec() {
        long ret;
        if (codec != null) {
            ret = codec.getCodec().getValue();
        } else {
            throw new IllegalStateException(this.getClass().getSimpleName() + ".getCodec(): codec is null, init first");
        }
        return ret;
    }

    public String getCodecName() {
        String ret = "";
        if (codec != null) {
            ret = codec.getCodec().getName();
        }
        return ret;
    }

    public long[] getCodecs() {
        int len = _codecMap.size();
        long[] codecs = new long[len];
        Set<MediaFormat> keySet = _codecMap.keySet();
        Iterator<MediaFormat> iter = keySet.iterator();
        int i = 0;
        while (iter.hasNext()) {
            MediaFormat format = iter.next();
            long codec = format.getValue();
            codecs[i] = codec;
            i++;
        }
        return codecs;
    }

    public double[] getEnergy() {
        double[] ret = {0.0, 0.0};
        return ret;
    }

    public int getFrameInterval() {
        return 20;
    }

    public int getFrameSize() {
        return -1;
    }

    public int getVADpc() {
        return 10;
    }

    public boolean isCodecAvailable(long codec) {
        MediaFormat format = VoiceFrame.findMediaFormat(codec);
        return _codecMap.containsKey(format);
    }

    public void releaseStampedAudio(StampedAudio stampedAudio) {
    }

    public boolean setAudioProperty(String name, Object value) throws IllegalArgumentException {
        audioProps.put(name, value);
        return true;
    }

    public void updateRemoteStats(NetStatsFace r) {
    }

    public boolean doVAD() {
        return false;
    }
    public void init(long codec, int latency) throws AudioException {
        Log.debug("audio init");
        MediaFormat format = VoiceFrame.findMediaFormat(codec);
        this.codec = _codecMap.get(format);
    }
}
