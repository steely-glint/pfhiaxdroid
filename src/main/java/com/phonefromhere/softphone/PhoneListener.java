/*
 * Copyright 2011 Westhawk ltd.
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
 * The phonelistener notifies of any changes in the call.
 * 
 * @see GenericSoftphone#setPhoneListener(PhoneListener)
 * 
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 1.1 $ $Date: 2011/02/03 14:33:15 $
 */
public interface PhoneListener {
    static final String version_id = "@(#)$Id: PhoneListener.java,v 1.1 2011/02/03 14:33:15 uid1003 Exp $ Copyright Westhawk Ltd";

    /** The phone is on the hook */
    int HOOK = 1;

    /** The call is connected */
    int CONNECTED = 2;

    /** The phone is ringing */
    int RINGING = 3;

    /**
     * The status of the call has changed. The status is one of the following:
     * 
     * <ol>
     * <li>
     * <a href="#HOOK">HOOK</a></li>
     * <li>
     * <a href="#CONNECTED">CONNECTED</a></li>
     * <li>
     * <a href="#RINGING">RINGING</a></li>
     * </ol>
     * 
     * @param whatChanged
     *            The current status.
     * @param errorMessage
     *            The error message as string (if any).
     */
    public void statusChangedEvent(int whatChanged, String errorMessage);

    /**
     * A text message (frame) is received.
     * 
     * @param txt
     *            The received text.
     */
    public void rcvdText(String txt);

    /**
     * A DTMF digit is received.
     * 
     * @param digit
     *            The (single) received DTMF digit.
     */
    public void rcvdDTMF(String digit);

    /**
     * An incoming call is received.
     * 
     * @param scno
     *            The source call number
     * @param calling
     *            The calling name or number
     */
    public void rcvdCall(short scno, String calling);

    /**
     * Sets whether or not the phone is registered at the PBX.
     * 
     * @param isRegistered
     */
    public void setRegistered(boolean isRegistered);
}
