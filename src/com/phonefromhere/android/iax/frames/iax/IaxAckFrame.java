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

import com.phonefromhere.android.iax.frames.FullFrame;

/*
 *  http://www.rfc-editor.org/rfc/rfc5456.txt
 *  
 6.9.1.  ACK: Acknowledgement Message

 An ACK acknowledges the receipt of an IAX message.  An ACK is sent
 upon receipt of a Full Frame that does not have any other protocol-
 defined response.  An ACK MUST have both a source call number and
 destination call number.  It MUST also not change the sequence number
 counters, and MUST return the same time-stamp it received.  This
 time-stamp allows the originating peer to determine to which message
 the ACK is responding.  Receipt of an ACK requires no action.

 An ACK MAY also be sent as an initial acknowledgment of an IAX
 message that requires some other protocol-defined message
 acknowledgment, as long as the required message is also sent within
 some peer-defined amount of time.  This allows the acknowledging peer
 to delay transmission of the proper IAX message, which may add
 security against brute-force password attacks during authentication
 exchanges.

 When the following messages are received, an ACK MUST be sent in
 return: NEW, HANGUP, REJECT, ACCEPT, PONG, AUTHREP, REGREL, REGACK,
 REGREJ, TXREL.  ACKs SHOULD not be expected by any peer and their
 purpose is purely to force the transport layer to be up to date.

 The ACK message does not requires any IEs.
 */

public class IaxAckFrame extends IaxFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: IaxAckFrame.java,v 1.3 2011/03/06 15:10:51 uid1003 Exp $ Copyright Westhawk Ltd";

    protected IaxAckFrame() {
        this((short) 0);
    }

    public IaxAckFrame(short sourceCallNumber) {
        super(sourceCallNumber, IaxSubclass.ACK);
    }
// thp changed this to swap src/dest and in and out
    // but it is now not used - 
    /*
    public IaxAckFrame(FullFrame receivedFrame) {
        this(receivedFrame.getDestinationCallNumber());
        this.setDestinationCallNumber(receivedFrame.getSourceCallNumber());
        this.setOSeqNo(receivedFrame.getISeqNo());
        this.setISeqNo(receivedFrame.getOSeqNo());
        this.setTimeStamp(receivedFrame.getTimeStamp());
    }
    */



    // the peer will not answer/respond to an ACK
    @Override
    public boolean isExpectingAnswer() {
        return false;
    }
}
