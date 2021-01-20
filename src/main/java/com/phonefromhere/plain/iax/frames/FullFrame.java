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
package com.phonefromhere.plain.iax.frames;

import com.phonefromhere.plain.iax.frames.control.ControlFrame;
import com.phonefromhere.plain.iax.frames.iax.IaxFrame;
import com.phonefromhere.plain.util.Arithmetic;
import com.phonefromhere.plain.util.IaxLog;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt

 8.1.1.  Full Frames

 Full Frames can send signaling or media data.  Generally, Full Frames
 are used to control initiation, setup, and termination of an IAX
 call, but they can also be used to carry stream data (though this is
 generally not optimal).

 Full Frames are sent reliably, so all Full Frames require an
 immediate acknowledgement upon receipt.  This acknowledgement can be
 explicit via an 'ACK' message (see Section 8.4) or implicit based
 upon receipt of an appropriate response to the Full Frame issued.

 The standard Full Frame header length is 12 octets.

 Field descriptions:

 'F' bit

 This bit specifies whether or not the frame is a Full Frame.  If
 the 'F' bit is set to 1, the frame is a Full Frame.  If it is set
 to 0, it is not a Full Frame.

 Source call number

 This 15-bit value specifies the call number the transmitting
 client uses to identify this call.  The source call number for an
 active call MUST NOT be in use by another call on the same client.
 Call numbers MAY be reused once a call is no longer active, i.e.,
 either when there is positive acknowledgement that the call has
 been destroyed or when all possible timeouts for the call have
 expired.

 'R' bit

 This bit specifies whether or not the frame is being
 retransmitted.  If the 'R' bit is set to 0, the frame is being
 transmitted for the first time.  If it is set to 1, the frame is
 being retransmitted.  IAX does not specify a retransmit timeout;
 this is left to the implementor.

 Destination call number

 This 15-bit value specifies the call number the transmitting
 client uses to reference the call at the remote peer.  This number
 is the same as the remote peer's source call number.  The
 destination call number uniquely identifies a call on the remote
 peer.  The source call number uniquely identifies the call on the
 local peer.

 Time-stamp

 The time-stamp field contains a 32-bit time-stamp maintained by an
 IAX peer for a given call.  The time-stamp is an incrementally
 increasing representation of the number of milliseconds since the
 first transmission of the call.

 OSeqno

 The 8-bit OSeqno field is the outbound stream sequence number.
 Upon initialization of a call, its value is 0.  It increases
 incrementally as Full Frames are sent.  When the counter
 overflows, it silently resets to 0.

 ISeqno

 The 8-bit ISeqno field is the inbound stream sequence number.
 Upon initialization of a call, its value is 0.  It increases
 incrementally as Full Frames are received.  At any time, the
 ISeqno of a call represents the next expected inbound stream
 sequence number.  When the counter overflows, it silently resets
 to 0.

 Frametype

 The Frametype field identifies the type of message carried by the
 frame.  See Section 8.2 for more information.

 'C' bit

 This bit determines how the remaining 7 bits of the Subclass field
 are coded.  If the 'C' bit is set to 1, the Subclass value is
 interpreted as a power of 2.  If it is not set, the Subclass value
 is interpreted as a simple 7-bit unsigned integer.

 1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |F|     Source Call Number      |R|   Destination Call Number   |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                            time-stamp                         |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |    OSeqno     |    ISeqno     |   Frame Type  |C|  Subclass   |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                                                               |
 :                             Data                              :
 |                                                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

 Figure 5: Full Frame Binary Format
 *
 */

public class FullFrame extends Frame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: FullFrame.java,v 1.4 2011/02/23 14:20:06 uid1003 Exp $ Copyright Westhawk Ltd";

    static final int HEADER = 12 * 8;

    private boolean _isRetransmitted = false;
    /* 15 bit */
    private short _destinationCallNumber = 0;
    /* 8 bit, would fit in a byte, if java bytes weren't signed, arggh */
    /*
     * The outgoing seq number == outgoing message count
     */
    private short _oSeqNo = 0;

    /*
     * The incoming seq number == highest numbered incoming message that has
     * been received.
     */
    private short _iSeqNo = 0;

    private FrameType _frameType = null;
    private long _subClass = 0;

    protected FullFrame(short sourceCallNumber) {
        super(true, sourceCallNumber);
    }

    public boolean isRetransmitted() {
        return _isRetransmitted;
    }

    public void setRetransmitted(boolean isRetransmitted) {
        if (isRetransmitted != _isRetransmitted) {
            this.clearEncodedFrame();
        }
        _isRetransmitted = isRetransmitted;
    }

    @Override
    public void incrementRetries() {
        super.incrementRetries();
        if (this.getRetries() > 0) {
            this.setRetransmitted(true);
        }
    }

    public short getDestinationCallNumber() {
        return _destinationCallNumber;
    }

    public void setDestinationCallNumber(short destinationCallNumber) {
        if (destinationCallNumber != _destinationCallNumber) {
            this.clearEncodedFrame();
        }
        _destinationCallNumber = destinationCallNumber;
    }

    public short getOSeqNo() {
        return _oSeqNo;
    }

    public void setOSeqNo(short oSeqNo) {
        if (oSeqNo != _oSeqNo) {
            this.clearEncodedFrame();
        }
        _oSeqNo = oSeqNo;
    }

    public short getISeqNo() {
        return _iSeqNo;
    }

    public void setISeqNo(short iSeqNo) {
        if (iSeqNo != _iSeqNo) {
            this.clearEncodedFrame();
        }
        _iSeqNo = iSeqNo;
    }

    public FrameType getFrameType() {
        return _frameType;
    }

    public void setFrameType(FrameType frameType) {
        if (frameType != _frameType) {
            this.clearEncodedFrame();
        }
        _frameType = frameType;
    }

    public long getSubClass() {
        return _subClass;
    }

    public void setSubClass(long subClass) {
        if (subClass != _subClass) {
            this.clearEncodedFrame();
        }
        _subClass = subClass;
    }

    protected static long getSubClass(boolean isC, byte subClassB) {
        long subClassI = subClassB;
        if (isC) {
            if (subClassB == 0x7F) {
                subClassI = 255;
            } else {
                // 2^subClassB
                subClassI = (1 << subClassB);
            }
        }
        return subClassI;
    }

    /** Upon receipt of this frame, should we send an ACK ? */
    public boolean mustSendAck() {
        return true;
    }

    @Override
    public byte[] writeFrame() {
        int headerOctets = (HEADER / 8);
        int totalOctets = headerOctets;
        if (getData() != null) {
            totalOctets += getData().length;
        }
        byte[] frame = new byte[totalOctets];

        // The F bit & source call number
        int posBit = super.writeFnSourceCallNumber(frame);

        // R bit
        if (_isRetransmitted) {
            Arithmetic.setBit(frame, posBit);
        }
        posBit++;

        // 15 bit destination call number
        posBit = Arithmetic.copyBits(_destinationCallNumber, 15, frame, posBit);

        // The 32 bit timestamp
        posBit = super.writeTimeStamp(32, frame, posBit);

        // The 8 bit oSeqNo
        posBit = Arithmetic.copyBits(_oSeqNo, 8, frame, posBit);

        // The 8 bit iSeqNo
        posBit = Arithmetic.copyBits(_iSeqNo, 8, frame, posBit);

        // The 8 bit frame type
        posBit = Arithmetic.copyBits(this._frameType.getValue(), 8, frame,
                posBit);

        long threshold = (1 << 7);
        boolean isC = false;
        byte subClass = 0;
        if (_subClass > threshold) {
            isC = true;
            subClass = (byte) Arithmetic.log2(_subClass);
        } else {
            isC = false;
            subClass = (byte) _subClass;
        }

        // The C bit
        if (isC) {
            Arithmetic.setBit(frame, posBit);
        }
        posBit++;

        // The 7 bit subclass
        posBit = Arithmetic.copyBits(subClass, 7, frame, posBit);

        // sanity check
        if (posBit != HEADER) {
            IaxLog.getLog().error(this.getClass().getSimpleName() + ".writeFrame(): pos="
                    + posBit + ", header=" + HEADER);
        }

        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".writeFrame(): header="
        // + headerOctets);
        // for (int i = 0; i < headerOctets; i++) {
        // Arithmetic.printByte(frame, i);
        // }

        // write the data
        super.writeData(frame, posBit);
        // IaxLog.getLog().iax(this.getClass().getSimpleName() +
        // ".writeFrame(): totalOctets="
        // + totalOctets + "\n");

        return frame;
    }

    @Override
    public void readFrame(byte[] input) {
        // The source call number
        int len = 0;
        int posBit = super.readSourceCallNumber(input);

        // R bit
        boolean isRetransmitted = false;
        if (Arithmetic.getBit(input, posBit) == 1) {
            isRetransmitted = true;
        }
        this.setRetransmitted(isRetransmitted);
        posBit++;

        // 15 bit destination call number
        len = 15;
        short destinationCallNumber = Arithmetic.copyBitsToShort(input, posBit,
                len);
        this.setDestinationCallNumber(destinationCallNumber);
        posBit += len;

        // The 32 bit timestamp
        posBit = super.readTimeStamp(input, posBit, 32);

        // The 8 bit oSeqNo
        len = 8;
        short oSeqNo = Arithmetic.copyBitsToShort(input, posBit, len);
        this.setOSeqNo(oSeqNo);
        posBit += len;

        // The 8 bit iSeqNo
        len = 8;
        short iSeqNo = Arithmetic.copyBitsToShort(input, posBit, len);
        this.setISeqNo(iSeqNo);
        posBit += len;

        // The 8 bit frame type
        len = 8;
        // don't read the frame type, that should be known by now
        posBit += len;

        // The C bit
        // The 7 bit subclass
        len = 8;
        long subclassL = readSubClass(input);
        this.setSubClass(subclassL);
        posBit += len;

        // sanity check
        if (posBit != HEADER) {
            IaxLog.getLog().error(this.getClass().getSimpleName() + ".writeFrame(): pos="
                    + posBit + ", header=" + HEADER);
        }

        // read the data
        super.readData(input, posBit);
    }

    public static long readSubClass(byte[] input) {
        // The subclass is located at the 12th octet:
        int posBit = (11 * 8);

        // The C bit
        boolean isC = false;
        if (Arithmetic.getBit(input, posBit) == 1) {
            isC = true;
        }
        posBit++;

        // The 7 bit subclass
        int len = 7;
        short subclassS = Arithmetic.copyBitsToShort(input, posBit, len);
        long subclassL = FullFrame.getSubClass(isC, (byte) subclassS);

        posBit += len;
        return subclassL;
    }

    public static FullFrame createFrame(byte[] input) {
        // read the frametype and subclass and create the appropriate FullFrame
        FullFrame frame = null;

        // The FrameType is located at the 11th octet:
        int posBit = (10 * 8);
        short frameTypeS = Arithmetic.copyBitsToShort(input, posBit, 8);

        FrameType frameType = findType(frameTypeS);
        if (frameType != null) {
            // IaxLog.getLog().debug(FullFrame.class.getSimpleName()
            // + ".createFrame(): frameType " + frameType.toString());
            switch (frameType) {
                case DTMF:
                    frame = new DtmfFrame();
                    break;
                case VOICE:
                    frame = new VoiceFrame();
                    break;
                case CONTROL:
                    frame = new ControlFrame();
                    break;
                case IAX:
                    frame = IaxFrame.createFrame(input);
                    break;
                case TEXT:
                    frame = new TextFrame();
                    break;
                case COMFORT:
                    break;
                case HTML:
                    break;
                case IMAGE:
                    break;
                case NULL:
                    break;
                case VIDEO:
                    break;
            }

            if (frame == null) {
                IaxLog.getLog().error(FullFrame.class.getSimpleName()
                        + ".createFrame(): frameType " + frameType.getName()
                        + " is not supported.");
            } else {

                frame.readFrame(input);
            }

        } else {
            IaxLog.getLog().error(FullFrame.class.getSimpleName()
                    + ".createFrame(): cannot find frametype with value "
                    + frameTypeS);
        }

        return frame;
    }

    private static FrameType findType(int value) {
        FrameType type = null;
        for (FrameType type2 : FrameType.values()) {
            if (value == type2.getValue()) {
                type = type2;
                break;
            }
        }
        return type;
    }

    @Override
    public String toShortString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toShortString());
        buf.append(", ");
        if (_frameType != null) {
            buf.append(_frameType.getName());
        }
        if (this.getRetries() > 0){
            buf.append(" (retries=").append(this.getRetries()).append(")");
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append("\t isRetransmitted=").append(_isRetransmitted).append("\n");
        buf.append("\t destinationCallNumber=").append(_destinationCallNumber)
                .append("\n");
        buf.append("\t oSeqNo=").append(_oSeqNo).append("\n");
        buf.append("\t iSeqNo=").append(_iSeqNo).append("\n");
        buf.append("\t ");
        if (_frameType != null) {
            buf.append(_frameType.toString());
        } else {
            buf.append("frametype=null");
        }
        buf.append("\n");
        return buf.toString();
    }

}
