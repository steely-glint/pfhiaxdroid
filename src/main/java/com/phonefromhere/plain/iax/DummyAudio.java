/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phonefromhere.plain.iax;

import com.phonefromhere.plain.codec.CodecFace;
import com.phonefromhere.plain.codec.MediaFormat;
import com.phonefromhere.plain.codec.gsm.GSM_Codec;
import com.phonefromhere.plain.codec.ulaw.Ulaw;
import com.phonefromhere.plain.iax.frames.VoiceFrame;
import com.phonefromhere.softphone.AudioException;
import com.phonefromhere.softphone.AudioFace;
import com.phonefromhere.softphone.AudioReceiver;
import com.phonefromhere.softphone.NetStatsFace;
import com.phonefromhere.softphone.StampedAudio;
import com.phonefromhere.softphone.StampedAudioImpl;
import com.phono.srtplight.Log;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author thp
 */
public class DummyAudio implements AudioFace {

    private Long firstStamp;
    private int latency;
    private AudioReceiver audioReceiver;
    private byte[] silentframe;
    Timer tick;
    private Properties audioProps;
    private LinkedHashMap<MediaFormat, CodecFace> _codecMap;
    private CodecFace codec;

    DummyAudio() {
        _codecMap = new LinkedHashMap();
        this.fillCodecMap();
        this.silentframe = new byte[160];
        tick = new Timer();
    }

    @Override
    public String getCodecName() {
        String ret = "";
        if (codec != null) {
            ret = codec.getCodec().getName();
        }
        return ret;
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
    public long[] getCodecs() {
        int len = _codecMap.size();
        long codecs[] = new long[len];

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

    @Override
    public int getVADpc() {
        return 10;
    }

    @Override
    public boolean isCodecAvailable(long codec) {
        MediaFormat format = VoiceFrame.findMediaFormat(codec);
        return _codecMap.containsKey(format);
    }

    @Override
    public int getOutboundTimestamp() {
        long ret = 0;
        if (firstStamp != null) {
            ret = System.currentTimeMillis() - firstStamp;
        }
        return (int) (ret & 0x7fffffff);
    }

    @Override
    public void init(long codec, int latency) throws AudioException {
        Log.debug("audio init");
        MediaFormat format = VoiceFrame.findMediaFormat(codec);
        this.codec = _codecMap.get(format);
        this.latency = latency; // do we care ?
    }

    @Override
    public void addAudioReceiver(AudioReceiver r) throws AudioException {
        audioReceiver = r;
    }

    @Override
    public StampedAudio readStampedAudio() throws AudioException {
        Log.verb("audio read");

        StampedAudio audio = getCleanStampedAudio();
        audio.setStampAndBytes(silentframe, 0, silentframe.length, getOutboundTimestamp());
        return audio;
    }

    @Override
    public void updateRemoteStats(NetStatsFace r) {
    }

    @Override
    public void writeStampedAudio(StampedAudio stampedAudio) throws AudioException {
        Log.verb("audio write");
    }

    @Override
    public StampedAudio getCleanStampedAudio() {
        return new StampedAudioImpl();
    }

    @Override
    public void releaseStampedAudio(StampedAudio stampedAudio) {
    }

    @Override
    public boolean isAudioUp() {
        return (this.firstStamp != null);
    }

    @Override
    public void startPlay() {
        Log.debug("Start Play");
    }

    @Override
    public void stopPlay() {
        Log.debug("Stop Play");
    }

    @Override
    public void startRec() {
        Log.debug("Start Rec");
        firstStamp = System.currentTimeMillis();
        DummyAudio that = this;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (audioReceiver != null) {
                    audioReceiver.newAudioDataReady(that, silentframe.length);
                }
            }
        };
        tick.scheduleAtFixedRate(task, this.getFrameInterval(), this.getFrameInterval());
    }

    @Override
    public void stopRec() {
        Log.debug("Stop  Rec");
        this.tick.cancel();
    }

    @Override
    public void destroy() throws AudioException {
        Log.debug("Destroy");

    }

    @Override
    public Properties getAudioProperties() {
        return audioProps;
    }

    @Override
    public boolean setAudioProperty(String name, Object value) throws IllegalArgumentException {
        audioProps.put(name, value);
        return true;
    }

    @Override
    public double[] getEnergy() {
        double[] ret = {0.0, 0.0};
        return ret;
    }

    @Override
    public boolean doVAD() {
        return false;
    }

    protected void fillCodecMap() {
        // add all the supported Codecs, in the order of preference

        Ulaw ulaw = new Ulaw();
        GSM_Codec gsmCodec = new GSM_Codec();
        DummyOpusCodec opusCodec = new DummyOpusCodec();
        _codecMap.put(MediaFormat.OPUS, opusCodec);
        _codecMap.put(MediaFormat.ULAW, ulaw);
        _codecMap.put(MediaFormat.GSM, gsmCodec);
    }

    @Override
    public long getCodec() {
        long ret;
        if (codec != null) {
            ret = codec.getCodec().getValue();
        } else {
            throw new IllegalStateException(this.getClass().getSimpleName()
                    + ".getCodec(): codec is null, init first");
        }
        return ret;
    }
}
