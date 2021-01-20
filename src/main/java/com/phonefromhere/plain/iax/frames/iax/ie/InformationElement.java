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
package com.phonefromhere.plain.iax.frames.iax.ie;

import java.io.UnsupportedEncodingException;

import com.phonefromhere.plain.util.Arithmetic;
import com.phonefromhere.plain.util.IaxLog;
import com.phono.srtplight.Log;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 * http://www.rfc-editor.org/rfc/rfc5457.txt
 * http://www.iana.org/assignments/iax-parameters/iax-parameters.xml
 * 

 8.6.  Information Elements

 IAX messages sent as Full Frames MAY carry information elements to
 specify user- or call-specific data.  Information elements are
 appended to a frame header in its data field.  Zero, one, or multiple
 information elements MAY be included with any IAX message.

 Information elements are coded as follows:

 The first octet of any information element consists of the "IE"
 field.  The IE field is an identification number that defines the
 particular information element.  Table 1 lists the defined
 information elements and each information element is defined below
 the table.

 The second octet of any information element is the "data length"
 field.  It specifies the length in octets of the information
 element's data field.

 The remaining octet(s) of an information element contain the
 actual data being transmitted.  The representation of the data is
 dependent on the particular information element as identified by
 its "IE" field.  Some information elements carry binary data, some
 carry UTF-8 [RFC3629] data, and some have no data field at all.
 Elements that carry UTF-8 MUST prepare strings as per [RFC3454]
 and [RFC3491], so that illegal characters, case folding, and other
 characters properties are handled and compared properly.  The data
 representation for each information element is described below.

 The following table specifies the Information Element Binary Format:

 1
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |      IE       |  Data Length  |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                               |
 :             DATA              :
 |                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class InformationElement {

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: InformationElement.java,v 1.4 2011/02/17 13:15:30 uid1003 Exp $ Copyright Westhawk Ltd";

    static final int HEADER = 2 * 8;
    static final String UTF8 = "UTF-8";

    private IEType _type = null;
    /* The length of _data in octets (or bytes) - (8 bit length) */
    private int _length = 0;
    private byte[] _data = null;

    public InformationElement() {
    }

    public InformationElement(IEType type) {
        this.setType(type);
    }

    public void setType(IEType type) {
        _type = type;
    }

    public IEType getType() {
        return _type;
    }

    public void setLength(int length) {
        _length = length;
    }

    public int getLength() {
        return _length;
    }

    public void setData(byte[] data) {
        _data = data;
    }

    public byte[] getData() {
        return _data;
    }

    public void set1Octet(int value) {
        setOctets(1, value);
    }

    public void set2Octets(int value) {
        setOctets(2, value);
    }

    public void set4Octets(long value) {
        setOctets(4, value);
    }



    protected void setOctets(int octets, long value) {
        this.setLength(octets);
        byte[] data = new byte[octets];
        int bits = (octets * 8);
        Arithmetic.copyBits(value, bits, data, 0);
        this.setData(data);
        if (Log.getLevel()>= Log.DEBUG){
            Log.debug("setting octets on ie "+this._type.getName());
            Arithmetic.printData(data);
        }
    }

    public int get1Octet() {
        int octets = 1;
        int bits = (octets * 8);
        short s = Arithmetic.copyBitsToShort(_data, 0, bits);
        compareLength(octets);
        return s;
    }

    public int get2Octets() {
        int octets = 2;
        int bits = (octets * 8);
        int i = Arithmetic.copyBitsToInt(_data, 0, bits);
        compareLength(octets);
        return i;
    }

    public long get4Octets() {
        int octets = 4;
        int bits = (octets * 8);
        long l = Arithmetic.copyBitsToLong(_data, 0, bits);
        compareLength(octets);
        return l;
    }

    public byte getVersion(){
        return _data[0];
    }
    
    public long get8VersionedOctets() {
        int octets = 8;
        int bits = (octets * 8);
        long l = Arithmetic.copyBitsToLong(_data, 8, bits);
        compareLength(octets+1);
        return l;
    }

    private void compareLength(int octets) {
        if (octets != _length) {
            IaxLog.getLog().error(this.getClass().getSimpleName() + ".compareLength(): "
                    + _type.getName() + ": _length=" + _length + " != "
                    + octets);
        }
    }

    public static byte[] stringToUtfBytes(String str) {
        byte[] data = null;
        if (str != null) {
            try {
                data = str.getBytes(UTF8);
            } catch (UnsupportedEncodingException e) {
                data = str.getBytes();
            }
        }
        return data;
    }

    public static String bytesToUtfString(byte[] data) {
        String str = null;
        if (data != null) {
            try {
                str = new String(data, UTF8);
            } catch (UnsupportedEncodingException e) {
                str = new String(data);
            }
        }
        return str;
    }

    public void setUtfString(String str) {
        byte[] data = null;
        if (str != null) {
            data = stringToUtfBytes(str);
            this.setLength(data.length);
            this.setData(data);
        }
    }

    public String getUtfString() {
        String str = "";
        if (_data != null) {
            str = bytesToUtfString(_data);
        }
        return str;
    }

    private static IEType findType(int value) {
        IEType type = null;
        for (IEType type2 : IEType.values()) {
            if (value == type2.getValue()) {
                type = type2;
                break;
            }
        }
        return type;
    }

    public int getTotalLength() {
        int totalOctets = (HEADER / 8) + _length;
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".getTotalLength(): "
        // + _type.toString() + ", totalOctets=" + totalOctets);
        return totalOctets;
    }

    public int writeIE(byte[] output, int posBit) {
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".writeIE(): " +
        // this.toString());
        // IaxLog.getLog().iax(this.getClass().getSimpleName() + ".writeIE(): length=" +
        // _length);

        // int startPosBit = posBit;
        // The 8 bit IE type
        posBit = Arithmetic.copyBits(_type.getValue(), 8, output, posBit);

        // The 8 bit length
        posBit = Arithmetic.copyBits(_length, 8, output, posBit);

        // The IE data
        if (_length > 0 && _data != null) {
            int octetNo = (posBit / 8);
            System.arraycopy(_data, 0, output, octetNo, _length);
        }
        posBit += (_length * 8);

        // int startOctetNo = (startPosBit / 8);
        // int endOctetNo = (posBit / 8);
        // for (int i = startOctetNo; i < endOctetNo; i++) {
        // Arithmetic.printByte(output, i);
        // }
        return posBit;
    }

    public int readIE(byte[] input, int posBit) {
        // The 8 bit IE type
        int len = 8;
        short ieTypeS = Arithmetic.copyBitsToShort(input, posBit, len);
        posBit += len;

        IEType ieType = findType(ieTypeS);
        this.setType(ieType);
        if (ieType == null) {
            IaxLog.getLog().error(InformationElement.class.getSimpleName()
                    + ".readIE(): invalid type " + ieTypeS);
        }

        // The 8 bit length
        len = 8;
        short dataLength = Arithmetic.copyBitsToShort(input, posBit, len);
        this.setLength(dataLength);
        posBit += len;

        // read all IE data
        len = dataLength;
        byte[] data = null;
        if (len > 0) {
            int octetNo = (posBit / 8);
            data = new byte[len];
            System.arraycopy(input, octetNo, data, 0, len);
        }
        this.setData(data);
        if ((ieType == IEType.FORMAT2) || (ieType == IEType.FORMAT)){
            Log.debug("Got "+ieType.getName()+" datalenght = "+len);
        }
        posBit += (len * 8);

        return posBit;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(_type.toString());
        buf.append(": ");

        switch (_type) {
            case CALLED_CONTEXT:
            case CALLED_NUMBER:
            case CALLING_NAME:
            case CALLING_NUMBER:
            case CALLTOKEN:
            case CAUSE:
            case CHALLENGE:
            case CODEC_PREFS:
            case MD5_RESULT:
            case RSA_RESULT:
            case USERNAME:
                buf.append("'").append(this.getUtfString()).append("'");
                break;
            case CALLINGPRES:
            case CALLINGTON:
            case CAUSECODE:
            case IAX_UNKNOWN:
                buf.append(this.get1Octet());
                break;
            case AUTHMETHODS:
            case CALLINGTNS:
            case VERSION:
                buf.append(get2Octets());
                break;
            case CAPABILITY:
            case FORMAT:
                buf.append("0x").append(Long.toHexString(get4Octets()));
                break;
            case FORMAT2:
            case CAPABILITY2:                
                buf.append("v").append(getVersion());
                buf.append("0x").append(Long.toHexString(get8VersionedOctets()));
                break;

            case ADSICPE:
            case APPARENT_ADDR:
            case AUTOANSWER:
            case CALLING_ANI:
            case CALLNO:
            case DATETIME:
            case DNID:
            case DPSTATUS:
            case ENCKEY:
            case ENCRYPTION:
            case LANGUAGE:
            case MSGCOUNT:
            case MUSICONHOLD:
            case OSPTOKEN:
            case PASSWORD:
            case RDNIS:
            case REFRESH:
            case RESERVED1:
            case RESERVED2:
            case RESERVED3:
            case RESERVED4:
            case RESERVED5:
            case RESERVED6:
            case RESERVED7:
            case RESERVED8:
            case RR_DELAY:
            case RR_DROPPED:
            case RR_JITTER:
            case RR_LOSS:
            case RR_OOO:
            case RR_PKTS:
            case SAMPLINGRATE:
            case TRANSFERID:
            case VARIABLE:
                buf.append("?");
                break;
        }
        return buf.toString();
    }

    public void set8VersionedOctets(int i, long value) {
        int octets = 8;
        this.setLength(octets+1);
        byte[] data = new byte[octets+1];
        data [0] = (byte) i;
        int bits = (octets * 8);
        Arithmetic.copyBits(value, bits, data,8);
        this.setData(data);
        if (Log.getLevel()>= Log.DEBUG){
            Log.debug("setting octets on ie "+this._type.getName());
            Arithmetic.printData(data);
        }
    }

}
