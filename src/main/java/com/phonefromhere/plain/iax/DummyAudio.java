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
import com.phonefromhere.softphone.AudioReceiver;
import com.phonefromhere.softphone.StampedAudio;
import com.phono.srtplight.Log;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author thp
 */
public class DummyAudio extends AbstractAudio {

    private Long firstStamp;
    private int latency;
    private byte[] silentframe;
    Timer tick;

    DummyAudio() {
        super();

        this.silentframe = new byte[160];
        tick = new Timer();
    }


    @Override
    public void init(long codec, int latency) throws AudioException {
        super.init(codec, latency);
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
    public void writeStampedAudio(StampedAudio stampedAudio) throws AudioException {
        Log.verb("audio write");
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
    public int getOutboundTimestamp() {
        long ret = 0;
        if (firstStamp != null) {
            ret = System.currentTimeMillis() - firstStamp;
        }
        return (int) (ret & 2147483647);
    }


    protected void fillCodecMap() {
        // add all the supported Codecs, in the order of preference
        super.fillCodecMap();
        Ulaw ulaw = new Ulaw();
        GSM_Codec gsmCodec = new GSM_Codec();
        _codecMap.put(MediaFormat.ULAW, ulaw);
        _codecMap.put(MediaFormat.GSM, gsmCodec);
    }

}
