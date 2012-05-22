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

import com.phonefromhere.android.util.Arithmetic;

/**
 * http://www.rfc-editor.org/rfc/rfc5456.txt This is the parent class of Mini
 * and Full frame
 * 
 * @author birgit
 * 
 */
public abstract class Frame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: Frame.java,v 1.6 2011/03/06 15:10:50 uid1003 Exp $ Copyright Westhawk Ltd";

    private boolean _isFullFrame = true;
    /* 15 bit value */
    private short _sourceCallNumber = 0;
    /* Full frame: 32 bit; Mini frame: 16 bit */
    private long _timestamp = 0;
    private byte[] _data = null;

    private byte[] _encodedFrame;

    private int _retries = 0;
    private long _lastSentMilliSec = 0;
    private boolean _isAcknowlegded = false;
    static private MiniFrame __miniFrameIn = new MiniFrame() ;

    public Frame(boolean isFullFrame, short sourceCallNumber) {
        _isFullFrame = isFullFrame;
        _sourceCallNumber = sourceCallNumber;
    }

    public boolean isFullFrame() {
        return _isFullFrame;
    }

    public void setSourceCallNumber(short sourceCallNumber) {
        if (sourceCallNumber != _sourceCallNumber) {
            this.clearEncodedFrame();
        }
        _sourceCallNumber = sourceCallNumber;
    }

    public short getSourceCallNumber() {
        return _sourceCallNumber;
    }

    public void setTimeStamp(long timestamp) {
        if (timestamp != _timestamp) {
            this.clearEncodedFrame();
        }
        _timestamp = timestamp;
    }

    public long getTimeStamp() {
        return _timestamp;
    }

    public void setData(byte[] data) {
        this.clearEncodedFrame();
        _data = data;
    }

    public byte[] getData() {
        return _data;
    }

    /**
     * Do we expect an answer to this frame? For example an ACK or a protocol
     * response.
     */
    public boolean isExpectingAnswer() {
        return true;
    }

    /** How many times has this frame been retried? */
    public int getRetries() {
        return _retries;
    }

    public void incrementRetries() {
        _retries++;
    }

    public void setSendAt(long lastSentMilliSec) {
        _lastSentMilliSec = lastSentMilliSec;
    }

    public long getSendAt() {
        return _lastSentMilliSec;
    }

    public void setAcknowlegded(boolean isAcknowlegded) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".setAcknowlegded(): "
        // + isAcknowlegded);
        _isAcknowlegded = isAcknowlegded;
    }

    public boolean isAcknowlegded() {
        return _isAcknowlegded;
    }

    /**
     * Writes the 'F' bit, the source call number. Return the number of bits
     * written.
     * 
     * @param output
     */
    protected int writeFnSourceCallNumber(byte[] output) {
        int posBit = 0;

        // The F bit
        if (_isFullFrame) {
            Arithmetic.setBit(output, posBit);
        }
        posBit++;

        // The 15 bit source call number
        posBit = Arithmetic.copyBits(_sourceCallNumber, 15, output, posBit);
        return posBit;
    }

    protected int readSourceCallNumber(byte[] input) {
        int posBit = 1;
        int len = 15;
        // The 15 bit source call number
        short sourceCallNumber = 0;
        sourceCallNumber = Arithmetic.copyBitsToShort(input, posBit, len);
        this.setSourceCallNumber(sourceCallNumber);
        posBit += len;
        return posBit;
    }

    protected int writeTimeStamp(int in_noLSB, byte[] output, int posBit) {
        // The 16 or 32 bit timestamp
        posBit = Arithmetic.copyBits(_timestamp, in_noLSB, output, posBit);
        return posBit;
    }

    protected int readTimeStamp(byte[] input, int posBit, int lenBit) {
        // The 16 or 32 bit timestamp
        long timestamp = 0;
        timestamp = Arithmetic.copyBitsToLong(input, posBit, lenBit);
        this.setTimeStamp(timestamp);
        posBit += lenBit;
        return posBit;
    }

    protected void writeData(byte[] output, int posBit) {
        if (_data != null) {
            int octet = (posBit / 8);
            System.arraycopy(_data, 0, output, octet, _data.length);
        }
    }

    protected int readData(byte[] input, int posBit) {
        // read all data from 'pos' to end of input into _data
        int octet = (posBit / 8);
        int len = input.length - octet;
        byte[] data = null;
        if (len > 0) {
            data = new byte[len];
            System.arraycopy(input, octet, data, 0, len);
        }
        this.setData(data);
        posBit += (len * 8);
        return posBit;
    }

    public byte[] getEncodedFrame() {
        if (_encodedFrame == null) {
            _encodedFrame = writeFrame();
        }
        return _encodedFrame;
    }

    public void clearEncodedFrame() {
        _encodedFrame = null;
    }

    abstract public byte[] writeFrame();

    abstract public void readFrame(byte[] input);

    public static Frame createFrame(byte[] input) {
        Frame frame = null;
        if (Arithmetic.getBit(input, 0) == 1) {
            frame = FullFrame.createFrame(input);
        } else {
            frame = __miniFrameIn;
            frame.readFrame(input);
        }
        return frame;
    }

    public String toShortString() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName()).append("\n");
        buf.append("\t retries=").append(_retries).append("\n");
        buf.append("\t sourceCallNumber=").append(_sourceCallNumber)
                .append("\n");
        buf.append("\t timestamp=").append(_timestamp).append("\n");
        return buf.toString();
    }
}
