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
package com.phonefromhere.android.iax;

import java.io.IOException;

import com.phonefromhere.android.iax.net.TransmitterReceiver;
import com.phonefromhere.android.util.IaxLog;
import com.phonefromhere.softphone.AudioException;
import com.phonefromhere.softphone.AudioFace;
import com.phonefromhere.softphone.GenericSoftphone;
import com.phonefromhere.softphone.PhoneListener;

public class AndroidPhoneIax implements GenericSoftphone {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: AndroidPhoneIax.java,v 1.15 2011/03/23 12:00:39 uid100 Exp $ Copyright Westhawk Ltd";

    private PhoneListener _phoneListener = null;
    private String _host = null;
    private String _username = null;
    private String _password = null;
    private AudioFace _audio = null;
    private TransmitterReceiver _transmitter = null;
    private CallLeg _callLeg = null;
    private boolean _isEncrypted = false;
    private String _version = "$Revision: 1.16 $";
    

   
   
    
   
    public AndroidPhoneIax() {
    }

    public void setAudioFace(AudioFace audio) {
        _audio = audio;
    }

    public String getVersion(){
        return _version;
    }

    @Override
    public void setHost(String host) {
        _host = host;
    }

    @Override
    public void setUsername(String username) {
        _username = username;
    }

    @Override
    public void setPassword(String password) {
        _password = password;
    }

    @Override
    public void initialize() {
        try {
            _transmitter = new TransmitterReceiver(_host);
        } catch (IOException exc) {
            IaxLog.getLog().error(exc);
        }
    }

    @Override
    public void dial(String no, String callingNumber, String callingName) {
        String errorMessageStart = this.getClass().getSimpleName()
                + ".dial(): Cannot dial " + no + ": set ";
        if (_transmitter != null) {
            if (_audio != null) {
                if (_username != null) {
                    if (_password != null) {
                        _isEncrypted = false;
                        _callLeg = _transmitter.newCallLeg(_audio);
                        _callLeg.setPhoneListener(_phoneListener);
                        _callLeg.newCall(_username, _password, no,
                                callingNumber, callingName);
                    } else {
                        IaxLog.getLog().error(
                                errorMessageStart + "password first.");
                    }
                } else {
                    IaxLog.getLog()
                            .error(errorMessageStart + "username first.");
                }
            } else {
                IaxLog.getLog().error(errorMessageStart + "audio first.");
            }
        } else {
            IaxLog.getLog().error(
                    errorMessageStart + "host first and initialize().");
        }
    }

    @Override
    public void cryptoDial(String no, String callerid, String callername) {
        throw new UnsupportedOperationException();
        // _isEncrypted = true;
    }

    @Override
    public void setPhoneListener(PhoneListener phoneListener) {
        _phoneListener = phoneListener;
        if (_callLeg != null) {
            _callLeg.setPhoneListener(_phoneListener);
        }
    }

    @Override
    public void hangup() {
        if (_callLeg != null) {
            _callLeg.sendHangup();
        }
    }

    @Override
    public void destroy() {
        hangup();
        if (_transmitter != null) {
            _transmitter.destroy();
        }
        if (_audio != null) {
            try {
                _audio.destroy();
            } catch (AudioException exc) {
                IaxLog.getLog().debug(
                        this.getClass().getSimpleName() + ".destroy(): "
                                + exc.getMessage());
            }
        }
    }

    @Override
    public boolean inCall() {
        boolean isInCall = false;
        if (_callLeg != null && _callLeg.isFinished() == false) {
            isInCall = true;
        }
        return isInCall;
    }

    @Override
    public boolean callIsRinging() {
        boolean isRinging = false;
        if (_callLeg != null && _callLeg.isRinging()) {
            isRinging = true;
        }
        return isRinging;
    }

    @Override
    public boolean callIsAnswered() {
        boolean isAnswered = false;
        if (_callLeg != null && _callLeg.getState() == CallLegState.UP) {
            isAnswered = true;
        }
        return isAnswered;
    }

    @Override
    public void sendText(String text) {
        if (_callLeg != null) {
            _callLeg.sendText(text);
        }
    }

    @Override
    public void sendDTMF(String dtmfString) {
        if (_callLeg != null && dtmfString != null) {
            _callLeg.sendDtmf(dtmfString.charAt(0));
            if (dtmfString.length() > 1) {
                IaxLog.getLog().error(
                        this.getClass().getSimpleName()
                                + ".sendDTMF(): only sending 1st digit of "
                                + dtmfString);
            }
        }
    }

    @Override
    public String getCodec() {
        String codecName = null;
        if (_audio != null) {
            codecName = _audio.getCodecName();
        }
        return codecName;
    }

    @Override
    public boolean callIsAudioUp() {
        boolean isAudioUp = false;
        if (_audio != null) {
            isAudioUp = _audio.isAudioUp();
        }
        return isAudioUp;
    }

    @Override
    public void muteMic(boolean mute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAudioProperty(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean callIsEncrypted() {
        return _isEncrypted;
    }

    @Override
    public boolean callHasECon() {
        return false;
    }

    @Override
    public boolean callHasVAD() {
        boolean hadVAD = false;
        if (_audio != null) {
            hadVAD = _audio.doVAD();
        }
        return hadVAD;
    }

    @Override
    public double[] getEnergy() {
        return _audio.getEnergy();
    }

    @Override
    public int getVADpc() {
        return _audio.getVADpc();
    }

    @Override
    public void answer(short scno) {
        // TODO - Only with incoming calls
    }

    @Override
    public void reject(short scno) {
        // TODO - Only with incoming calls
    }

    @Override
    public void busy(short scno) {
        // TODO - Only with incoming calls
    }

    @Override
    public void initializeRegistration(boolean doEncryption) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGain(double gain) {
        throw new UnsupportedOperationException();
    }

    /**
     * The other end hung up.
     */
    @Override
    public void hungup() {
        // TODO Auto-generated method stub
    }
   
}
