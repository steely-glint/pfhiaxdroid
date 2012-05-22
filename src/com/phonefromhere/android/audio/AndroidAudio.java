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
package com.phonefromhere.android.audio;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import android.media.AudioFormat;
import android.media.AudioTrack;

import com.phonefromhere.android.AndroidLog;
import com.phonefromhere.android.codec.CodecFace;
import com.phonefromhere.android.codec.MediaFormat;
import com.phonefromhere.android.codec.gsm.GSM_Codec;
import com.phonefromhere.android.codec.speex.NativeSpeexCodec;
import com.phonefromhere.android.codec.speex.SpeexCodec;
import com.phonefromhere.android.codec.ulaw.Ulaw;
import com.phonefromhere.android.iax.frames.VoiceFrame;
import com.phonefromhere.softphone.AudioException;
import com.phonefromhere.softphone.AudioFace;
import com.phonefromhere.softphone.AudioReceiver;
import com.phonefromhere.softphone.NetStatsFace;
import com.phonefromhere.softphone.StampedAudio;
import com.phonefromhere.softphone.StampedAudioImpl;

public class AndroidAudio implements AudioFace {

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: AndroidAudio.java,v 1.11 2011/03/23 09:22:42 uid100 Exp $ Copyright Westhawk Ltd";
    public final static int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //public final static int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
    private AudioReceiver _audioReceiver;
    private Properties _audioProperties;
    // Codec related
    protected LinkedHashMap<MediaFormat, CodecFace> _codecMap;
    protected CodecFace _defaultCodec;
    private CodecFace _codec;
    // a circular buffer for StampedAudio, read from the mic
    private StampedAudio[] _stampedBuffer;
    private int _stampedBufferStart;
    private int _stampedBufferEnd;
    private AndroidAudioSpeaker _speaker;
    private AndroidAudioMic _mic;
    private ArrayList<StampedAudio> _cleanAudioList;

    public AndroidAudio() {
        _codecMap = new LinkedHashMap<MediaFormat, CodecFace>();
        fillCodecMap();
        _audioProperties = new Properties();
        _cleanAudioList = new ArrayList<StampedAudio>();

    }

    @Override
    public void init(long codec, int latency) throws AudioException {
        MediaFormat format = VoiceFrame.findMediaFormat(codec);
        _codec = _codecMap.get(format);
        if (_codec == null) {
            _codec = getDefaultCodec();
            AndroidLog.getLog().warn(
                    this.getClass().getSimpleName()
                    + ".init(): Using default codec:"
                    + _codec.getCodec().getName());
        }

        if (_codec != null) {
            AndroidLog.getLog().debug(
                    this.getClass().getSimpleName() + ".init(): "
                    + _codec.toString());

            // apply audio properties again, in case it is codec related!
            Enumeration<Object> e = _audioProperties.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                Object value = _audioProperties.getProperty(key);
                setAudioProperty(key, value);
            }
        } else {
            String text = this.getClass().getSimpleName() + ".init(): codec="
                    + _codec.getCodec().getName() + " (" + codec
                    + ") is not supported.";
            AndroidLog.getLog().error(text);
            throw new AudioException(text);
        }

        if (latency == 0) {
            latency = 120;
        }

        int sampleRate = (int) _codec.getSampleRate();

        // frame interval in milliseconds is:
        int frameIntMS = _codec.getFrameInterval();
        // frame rate per sec is:
        int frameRateSec = 1000 / frameIntMS;
        // samples per frame, at 8kHz, is:
        int samplesPerFrame = sampleRate / frameRateSec;
        // sample is in short, so frame size in bytes is:
        int bytesPerFrame = samplesPerFrame * 2;

        _stampedBuffer = new StampedAudioImpl[frameRateSec];

        // open Audio: mic & speakers/headset
        boolean isOK = true;
        if (_speaker != null) {
            _speaker.destroy();
        }
        _speaker = new AndroidAudioSpeaker(_codec, this);
        isOK = _speaker.initSpeaker(sampleRate, bytesPerFrame);

        if (isOK == true) {
            if (_mic != null) {
                _mic.destroy();
            }
            _mic = new AndroidAudioMic(_codec, this);
            isOK = _mic.initMic(sampleRate, bytesPerFrame);
        }
        if (isOK == false) {
            String text = this.getClass().getSimpleName() + ".init(): "
                    + "failed to initialise either speaker or microphone";
            AndroidLog.getLog().error(text);
            throw new AudioException(text);
        }

    }

    @Override
    public long getCodec() {
        long ret;
        if (_codec != null) {
            ret = _codec.getCodec().getValue();
        } else {
            throw new IllegalStateException(this.getClass().getSimpleName()
                    + ".getCodec(): codec is null, init first");
        }
        return ret;
    }

    @Override
    public String getCodecName() {
        String ret = "";
        if (_codec != null) {
            ret = _codec.getCodec().getName();
        }
        return ret;
    }

    @Override
    public int getFrameSize() {
        int ret;
        if (_codec != null) {
            ret = _codec.getFrameSize();
        } else {
            throw new IllegalStateException(this.getClass().getSimpleName()
                    + ".getFrameSize(): codec is null, init first");
        }
        return ret;
    }

    @Override
    public int getFrameInterval() {
        int ret;
        if (_codec != null) {
            ret = _codec.getFrameInterval();
        } else {
            AndroidLog.getLog().warn(
                    this.getClass().getSimpleName()
                    + ".getFrameInterval(): codec is null, init first");
            ret = 20;
        }
        return ret;
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
        return -1;
    }

    @Override
    public boolean isCodecAvailable(long codec) {
        MediaFormat format = VoiceFrame.findMediaFormat(codec);
        return _codecMap.containsKey(format);
    }

    @Override
    public int getOutboundTimestamp() {
        int ret = 0;
        if (_mic != null) {
            ret = _mic.getOutboundTimestamp();
        }
        return ret;
    }

    @Override
    public void addAudioReceiver(AudioReceiver r) {
        _audioReceiver = r;
    }

    /**
     * Sends audio data to the speakers.
     * 
     * @param stampedAudio
     *            The stamped audio to speaker
     */
    @Override
    public void writeStampedAudio(StampedAudio stampedAudio)
            throws AudioException {
        if (_speaker != null) {
            if (_speaker.getSpeakerFrames() == 0) {
                // let the mic buffer prefill before we start the speaker
                // startRec();
            }
            _speaker.writeStampedAudio(stampedAudio);

        } else {
            throw new AudioException(
                    this.getClass().getSimpleName()
                    + ".writeStampedAudio(): Audio not initialised, call init() first!");
        }
    }
    int _aibc = 0;

    @Override
    public StampedAudio getCleanStampedAudio() {
        StampedAudio ret = null;
        synchronized (_cleanAudioList) {
            if (_cleanAudioList.size() > 0) {
                ret = _cleanAudioList.remove(0);
            } else {
                ret = new StampedAudioImpl();
                AndroidLog.getLog().debug(
                        this.getClass().getSimpleName()
                        + ".getCleanStampedAudio(): created a fresh stampedAudio instance " + _aibc);
                _aibc++;

            }
        }
        return ret;
    }

    @Override
    public void releaseStampedAudio(StampedAudio stampedAudio) {
        synchronized (_cleanAudioList) {
            _cleanAudioList.add(stampedAudio);
        }
    }

    /**
     * Saves the stamped audio, read from the mic, into a buffer
     */
    public void saveReadStampedAudio(StampedAudio stampedAudio) {
        _stampedBufferEnd++;
        int actualPos = _stampedBufferEnd % _stampedBuffer.length;
        _stampedBuffer[actualPos] = stampedAudio;

        AndroidLog.getLog().sound(
                this.getClass().getSimpleName()
                + ".saveReadStampedAudio(): pos=" + _stampedBufferEnd
                + " (" + actualPos + "), " + stampedAudio.toString());

        int distance = _stampedBufferEnd - _stampedBufferStart;
        if (distance > _stampedBuffer.length) {
            AndroidLog.getLog().debug(
                    this.getClass().getSimpleName()
                    + ".saveReadStampedAudio(): overflow, start="
                    + _stampedBufferStart + ", end="
                    + _stampedBufferEnd);
        }

        if (_audioReceiver != null) {
            _audioReceiver.newAudioDataReady(this, _codec.getFrameSize());
        } else {
            AndroidLog.getLog().error(
                    this.getClass().getSimpleName()
                    + ".saveReadStampedAudio(): "
                    + " _audioReceiver is null");
        }
    }

    /**
     * Reads data from the audio queue. May block for upto 2x frame interval,
     * otherwise returns null. May throw an exception if Audio Channel is not
     * set up correctly.
     * 
     * @return a StampedAudio containing a framesize worth of data.
     * @see #getFrameSize()
     */
    @Override
    public StampedAudio readStampedAudio() throws AudioException {
        StampedAudio stampedAudio = null;
        if (_mic != null) {
            if (_stampedBufferStart < _stampedBufferEnd) {
                int actualPos = _stampedBufferStart % _stampedBuffer.length;
                stampedAudio = _stampedBuffer[actualPos];

                if (stampedAudio != null) {
                    AndroidLog.getLog().sound(
                            this.getClass().getSimpleName()
                            + ".readStampedAudio(): pos="
                            + _stampedBufferStart + " (" + actualPos
                            + "), " + stampedAudio.toString());
                }

                _stampedBufferStart++;
            }
        } else {
            throw new AudioException(
                    this.getClass().getSimpleName()
                    + ".readStampedAudio(): Audio not initialised, call init() first!");
        }
        return stampedAudio;
    }

    @Override
    public boolean isAudioUp() {
        return (_mic != null && _speaker != null);
    }

    @Override
    public void startPlay() {
        if (_speaker != null) {
            _speaker.startPlay();
        } else {
            AndroidLog.getLog().error(
                    this.getClass().getSimpleName()
                    + ".startPlay(): initialise speaker first");
        }
    }

    @Override
    public void stopPlay() {
        if (_speaker != null) {
            _speaker.stopPlay();
        }
    }

    @Override
    public void startRec() {
        if (_mic != null) {
            _mic.startRec();
            /*
             * AndroidLog.getLog().error( this.getClass().getSimpleName() +
             * ".startRec(): not starting mic to see what happens....");
             */
        } else {
            AndroidLog.getLog().error(
                    this.getClass().getSimpleName()
                    + ".startRec(): initialise microphone first.");
        }
    }

    @Override
    public void stopRec() {
        if (_mic != null) {
            _mic.stopRec();
        }
    }

    @Override
    public void destroy() throws AudioException {
        AndroidLog.getLog().debug(
                this.getClass().getSimpleName() + ".destroy(): ");
        if (_speaker != null) {
            _speaker.destroy();
            _speaker = null;
        }
        if (_mic != null) {
            _mic.destroy();
            _mic = null;
        }
    }

    @Override
    public Properties getAudioProperties() {
        return new Properties(_audioProperties);
    }

    @Override
    public boolean setAudioProperty(String name, Object value)
            throws IllegalArgumentException {
        AndroidLog.getLog().debug(
                this.getClass().getSimpleName() + ".setAudioProperty(): name="
                + name + ", value=" + value);
        _audioProperties.put(name, value);
        return false;
    }

    @Override
    public double[] getEnergy() {
        double[] ret = new double[2];
        ret[0] = 0;
        ret[1] = 0;
        if (_mic != null) {
            ret[0] = _mic.getInEnergy();
        }
        if (_speaker != null) {
            ret[1] = _speaker.getOutEnergy();

        }
        return ret;
    }

    @Override
    public void updateRemoteStats(NetStatsFace r) {
        // not implemented yet
    }

    @Override
    public boolean doVAD() {
        return false;
    }

    /**
     * can be over-ridden to manipulate the codec availability and order
     * current implementation looks like this:
     * <pre>
          protected void fillCodecMap() {
        // add all the supported Codecs, in the order of preference
        Ulaw ulaw = new Ulaw();
        GSM_Codec gsmCodec = new GSM_Codec();
        CodecFace speex = null;
        try {
            speex = new NativeSpeexCodec();
            AndroidLog.getLog().debug(
                    this.getClass().getSimpleName() + "fillCodecMap: " + "got native speex codec");

        } catch (Throwable thrown) {
            speex = new SpeexCodec();
            AndroidLog.getLog().debug(
                    this.getClass().getSimpleName() + "fillCodecMap: " + "using java speex codec");
        }
        _codecMap.put(MediaFormat.ULAW, ulaw);
        _codecMap.put(MediaFormat.GSM, gsmCodec);
        _codecMap.put(MediaFormat.SPEEX, speex);

        _defaultCodec = ulaw;
    }
     </pre>
     */
    protected void fillCodecMap() {
        // add all the supported Codecs, in the order of preference
        Ulaw ulaw = new Ulaw();
        GSM_Codec gsmCodec = new GSM_Codec();
        CodecFace speex = null;
        try {
            speex = new NativeSpeexCodec();
            AndroidLog.getLog().debug(
                    this.getClass().getSimpleName() + "fillCodecMap: " + "got native speex codec");

        } catch (Throwable thrown) {
            speex = new SpeexCodec();
            AndroidLog.getLog().debug(
                    this.getClass().getSimpleName() + "fillCodecMap: " + "using java speex codec");
        }
        _codecMap.put(MediaFormat.ULAW, ulaw);
        _codecMap.put(MediaFormat.GSM, gsmCodec);
        _codecMap.put(MediaFormat.SPEEX, speex);

        _defaultCodec = ulaw;
    }

    private CodecFace getDefaultCodec() {
        return _defaultCodec;
    }

    public AudioTrack getAudioTrack() {
        AudioTrack ret = null;
        if (_speaker != null) {
            ret = _speaker.getAudioTrack();
        }
        return ret;

    }
}
