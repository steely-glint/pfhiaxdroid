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
package com.phonefromhere.android.iax.frames;

import com.phonefromhere.android.util.IaxLog;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt

 8.1.2.  Mini Frames

 Mini Frames are so named because their header is a minimal 4 octets.
 Mini Frames carry no control or signaling data; their sole purpose is
 to carry a media stream on an already-established IAX call.  They are
 sent unreliably.  This decision was made because VoIP calls typically
 can miss several frames without significant degradation in call
 quality while the incurred overhead in ensuring reliability increases
 bandwidth requirements and decreases throughput.  Further, because
 voice calls are typically sent in real time, lost frames are too old
 to be reintegrated into the audio stream by the time they can be
 retransmitted.

 Field descriptions:

 'F' bit

 Mini Frames MUST have the 'F' bit set to 0 to specify that they
 are not Full Frames.

 Source call number

 The source call number is the number that is used by the
 transmitting peer to identify the current call.

 time-stamp

 Mini frames carry a 16-bit time-stamp, which is the lower 16 bits
 of the transmitting peer's full 32-bit time-stamp for the call.
 The time-stamp allows synchronization of incoming frames so that
 they MAY be processed in chronological order instead of the
 (possibly different) order in which they are received.  The 16-bit
 time-stamp wraps after 65.536 seconds, at which point a full frame
 SHOULD be sent to notify the remote peer that its time-stamp has
 been reset.  A call MUST continue to send mini frames starting
 with time-stamp 0 even if acknowledgment of the resynchronization
 is not received.

 The F bit, source call number, and 16-bit time-stamp comprise the
 entire 4-octet header for a full frame.  Following this header is the
 actual stream data, of arbitrary length, up to the maximum supported
 by the network.

 Mini frames are implicitly defined to be of type 'voice frame'
 (frametype 2; see Section 8.2).  The subclass is implicitly defined
 by the most recent full voice frame of a call (i.e. the subclass for
 a voice frame specifies the CODEC used with the stream).  The first
 voice frame of a call SHOULD be sent using the CODEC agreed upon in
 the initial CODEC negotiation.  On-the-fly CODEC negotiation is
 permitted by sending a full voice frame specifying the new CODEC to
 use in the subclass field.

 1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |F|     Source call number      |            time-stamp         |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                                                               |
 :                             Data                              :
 |                                                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

 Figure 6: Mini Frame Binary Format
 *
 */
public class MiniFrame extends Frame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: MiniFrame.java,v 1.6 2011/03/06 15:10:50 uid1003 Exp $ Copyright Westhawk Ltd";

    static final int HEADER = 4 * 8;

    private byte[] _frame; // cache to stop needless re-allocs.
    
    protected MiniFrame() {
        this((short) 0);
    }

    public MiniFrame(short sourceCallNumber) {
        super(false, sourceCallNumber);
    }

    @Override
    public boolean isExpectingAnswer() {
        return false;
    }

    @Override
    public void setTimeStamp(long timestamp) {
        // TODO: do something with the wrapping!?
        super.setTimeStamp(timestamp);
    }

    @Override
    public byte[] writeFrame() {
        int totalOctets = (HEADER / 8);
        if (getData() != null) {
            totalOctets += getData().length;
        }
        if ((_frame == null) || (_frame.length != totalOctets)){
            _frame = new byte[totalOctets];
        } else {
            for (int b=0;b<_frame.length;b++){
                _frame[b] = 0;
            }
        }

        // The F bit & source call number
        int posBit = super.writeFnSourceCallNumber(_frame);

        // The 16 bit timestamp
        posBit = super.writeTimeStamp(16, _frame, posBit);

        // sanity check
        if (posBit != HEADER) {
            IaxLog.getLog().error(this.getClass().getSimpleName() + ".writeFrame(): pos="
                    + posBit + ", header=" + HEADER);
        }

        // write the data
        super.writeData(_frame, posBit);

        return _frame;
    }
    @Override
    protected int readData(byte[] input, int posBit) {
        // read all data from 'pos' to end of input into _data
        int octet = (posBit / 8);
        int len = input.length - octet;
        byte[] data = getData();
        if ((data == null )|| (data.length != len)){
            data = new byte[len];
        }
        if (len > 0) {
            System.arraycopy(input, octet, data, 0, len);
        }
        this.setData(data);
        posBit += (len * 8);
        return posBit;
    }
    @Override
    public void readFrame(byte[] input) {
        // The source call number
        int posBit = super.readSourceCallNumber(input);

        // The 16 bit timestamp
        posBit = super.readTimeStamp(input, posBit, 16);

        // sanity check
        if (posBit != HEADER) {
            IaxLog.getLog().error(this.getClass().getSimpleName() + ".writeFrame(): pos="
                    + posBit + ", header=" + HEADER);
        }

        // read the data
        readData(input, posBit);
    }

}
