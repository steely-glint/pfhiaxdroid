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
package com.phonefromhere.softphone;

/**
 * This interface should be implemented to be used by Phonefromhere.
 * 
 * @see Phonefromhere#Phonefromhere
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 1.2 $ $Date: 2011/02/10 16:16:25 $
 */
public interface GenericSoftphone {

    static final String version_id = "@(#)$Id: GenericSoftphone.java,v 1.2 2011/02/10 16:16:25 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * Get the current energy levels in and out.
     * 
     * @return
     */
    public double[] getEnergy();

    /**
     * Get codec name
     * 
     * @return
     */
    public String getCodec();

    public int getVADpc();

    /**
     * Adds the event handler. This has to be called before initialize().
     * 
     * @param pl
     *            The PhoneListener
     * @see #initialize()
     */
    public void setPhoneListener(PhoneListener pl);

    /**
     * Is there an active call?
     * 
     * @return boolean
     */
    public boolean inCall();

    /**
     * If there is a current active call, is it ringing?
     * 
     * @return boolean
     */
    public boolean callIsRinging();

    /**
     * If there is an active call, is it answered?
     * 
     * @return boolean
     */
    public boolean callIsAnswered();

    /**
     * If there is an active call, is it encrypted?
     * 
     * @return boolean
     */
    public boolean callIsEncrypted();

    /**
     * If there is an active call, is the audio running?
     * 
     * @return boolean
     */
    public boolean callIsAudioUp();

    /**
     * If there is an active call, is there an echo cancellation running?
     * 
     * @return boolean
     */
    public boolean callHasECon();

    /**
     * If there is an active call, is there an voice activity detection running?
     * 
     * @return boolean
     */
    public boolean callHasVAD();

    /**
     * Sets the username to use for this session. This has to be called before
     * initialize().
     * 
     * @param username
     *            The username
     * @see #initialize()
     */
    public void setUsername(String username);

    /**
     * Sets the password to use for this session. This has to be called before
     * initialize().
     * 
     * @param password
     *            The password
     * @see #initialize()
     */
    public void setPassword(String password);

    /**
     * Sets the host to use for this session. This has to be called before
     * initialize().
     * 
     * @param host
     *            The hostname of the PBX
     * @see #initialize()
     */
    public void setHost(String host);

    /**
     * Sets if we need to use echo cancellation (ie is the user using speakers?)
     * 
     * @param name
     * @param value
     */
    public void setAudioProperty(String name, String value);

    /**
     * Hangs up the current active call.
     */
    public void hangup();

    /**
     * The other end hung up.
     */
    public void hungup();

    /**
     * Dials a number, using the callerid and callername.
     * 
     * @param no
     *            The number to dial
     * @param callerid
     *            The caller id
     * @param callername
     *            The caller name
     */
    public void dial(String no, String callerid, String callername);

    /**
     * Dials a number securely (encrypting the audio at least).
     * 
     * @param no
     *            The number to dial
     * @param callerid
     *            The caller id
     * @param callername
     *            The caller name
     */
    public void cryptoDial(String no, String callerid, String callername);

    /**
     * The user answers an incoming call with a particular scallno.
     * 
     * @param scno
     *            The source call number of the incoming call
     */
    public void answer(short scno);

    /**
     * The user rejects an incoming call with a particular scallno.
     * 
     * @param scno
     *            The source call number of the incoming call
     */
    public void reject(short scno);

    /**
     * Tell the other side that the user is busy and cannot answer an incoming
     * call with a particular scallno.
     * 
     * @param scno
     *            The source call number of the incoming call
     */
    public void busy(short scno);

    /**
     * Sends a text string to the far end.
     * 
     * @param text
     *            The text to send
     */
    public void sendText(String text);

    /**
     * Sends a single DTMF digit to the far end.
     * 
     * @param digit
     *            The single DTMF digit to send
     */
    public void sendDTMF(String digit);

    /**
     * Called once (and only once) to set up all the parameters. Make sure the
     * phone listener, the username, the password and the hostname are setup
     * before calling this method.
     * 
     * @see #setPhoneListener(PhoneListener)
     * @see #setUsername(String)
     * @see #setPassword(String)
     * @see #setHost(String)
     */
    public void initialize();

    /**
     * Called once (and only once) to set up registration to support incoming
     * calls. This has to be called AFTER initialize().
     * 
     * @param doEncryption
     * @see #initialize()
     */
    public void initializeRegistration(boolean doEncryption);

    /**
     * Called once when the phone (applet) is destroyed.
     */
    public void destroy();

    /**
     * Mute the audio. Not necessarily implemented by all implementations.
     * 
     * @param mute
     */
    public void muteMic(boolean mute);

    /**
     * Sets the audio gain. Not necessarily implemented by all implementations.
     * 
     * @param gain
     */
    public void setGain(double gain);
}
