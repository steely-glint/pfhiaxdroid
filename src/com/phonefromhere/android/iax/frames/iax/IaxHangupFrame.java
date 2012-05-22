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
package com.phonefromhere.android.iax.frames.iax;

import com.phonefromhere.android.iax.frames.iax.ie.CauseCodeType;

/*
 *  http://www.rfc-editor.org/rfc/rfc5456.txt
 *  
 6.2.5.  HANGUP Request Message

 A HANGUP message is sent by either peer and indicates a call tear-
 down.  It MAY include the 'causecode' and 'cause' IEs to indicate the
 reason for terminating the call.  Upon receipt of a HANGUP message,
 an IAX peer MUST immediately respond with an ACK, and then destroy
 the call leg at its end.  After a HANGUP message has been received
 for a call leg, any messages received that reference that call leg
 (i.e., have the same source/destination call identifiers) MUST be
 answered with an INVAL message.  This indicates that the received
 message is invalid because the call no longer exists.

 After sending a HANGUP message, the sender MUST destroy the call and
 respond to subsequent messages regarding this call with an INVAL
 message.
 */

public class IaxHangupFrame extends IaxFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: IaxHangupFrame.java,v 1.1 2011/02/10 16:16:24 uid1003 Exp $ Copyright Westhawk Ltd";
    private HungupListener _hungupListener;

    protected IaxHangupFrame() {
        this((short) 0);
    }

    public IaxHangupFrame(short sourceCallNumber) {
        super(sourceCallNumber, IaxSubclass.HANGUP);
    }

    public void setCauseCode(CauseCodeType causeCode, String cause) {
        setCauseCode(causeCode);
        if (cause == null && causeCode != null) {
            cause = causeCode.getMessage();
        }
        setCause(cause);
    }

    public void setHungupListener(HungupListener listener) {
        _hungupListener = listener;
    }
    
    @Override
    public void setAcknowlegded(boolean isAcknowlegded) {
        super.setAcknowlegded(isAcknowlegded);
        
        if (_hungupListener != null) {
            CauseCodeType causeCode = this.getCauseCode();
            String cause = this.getCause();
            if (cause == null && causeCode != null) {
                cause = causeCode.getMessage();
            }
            _hungupListener.hungup(causeCode, cause);
        }
    }

}
