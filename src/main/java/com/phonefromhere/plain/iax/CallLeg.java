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
package com.phonefromhere.plain.iax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import com.phonefromhere.plain.codec.MediaFormat;
import com.phonefromhere.plain.iax.frames.DtmfFrame;
import com.phonefromhere.plain.iax.frames.Frame;
import com.phonefromhere.plain.iax.frames.FrameType;
import com.phonefromhere.plain.iax.frames.FullFrame;
import com.phonefromhere.plain.iax.frames.MiniFrame;
import com.phonefromhere.plain.iax.frames.TextFrame;
import com.phonefromhere.plain.iax.frames.VoiceFrame;
import com.phonefromhere.plain.iax.frames.control.ControlFrame;
import com.phonefromhere.plain.iax.frames.control.ControlSubclass;
import com.phonefromhere.plain.iax.frames.iax.HungupListener;
import com.phonefromhere.plain.iax.frames.iax.IaxAckFrame;
import com.phonefromhere.plain.iax.frames.iax.IaxFrame;
import com.phonefromhere.plain.iax.frames.iax.IaxHangupFrame;
import com.phonefromhere.plain.iax.frames.iax.IaxNewFrame;
import com.phonefromhere.plain.iax.frames.iax.IaxSubclass;
import com.phonefromhere.plain.iax.frames.iax.ie.AuthMethodType;
import com.phonefromhere.plain.iax.frames.iax.ie.CauseCodeType;
import com.phonefromhere.plain.iax.net.TransmitterReceiver;
import com.phonefromhere.plain.util.IaxLog;
import com.phonefromhere.softphone.AudioException;
import com.phonefromhere.softphone.AudioFace;
import com.phonefromhere.softphone.AudioReceiver;
import com.phonefromhere.softphone.PhoneListener;
import com.phonefromhere.softphone.StampedAudio;
import com.phono.srtplight.Log;


/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 *
6.2.1.  Overview

The IAX protocol can be used to set up 'links' or 'call legs' between
two peers for the purposes of placing a call.  The process,
illustrated in Figure 2 and Figure 3, starts when a peer sends a NEW
message indicating the destination 'number' (or name) of a Called
Party on the remote peer.  

The remote peer can respond with either a
credentials challenge (AUTHREQ), a REJECT message, or an ACCEPT
message.  The AUTHREQ message indicates the permitted authentication
schemes and SHOULD result in the sending of an AUTHREP message with
the requested credentials.  The REJECT message indicates the call
cannot be established at this time.  

ACCEPT indicates that the call
leg between these two peers is established and that higher-level call
signaling (Section 6.3) MAY proceed.  After sending or receiving the
ACCEPT message, the call leg is in the 'Linked' state and is used to
pass call control messages until the call is completed.  Further
detail on messages used for this process can be found in Section 6.3.

Call legs are labeled with a pair of identifiers.  Each end of the
call leg assigns the source or destination identifier during the call
leg creation process.
 */
public class CallLeg implements HungupListener, AudioReceiver {

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: CallLeg.java,v 1.12 2011/03/16 13:58:44 uid100 Exp $ Copyright Westhawk Ltd";
    public final static int MAX_RETRIES = 4;
    public final static int RETRY_INTERVAL_MS = 600;
    //private static final Object LIST_LOCK = new Object();
    private CallLegState _state = CallLegState.INITIAL;
    protected short _sourceIdentifier = 0;
    protected short _destIdentifier = 0;
    private AudioFace _audio; // can remain null!
    protected TransmitterReceiver _transmitter;
    protected PhoneListener _phoneListener;
    private List<Frame> _framesToSendList;
    /*
     * The outgoing seq number == outgoing message count
     */
    private short _outgoingMessageCount = 0;

    /*
     * The incoming seq number == highest numbered incoming message that has
     * been received.
     */
    private short _incomingMaxNo = 0;
    private long _lastReceivedTimestamp = 0;
    /*
     * These two parameters invalid the CallLeg. I want to hang on to them for a
     * while, to make sure I don't reuse the _sourceIdentifier.
     */
    private boolean _isFinished = false;
    private long _finishedAtTimeMilli = 0;
    private Collection<MediaFormat> _formats = null;
    protected String _username = null;
    protected String _password = null;
    private String _calledNumber;
    private String _callingNumber;
    private String _callingName;
    private boolean _isRinging;
    private boolean _isFirstAudio = true;
    private MiniFrame _miniFrameOut;

    public CallLeg(TransmitterReceiver transmitter, short sourceIdentifier) {
        IaxLog.getLog().debug(this.getClass().getSimpleName()
                + ".CallLeg(): sourceIdentifier=" + sourceIdentifier);
        _transmitter = transmitter;
        this.setSourceIdentifier(sourceIdentifier);
        _miniFrameOut = new MiniFrame(this._sourceIdentifier);

        _framesToSendList = new ArrayList<Frame>();
    }

    protected void setStateInitial(String errorMessage) {
        if (_state == null || _state != CallLegState.INITIAL) {
            _isRinging = false;
            this.setFinished("setStateInitial(): " + errorMessage);
            this.setState(CallLegState.INITIAL);
            if (_audio != null) {
                _audio.stopPlay();
                _audio.stopRec();
            }
            if (_phoneListener != null) {
                _phoneListener.statusChangedEvent(PhoneListener.HOOK,
                        errorMessage);
            }
        }
    }

    protected void setStateWaiting() {
        if (_state == null || _state != CallLegState.WAITING) {
            _isRinging = false;
            this.setState(CallLegState.WAITING);
        }
    }

    protected void setStateLinked() {
        if (_state == null || _state != CallLegState.LINKED) {
            _isRinging = false;
            this.setState(CallLegState.LINKED);
        }
    }

    protected void setStateUp() {
        if (_state == null || _state != CallLegState.UP) {
            _isRinging = false;
            this.setState(CallLegState.UP);
            if (_audio != null) {
                _audio.startPlay();
                _audio.startRec();
            }
            if (_phoneListener != null) {
                _phoneListener.statusChangedEvent(PhoneListener.CONNECTED, null);
            }
        }
    }

    private void setState(CallLegState state) {
        _state = state;
    }

    public CallLegState getState() {
        return _state;
    }

    protected void setSourceIdentifier(short sourceIdentifier) {
        _sourceIdentifier = sourceIdentifier;
    }

    public short getSourceIdentifier() {
        return _sourceIdentifier;
    }

    public void setDestIdentifier(short destIdentifier) {
        IaxLog.getLog().debug(this.getClass().getSimpleName()
                + ".setDestIdentifier(): _sourceIdentifier="
                + _sourceIdentifier + ", destIdentifier=" + destIdentifier);

        _destIdentifier = destIdentifier;
    }

    public short getDestIdentifier() {
        return _destIdentifier;
    }

    public boolean isRinging() {
        return _isRinging;
    }

    public void setPhoneListener(PhoneListener phoneListener) {
        _phoneListener = phoneListener;
    }

    public void setAudioFace(AudioFace audio) {
        // Note, _audio can be null!
        _audio = audio;
        if (_audio != null) {
            _formats = new LinkedHashSet<MediaFormat>();
            long[] codecs = _audio.getCodecs();
            for (int i = 0; i < codecs.length; i++) {
                long codec = codecs[i];
                MediaFormat format = VoiceFrame.findMediaFormat(codec);
                _formats.add(format);
            }

            try {
                _audio.addAudioReceiver(this);
            } catch (AudioException exc) {
                IaxLog.getLog().error(exc.toString());
            }
        }
    }

    public boolean newCall(String username, String password,
            String calledNumber, String callingNumber, String callingName) {
        boolean isOK = false;
        if (!_isFinished) {
            if (_state == CallLegState.INITIAL) {
                isOK = true;
                _username = username;
                _password = password;
                _calledNumber = calledNumber;
                _callingNumber = callingNumber;
                _callingName = callingName;
                sendNewFrame("");
                this.setStateWaiting();
                isOK = true;
            } else {
                logInvalidStateFrameSend("NewFrame");
            }
        } else {
            IaxLog.getLog().debug(this.getClass().getSimpleName()
                    + ".newCall(): not making new call: isTornDown="
                    + _isFinished);
        }
        return isOK;
    }

    public void sendHangup() {
        sendHangupFrame(CauseCodeType.NORMAL_CLEARING);
    }

    public boolean sendDtmf(char digit) {
        return sendDtmfFrame(digit);
    }

    public boolean sendText(String text) {
        return sendTextFrame(text);
    }

    // Only queues frame. Actual send will happen in sendFrames()
    protected void queueFrame(Frame frame) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".queueFrame(): "
        // + frame.toString());
        updateCountersOnOutgoing(frame);
        if (frame.isExpectingAnswer()) {
            synchronized (_framesToSendList) {
                _framesToSendList.add(frame); //queue retries
            }
        }
        _transmitter.sendFrame(frame); // sends the first time
        frame.setSendAt(System.currentTimeMillis());
        frame.incrementRetries();
    }

    public void sendRetryFrames() {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".sendFrames(): ");
        synchronized (_framesToSendList) {
            Iterator<Frame> iter = _framesToSendList.iterator();
            while (iter.hasNext() && _isFinished == false) {
                Frame frame = iter.next();

                if (frame.isAcknowlegded()) {
                    // we've got an answer, don't send again
                    iter.remove();
                } else {
                    if (frame.getRetries() > MAX_RETRIES) {
                        /*
                         * If no acknowledgment is received after a locally
                         * configured number of retries (default 4), the call
                         * leg SHOULD be considered unusable and the call MUST
                         * be torn down without any further interaction on this
                         * call leg.
                         */
                        IaxLog.getLog().debug(this.getClass().getSimpleName() + ".sendFrames(): exceeds MAX_RETRIES "
                                + frame.toString());
                        handleTimeout(false);
                    } else {
                        long now = System.currentTimeMillis();

                        boolean doActuallySendNow = false;
                        // only deal with retries here
                        if (frame.getRetries() != 0) {
                            // wait till the retry interval has passed
                            int diff = (int) (now - frame.getSendAt());
                            if (diff >= RETRY_INTERVAL_MS) {
                                doActuallySendNow = true;
                            }
                        }

                        if (doActuallySendNow) {
                            _transmitter.sendFrame(frame);
                            frame.setSendAt(now);
                            frame.incrementRetries();
                        }
                    }
                }
            }
        } // end while

        if (_isFinished) {
            clear();
        }
    }

    public void startRetry() {
        Runnable retry = new Runnable() {

            @Override
            public void run() {
                while (!_isFinished) {
                    try {
                        sendRetryFrames();
                        Thread.sleep(RETRY_INTERVAL_MS / 2);
                    } catch (InterruptedException ex) {
                        ; // who cares ?
                    }
                }
            }
        };
        Thread rt = new Thread(retry, "call_" + _sourceIdentifier + "_retry");
        rt.start();
    }

    public void receivedFrame(Frame frame) {
        if (_isFinished) {
            sendInvalFrame();
        } else {
            // handle/process frame
            if (frame.isFullFrame()) {
                IaxLog.getLog().debug(this.getClass().getSimpleName() + ".receivedFrame(): "
                        + frame.toString());
                FullFrame fullFrame = (FullFrame) frame;
                boolean processThisFrame = updateCountersOnIncoming(fullFrame);
                if (fullFrame.mustSendAck()) {
                    IaxAckFrame ack = new IaxAckFrame(_sourceIdentifier);
                    ack.setTimeStamp(fullFrame.getTimeStamp());
                    this.queueFrame(ack);
                }

                if (processThisFrame) {
                    FrameType frameType = fullFrame.getFrameType();
                    switch (frameType) {
                        case VOICE:
                            receivedVoiceFrame((VoiceFrame) frame);
                            break;
                        case DTMF:
                            receivedDtmfFrame((DtmfFrame) frame);
                            break;
                        case TEXT:
                            receivedTextfFrame((TextFrame) frame);
                            break;
                        case CONTROL:
                            receivedControlFrame((ControlFrame) frame);
                            break;
                        case IAX:
                            receivedIaxFrame((IaxFrame) frame);
                            break;
                        default:
                            IaxLog.getLog().error(this.getClass().getSimpleName()
                                    + ".receivedFrame(): unexpected frame type "
                                    + frameType.toString());
                    }
                }
            } else {
                receivedMiniFrame((MiniFrame) frame);
            }
        }
    }

    @Override
    // AudioReceiver
    public void newAudioDataReady(AudioFace audioFace, int bytesAvailable) {
        Log.verb("new Audio data ready");
        try {
            StampedAudio stampedAudio = audioFace.readStampedAudio();

            while (stampedAudio != null) {
                Frame frame = null;
                if (_state == CallLegState.UP) {
                    // Always start with a Full Voice Frame, then
                    // move over to Mini Frames
                    if (_isFirstAudio) {
                        long codecValue = audioFace.getCodec();
                        MediaFormat format = VoiceFrame.findMediaFormat(codecValue);
                        frame = new VoiceFrame(this._sourceIdentifier, format);
                        _isFirstAudio = false;
                        frame.setTimeStamp(stampedAudio.getStamp());
                        frame.setData(stampedAudio.getData());
                        this.queueFrame(frame);
                    } else {
                        _miniFrameOut.setData(stampedAudio.getData());
                        _miniFrameOut.setTimeStamp(stampedAudio.getStamp());
                        _transmitter.sendFrame(_miniFrameOut);

                        //this.queueFrame(frame);
                        // skip the queue and send directly
                        // exactly one outbound Miniframe
                    }

                } else {
                    // ignore the data, but keep reading
                }
                audioFace.releaseStampedAudio(stampedAudio);
                stampedAudio = audioFace.readStampedAudio();
            }
        } catch (AudioException exc) {
            IaxLog.getLog().error(exc.toString());
        }
    }

    /*
     * 7. Message Transport
     * 
     * When starting a call, the outgoing and incoming message sequence numbers
     * MUST both be set to zero.
     * 
     * The message includes the outgoing message count and the highest numbered
     * incoming message that has been received.
     * 
     * In addition, it contains a time-stamp that represents the number of
     * milliseconds since the call started. Or, in the case of certain network
     * timing messages, it contains a copy of the time-stamp sent to it.
     * Time-stamps MAY be approximate, but, MUST be in order.
     */
    protected void updateCountersOnOutgoing(Frame outgoingFrame) {
        if (outgoingFrame.isFullFrame()) {
            FullFrame outgoingFullFrame = (FullFrame) outgoingFrame;
            outgoingFullFrame.setDestinationCallNumber(this._destIdentifier);
            outgoingFullFrame.setISeqNo(this._incomingMaxNo);
            outgoingFullFrame.setOSeqNo(this._outgoingMessageCount);

            boolean doIncreaseOSeqNo = doIncrementMessageCount(outgoingFullFrame);
            if (doIncreaseOSeqNo) {
                this._outgoingMessageCount++;
            }
        }
    }

    /*
     * 7. Message Transport
     * 
     * Each reliable message that is sent increments the message count by one
     * except the ACK, INVAL, TXCNT, TXACC, and VNAK messages, which do not
     * change the message count.
     */
    @SuppressWarnings("incomplete-switch")
    private boolean doIncrementMessageCount(FullFrame fullFrame) {
        boolean doIncrement = true;
        if (fullFrame.getFrameType() == FrameType.IAX) {
            IaxFrame iaxFrame = (IaxFrame) fullFrame;
            IaxSubclass subclassI = iaxFrame.getSubClassI();
            switch (subclassI) {
                case ACK:
                case INVAL:
                case TXCNT:
                case TXACC:
                case VNAK:
                case CALLTOKEN:
                    doIncrement = false;
                    break;
            }
        }
        // IaxLog.getLog().iax(this.getClass().getSimpleName() +
        // ".doIncrementMessageCount(): " + doIncrement);
        return doIncrement;
    }

    /*
     * 7. Message Transport
     * 
     * When starting a call, the outgoing and incoming message sequence numbers
     * MUST both be set to zero.
     * 
     * When any message is received, the time-stamps MUST be checked to make
     * sure that they are in order. If a message is received out of order, it
     * MUST be ignored and a VNAK message sent to resynchronize the peers.
     * 
     * If the message is a reliable message, the incoming message counter MUST
     * be used to acknowledge all the messages up to that sequence number that
     * have been sent.
     */
    protected boolean updateCountersOnIncoming(FullFrame incomingFullFrame) {
        boolean processThisFrame = true;

        // their OSeq should be equal to my_incomingMaxNo
        // else some frames didn't arrive here, or not in the correct order
        short theirOSeqNo = incomingFullFrame.getOSeqNo();
        if (doIncrementMessageCount(incomingFullFrame)) {
            if (theirOSeqNo == _incomingMaxNo) {
                _incomingMaxNo++;
            } else if (theirOSeqNo > _incomingMaxNo) {
                IaxLog.getLog().debug(this.getClass().getSimpleName()
                        + ".updateCountersOnIncoming(): oSeqNo problem: theirOSeqNo="
                        + theirOSeqNo + " > _incomingMaxNo=" + _incomingMaxNo);

                processThisFrame = false;
                sendVNakFrame();
            } else {
                // we've seen this one, probably an old retry?
                processThisFrame = false;

                IaxLog.getLog().debug(this.getClass().getSimpleName()
                        + ".updateCountersOnIncoming(): oSeqNo problem (ignore): theirOSeqNo="
                        + theirOSeqNo + " < _incomingMaxNo=" + _incomingMaxNo);
            }
        }

        if (processThisFrame) {
            if (this._destIdentifier == 0) {
                // My destination number/identifier isn't set yet, initialise
                // Their source call number == our destination call number
                short theirSourceNumber = incomingFullFrame.getSourceCallNumber();
                this.setDestIdentifier(theirSourceNumber);
            }

            // The incoming seq number == highest numbered incoming message that
            // they have received
            short theirISeqNo = incomingFullFrame.getISeqNo();

            // Acknowledge everything up to & including their iSeqNo
            synchronized (_framesToSendList) {
                Iterator<Frame> iter = _framesToSendList.iterator();
                while (iter.hasNext() && _isFinished == false) {
                    Frame frame = iter.next();
                    if (frame.isFullFrame()) {
                        FullFrame fullFrame = (FullFrame) frame;
                        if (fullFrame.getOSeqNo() <= theirISeqNo) {
                            fullFrame.setAcknowlegded(true);
                        }
                    }
                }
            }
        }
        if (processThisFrame == false) {
            IaxLog.getLog().debug(this.getClass().getSimpleName()
                    + ".updateCountersOnIncoming(): "
                    + incomingFullFrame.toShortString() + ", "
                    + processThisFrame);
        }
        return processThisFrame;
    }

    protected void receivedMiniFrame(MiniFrame miniFrame) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".receivedMiniFrame(): ");

        if (_state == CallLegState.UP) {
            this.playMedia(miniFrame);
        } else {
            logInvalidStateFrameReceived(miniFrame);
        }
    }

    protected void receivedVoiceFrame(VoiceFrame voiceFrame) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".receivedVoiceFrame(): ");

        if (_state == CallLegState.UP) {
            this.playMedia(voiceFrame);
        } else {
            logInvalidStateFrameReceived(voiceFrame);
        }
    }

    protected void playMedia(Frame frame) {
        if (_audio != null) {
            byte[] data = frame.getData();
            StampedAudio stampedAudio = _audio.getCleanStampedAudio();
            if (data != null && stampedAudio != null) {
                stampedAudio.setStampAndBytes(data, 0, data.length,
                        (int) frame.getTimeStamp());
                try {
                    _audio.writeStampedAudio(stampedAudio);
                } catch (AudioException exc) {
                    IaxLog.getLog().error(exc.toString());
                }
            }
        }
    }

    protected boolean sendDtmfFrame(char digit) {
        boolean isOK = false;
        if (_state == CallLegState.UP) {
            DtmfFrame dtmfFrame = new DtmfFrame(this._sourceIdentifier);
            dtmfFrame.setDtmfDigit(digit);
            this.queueFrame(dtmfFrame);
            isOK = true;
        } else {
            logInvalidStateFrameSend("DtmfFrame");
        }
        return isOK;
    }

    protected void receivedDtmfFrame(DtmfFrame dtmfFrame) {
        IaxLog.getLog().debug(this.getClass().getSimpleName() + ".receivedDtmfFrame(): ");

        if (_state == CallLegState.UP) {
            String digit = "" + dtmfFrame.getDtmfDigit();
            if (_phoneListener != null) {
                _phoneListener.rcvdDTMF(digit);
            }
        } else {
            logInvalidStateFrameReceived(dtmfFrame);
        }
    }

    protected boolean sendTextFrame(String text) {
        boolean isOK = false;
        if (_state == CallLegState.UP) {
            TextFrame textFrame = new TextFrame(this._sourceIdentifier);
            textFrame.setText(text);
            this.queueFrame(textFrame);
            isOK = true;
        } else {
            logInvalidStateFrameSend("TextFrame");
        }
        return isOK;
    }

    protected void receivedTextfFrame(TextFrame textFrame) {
        IaxLog.getLog().debug(this.getClass().getSimpleName() + ".receivedTextfFrame(): ");

        if (_state == CallLegState.UP) {
            String text = textFrame.getText();
            if (_phoneListener != null) {
                _phoneListener.rcvdText(text);
            }
        } else {
            logInvalidStateFrameReceived(textFrame);
        }
    }

    protected void receivedControlFrame(ControlFrame controlFrame) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() +
        // ".receivedControlFrame(): ");

        ControlSubclass subclass = controlFrame.getSubClassC();
        if (subclass != null) {
            switch (subclass) {
                case ANSWER:
                    // state Linked -> Up
                    if (_state == CallLegState.LINKED) {
                        this.setStateUp();
                        if (_phoneListener != null) {
                            _phoneListener.statusChangedEvent(
                                    PhoneListener.CONNECTED, null);
                        }
                    } else {
                        logInvalidStateFrameReceived(controlFrame);
                    }
                    break;
                case BUSY:
                    // should only happen in Linked state??
                    if (_state == CallLegState.LINKED) {
                    } else {
                        logInvalidStateFrameReceived(controlFrame);
                    }
                    break;
                case CALL_PROCEEDING:
                    // should only happen in Linked state
                    if (_state == CallLegState.LINKED) {
                    } else {
                        logInvalidStateFrameReceived(controlFrame);
                    }
                    break;
                case CALL_PROGRESS:
                    break;
                case CONGESTION:
                    break;
                case FLASH_HOOK:
                    break;
                case HANGUP:
                    // go back to Initial state
                    setStateInitial(null);
                    break;
                case HOLD:
                    break;
                case KEY_RADIO:
                    break;
                case OPTION:
                    break;
                case RESERVED1:
                    break;
                case RESERVED2:
                    break;
                case RESERVED3:
                    break;
                case RESERVED4:
                    break;
                case RINGING:
                    // should only happen in Linked state
                    if (_state == CallLegState.LINKED) {
                        _isRinging = true;
                        if (_phoneListener != null) {
                            _phoneListener.statusChangedEvent(
                                    PhoneListener.RINGING, null);
                        }
                    } else {
                        logInvalidStateFrameReceived(controlFrame);
                    }
                    break;
                case UNHOLD:
                    break;
                case UNKEY_RADIO:
                    break;
                case SRCUPDATE:
                    break;
                case T38:
                    break;
                case VIDUPDATE:
                    break;
                case STOPSOUNDS:
                    break;
            }
        } else {
            // int subclassi = (int) controlFrame.getSubClass();
            // Actually, don't send this. It messes up the communication
            // Just ignore it.
            // sendUnsupportedFrame(subclassi);
        }
    }

    protected void receivedIaxFrame(IaxFrame iaxFrame) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".receivedIaxFrame(): ");

        IaxSubclass subclass = iaxFrame.getSubClassI();
        if (subclass != null) {
            switch (subclass) {
                case ACCEPT:
                    // state Waiting -> Linked
                    if (_state == CallLegState.WAITING) {
                        MediaFormat format = iaxFrame.getFormat2();
                        if (format == null) {
                            format = iaxFrame.getFormat();
                        }
                        try {
                            _audio.init(format.getValue(), 0);
                            this.setStateLinked();
                        } catch (AudioException exc) {
                            // no can do, send HANGUP
                            IaxLog.getLog().error(exc.toString());
                            this.sendHangupFrame(CauseCodeType.FACILITY_REJECTED);
                        }
                    } else {
                        logInvalidStateFrameReceived(iaxFrame);
                    }
                    break;
                case ACK:
                    // IaxAckFrame ackFrame = (IaxAckFrame) iaxFrame;
                    break;
                case AUTHREP:
                    // ignoring - we're not (yet) receiving incoming calls
                    break;
                case AUTHREQ:
                    if (_state == CallLegState.WAITING) {
                        String username = iaxFrame.getUsername();
                        List<AuthMethodType> authMethods = iaxFrame.getSupportedAuthMethods();
                        String challenge = iaxFrame.getChallenge();
                        receivedAuthRequest(username, authMethods, challenge);
                    } else {
                        logInvalidStateFrameReceived(iaxFrame);
                    }
                    break;
                case CALLTOKEN:
                    receivedCallToken(iaxFrame);
                    break;
                case DIAL:
                    break;
                case DPREP:
                    break;
                case DPREQ:
                    break;
                case HANGUP:
                    // go back to Initial state
                    IaxHangupFrame hangupFrame = (IaxHangupFrame) iaxFrame;
                    setStateInitial(hangupFrame.getCause());
                    break;
                case INVAL:
                    break;
                case LAGRP:
                    break;
                case LAGRQ:
                    // answer with LAGRP
                    IaxFrame lagrpFrame = new IaxFrame(this._sourceIdentifier,
                            IaxSubclass.LAGRP);
                    lagrpFrame.setTimeStamp(iaxFrame.getTimeStamp());
                    queueFrame(lagrpFrame);
                    break;
                case MWI:
                    break;
                case NEW:
                    // ignoring - we're not (yet) receiving incoming calls
                    // IaxNewFrame newFrame = (IaxNewFrame) iaxFrame;
                    break;
                case PING:
                    // answer with PONG
                    sendPongFrame(iaxFrame);
                    break;
                case POKE:
                    // answer with PONG
                    sendPongFrame(iaxFrame);
                    break;
                case PONG:
                    break;
                case QUELCH:
                    break;
                case REGACK:
                    // To be handled by RegistrationCallLeg
                    break;
                case REGAUTH:
                    // To be handled by RegistrationCallLeg
                    break;
                case REGREJ:
                    // To be handled by RegistrationCallLeg
                    break;
                case REGREL:
                    // To be handled by RegistrationCallLeg
                    break;
                case REGREQ:
                    // To be handled by RegistrationCallLeg
                    // Send REGREJ
                    break;
                case REJECT:
                    // state Waiting -> Initial
                    if (_state == CallLegState.WAITING) {
                        setStateInitial(iaxFrame.getCause());
                    } else {
                        logInvalidStateFrameReceived(iaxFrame);
                    }
                    break;
                case RESERVED1:
                    break;
                case RESERVED2:
                    break;
                case RESERVED3:
                    break;
                case RESERVED4:
                    break;
                case RTKEY:
                    break;
                case TRANSFER:
                    break;
                case TXACC:
                    break;
                case TXCNT:
                    break;
                case TXMEDIA:
                    break;
                case TXREADY:
                    break;
                case TXREJ:
                    break;
                case TXREL:
                    break;
                case TXREQ:
                    break;
                case UNQUELCH:
                    break;
                case UNSUPPORT:
                    break;
                case VNAK:
                    short theirISeqNo = iaxFrame.getISeqNo();
                    receivedVNakFrame(theirISeqNo);
                    break;
            }
        } else {
            // int subclassi = (int) iaxFrame.getSubClass();
            // Actually, don't send this. It messes up the communication
            // Just ignore it.
            // sendUnsupportedFrame(subclassi);
        }
    }

    protected void receivedCallToken(IaxFrame iaxFrame) {
        if (_state == CallLegState.WAITING) {
            String callToken = iaxFrame.getCallToken();
            // Send the NEW again, this time with this CallToken.
            sendNewFrame(callToken);
        } else {
            logInvalidStateFrameReceived(iaxFrame);
        }
    }

    public void destroy() {
        // remove every frame in the queue
        clear();
        // queue one more HANGUP
        this.sendHangup();
        // don't wait for ACK to set _isFinished
        setFinished("destroy");
    }

    protected void handleTimeout(boolean doClear) {
        this.setStateInitial("RETRANSMITS");
        if (doClear) {
            clear();
        }
    }

    protected void clear() {
        // Do NOT reset the Source/Destination Identifiers!
        synchronized (_framesToSendList) {
            _framesToSendList.clear();
        }
    }

    public boolean isFinished() {
        return _isFinished;
    }

    protected void setFinished(String message) {
        IaxLog.getLog().debug(this.getClass().getSimpleName()
                + ".setFinished(): _sourceIdentifier=" + this._sourceIdentifier
                + ", " + message);
        _isFinished = true;
        _finishedAtTimeMilli = new Date().getTime();
    }

    public long getFinishedOutAt() {
        return _finishedAtTimeMilli;
    }

    protected void sendNewFrame(String callToken) {
        IaxNewFrame newFrame = new IaxNewFrame(_sourceIdentifier,
                _calledNumber, _formats);
        newFrame.setCallingName(_callingName);
        newFrame.setCallingNumber(_callingNumber);
        newFrame.setCallToken(callToken);
        newFrame.setUsername(_username);
        // on a new, always reset the counters:
        this._destIdentifier = 0;
        this._incomingMaxNo = 0;
        this._outgoingMessageCount = 0;
        queueFrame(newFrame);
    }

    protected void sendPongFrame(IaxFrame incomingFrame) {
        IaxFrame pongFrame = new IaxFrame(this._sourceIdentifier,
                IaxSubclass.PONG);
        pongFrame.setTimeStamp(incomingFrame.getTimeStamp());
        queueFrame(pongFrame);
    }

    protected void sendHangupFrame(CauseCodeType type) {
        this.sendHangupFrame(type, null);
    }

    protected void sendHangupFrame(CauseCodeType type, String message) {
        IaxLog.getLog().debug(this.getClass().getSimpleName() + ".sendHangupFrame()");
        IaxHangupFrame hangupFrame = new IaxHangupFrame(_sourceIdentifier);
        hangupFrame.setCauseCode(type, message);
        hangupFrame.setHungupListener(this);
        this.queueFrame(hangupFrame);
    }

    /*
     * 6.9.5. UNSUPPORT Unsupported Response Message
     * 
     * An UNSUPPORT message is sent in response to a message that is not
     * supported by an IAX peer. This occurs when an IAX command with an
     * unrecognized or unsupported subclass is received. No action is required
     * upon receipt of this message, though the peer SHOULD be aware that the
     * message referred to in the optionally included 'IAX unknown' IE is not
     * supported by the remote peer.
     */
    protected void sendUnsupportedFrame(int subclass) {
        IaxFrame unsupportedFrame = new IaxFrame(this._sourceIdentifier,
                IaxSubclass.UNSUPPORT);
        unsupportedFrame.setUnknown(subclass);
        queueFrame(unsupportedFrame);
    }

    /*
     * 6.9.2. INVAL: Invalid Response Message
     * 
     * An INVAL is sent as a response to a received message that is not valid.
     * This occurs when an IAX peer sends a message on a call after the remote
     * peer has hung up its end. Upon receipt of an INVAL, a peer MUST destroy
     * its side of a call.
     * 
     * The INVAL message does not requires any IEs.
     */
    protected void sendInvalFrame() {
        IaxFrame invalFrame = new IaxFrame(this._sourceIdentifier,
                IaxSubclass.INVAL);
        queueFrame(invalFrame);
    }

    @Override
    // HungupListener
    public void hungup(CauseCodeType type, String message) {
        this.setStateInitial(message);
    }

    /*
     * 6.2.7. AUTHREQ Authentication Request Message
     * 
     * The AUTHREQ message is sent in response to a NEW message if
     * authentication is required for the call to be accepted. It MUST include
     * the 'authentication methods' and 'username' IEs, and the 'challenge' IE
     * if MD5 or RSA authentication is specified.
     * 
     * Upon receiving an AUTHREQ message, the receiver MUST respond with an
     * AUTHREP or HANGUP message.
     * 
     * 6.2.6. AUTHREP Authentication Reply Message
     * 
     * An AUTHREP MUST include the appropriate challenge response or password
     * IE, and is only sent in response to an AUTHREQ. An AUTHREP requires a
     * response of either an ACCEPT or a REJECT.
     * 
     * Typical reasons for rejecting an AUTHREP include 'destination does not
     * exist' and 'suitable bearer not found'.
     */
    protected void receivedAuthRequest(String username,
            List<AuthMethodType> authMethods, String challenge) {
        CauseCodeType type = null;
        String message = null;
        if (_username != null && _username.equals(username) == false) {
            type = CauseCodeType.FACILITY_NOT_SUBSCRIBED;
            message = "No username " + username;
            sendHangupFrame(type, message);
        } else {
            boolean isOK = false;
            IaxFrame authRepFrame = new IaxFrame(this._sourceIdentifier,
                    IaxSubclass.AUTHREP);
            if (authMethods.contains(AuthMethodType.MD5)) {
                isOK = authRepFrame.setMD5Result(challenge, _password);
            } else if (authMethods.contains(AuthMethodType.RSH)) {
                isOK = authRepFrame.setRSAResult(challenge, _password);
            }

            if (isOK) {
                this.queueFrame(authRepFrame);
            } else {
                // unlikely, but still
                type = CauseCodeType.FACILITY_NOT_SUBSCRIBED;
                sendHangupFrame(type);
            }
        }
    }

    /*
     * 6.9.3. VNAK: Voice Negative Acknowledgement Message
     * 
     * A VNAK is sent when a message is received out of order, particularly when
     * a Mini Frame is received before the first full voice frame on a call. It
     * is a request for retransmission of dropped messages. A message is
     * considered out of sequence if the received iseqno is different than the
     * expected iseqno. On receipt of a VNAK, a peer MUST retransmit all frames
     * with a higher sequence number than the VNAK message's iseqno.
     * 
     * The VNAK message does not requires any IEs.
     */
    protected void receivedVNakFrame(short theirISeqNo) {
        IaxLog.getLog().debug(this.getClass().getSimpleName()
                + ".receivedVNakFrame(): theirISeqNo=" + theirISeqNo);
        // all these frames should still be in the queue
        // make sure they get send ASAP
        long now = new Date().getTime();
        synchronized (_framesToSendList) {
            Iterator<Frame> iter = _framesToSendList.iterator();
            while (iter.hasNext()) {
                Frame frame = iter.next();
                if (frame.isFullFrame()) {
                    FullFrame fullFrame = (FullFrame) frame;
                    if (fullFrame.getOSeqNo() > theirISeqNo) {
                        // fake the last sendAt time, so they are send ASAP
                        fullFrame.setSendAt(now - RETRY_INTERVAL_MS - 10);
                    }
                }
            }
        }
    }

    protected void sendVNakFrame() {
        IaxFrame vnakFrame = new IaxFrame(_sourceIdentifier, IaxSubclass.VNAK);
        this.queueFrame(vnakFrame);
    }

    protected void logInvalidStateFrameReceived(Frame frame) {
        IaxLog.getLog().debug("Invalid state " + _state.toString() + " for received frame="
                + frame.toString());
    }

    protected void logInvalidStateFrameSend(String frame) {
        IaxLog.getLog().debug("Invalid state " + _state.toString() + " for sending frame="
                + frame);
    }

}
