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

import com.phonefromhere.plain.iax.frames.iax.IaxFrame;
import com.phonefromhere.plain.iax.net.TransmitterReceiver;

public class RegistrationCallLeg extends CallLeg {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: RegistrationCallLeg.java,v 1.1 2011/02/10 16:16:25 uid1003 Exp $ Copyright Westhawk Ltd";

    private RegistrationCallLegState _state = RegistrationCallLegState.UNREGISTERED;

    public RegistrationCallLeg(TransmitterReceiver transmitter,
            short sourceIdentifier) {
        super(transmitter, sourceIdentifier);
    }

    /*
     * Include CALLTOKEN IE in REGREQ and REGREL: Like you do with NEW: first
     * empty CallToken IE, receive a CallToken IAX Frame, then send a second
     * REQREQ and REGREL with that CallToken IE!
     */
    @Override
    protected void receivedCallToken(IaxFrame iaxFrame) {
        // TODO Auto-generated method stub
        super.receivedCallToken(iaxFrame);
        String callToken = iaxFrame.getCallToken();
    }

}
