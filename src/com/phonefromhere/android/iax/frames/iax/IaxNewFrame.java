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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.phonefromhere.android.codec.MediaFormat;
import com.phonefromhere.android.iax.frames.VoiceFrame;
import com.phonefromhere.android.iax.frames.iax.ie.CallingPresType;
import com.phonefromhere.android.iax.frames.iax.ie.CallingTonType;
import com.phonefromhere.android.iax.frames.iax.ie.IEType;
import com.phonefromhere.android.iax.frames.iax.ie.InformationElement;
import com.phonefromhere.android.util.IaxLog;

/*
 *  http://www.rfc-editor.org/rfc/rfc5456.txt
 *  
 6.2.2.  NEW Request Message

 A NEW message is sent to initiate a call.  It is the first call-
 specific message sent to initiate an actual media exchange between
 two peers.  'NEW' messages are unique compared to other Call
 Supervision messages in that they do not require a destination call
 identifier in their header.  This absence is because the remote
 peer's source call identifier is not created until after receipt of
 this frame.  Before sending a NEW message, the local IAX peer MUST
 assign a source call identifier that is not currently being used for
 another call.  A time-stamp MUST also be assigned for the call,
 beginning at zero and incrementing by one each millisecond.  Sequence
 numbers for a NEW message, described in the transport section,
 (Section 7) are both set to 0.

 A NEW message MUST include the 'version' IE, and it MUST be the first
 IE; the order of other IEs is unspecified.  A NEW SHOULD generally
 include IEs to indicate routing on the remote peer, e.g., via the
 'called number' IE or to indicate a peer partition or ruleset, the
 'called context' IE.  Caller identification and CODEC negotiation IEs
 MAY also be included.

 Upon receipt of a NEW message, the receiving peer examines the
 destination and MUST perform one of the following actions:

 Send a REJECT response,

 Challenge the caller with an AUTHREQ response,

 Accept the call using an ACCEPT message, or

 Abort the connection using a HANGUP message, although the REJECT
 message is preferred at this point in call.

 If the call is accepted, the peer MUST progress the call and further
 respond with one of PROCEEDING, RINGING, BUSY, or ANSWER depending on
 the status of the called party on the peer.  See Section 6.3 for
 further details.

 The following table specifies IEs for the NEW message:
 +--------------+----------------+-------------+---------------------+
 | IE           | Section        | Status      | Comments            |
 +--------------+----------------+-------------+---------------------+
 | Version      | Section 8.6.10 | Required    |                     |
 |              |                |             |                     |
 | Called       | Section 8.6.1  | Required    |                     |
 | Number       |                |             |                     |
 |              |                |             |                     |
 | Auto Answer  | Section 8.6.24 | Optional    |                     |
 |              |                |             |                     |
 | Codecs Prefs | Section 8.6.35 | Required    |                     |
 |              |                |             |                     |
 | Calling      | Section 8.6.29 | Required    |                     |
 | Presentation |                |             |                     |
 |              |                |             |                     |
 | Calling      | Section 8.6.2  | Optional    |                     |
 | Number       |                |             |                     |
 |              |                |             |                     |
 | Calling TON  | Section 8.6.30 | Required    |                     |
 |              |                |             |                     |
 | Calling TNS  | Section 8.6.31 | Required    |                     |
 |              |                |             |                     |
 | Calling Name | Section 8.6.4  | Optional    |                     |
 |              |                |             |                     |
 | ANI          | Section 8.6.3  | Optional    |                     |
 |              |                |             |                     |
 | Language     | Section 8.6.9  | Optional    |                     |
 |              |                |             |                     |
 | DNID         | Section 8.6.12 | Optional    |                     |
 |              |                |             |                     |
 | Called       | Section 8.6.5  | Conditional | 'Default' assumed   |
 | Context      |                |             | if IE excluded      |
 |              |                |             |                     |
 | Username     | Section 8.6.6  | Optional    |                     |
 |              |                |             |                     |
 | RSA Result   | Section 8.6.16 | Conditional | If challenged with  |
 |              |                |             | RSA                 |
 |              |                |             |                     |
 | MD5 Result   | Section 8.6.15 | Conditional | If challenged with  |
 |              |                |             | MD5                 |
 |              |                |             |                     |
 | Format       | Section 8.6.8  | Required    |                     |
 |              |                |             |                     |
 | Capability   | Section 8.6.7  | Conditional |                     |
 |              |                |             |                     |
 | ADSICPE      | Section 8.6.11 | Optional    |                     |
 |              |                |             |                     |
 | Date Time    | Section 8.6.28 | Optional    | Suggested           |
 |              |                |             |                     |
 | Encryption   | Section 8.6.34 | Optional    |                     |
 |              |                |             |                     |
 | OSP Token    | Section 8.6.42 | Optional    |                     |
 +--------------+----------------+-------------+---------------------+


 */
public class IaxNewFrame extends IaxFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: IaxNewFrame.java,v 1.3 2011/02/17 13:15:30 uid1003 Exp $ Copyright Westhawk Ltd";

    public final static int VERSION = 2;

    protected IaxNewFrame() {
        this((short) 0);
    }

    public IaxNewFrame(short sourceCallNumber) {
        super(sourceCallNumber, IaxSubclass.NEW);
    }

    public IaxNewFrame(short sourceCallNumber, String calledNumber,
            Collection<MediaFormat> formats) {
        this(sourceCallNumber);
        this.setVersion(VERSION);
        this.setCallingPresentation(CallingPresType.ALLOW_NOT);
        this.setCallingTon(CallingTonType.UNKNOWN);
        this.setCallingTNS();
        this.setCallToken("");
        this.setCalledNumber(calledNumber);
        this.setSupportedMediaFormats(formats);
    }

    /*
     * 8.6.10. VERSION
     * 
     * The purpose of the VERSION information element is to indicate the
     * protocol version the peer is using. Peers at each end of a call MUST use
     * the same protocol version. Currently, the only supported version is 2.
     * The data field of the VERSION information element is 2 octets long.
     * 
     * The VERSION information element MUST be sent with an IAX NEW message.
     * 
     * When sent, the VERSION information element MUST be the first IE in the
     * message.
     */
    protected void setVersion(int version) {
        InformationElement ie = new InformationElement(IEType.VERSION);
        ie.set2Octets(version);
        this.addInformationElement(ie);
    }

    /*
     * 8.6.1. CALLED NUMBER
     * 
     * The purpose of the CALLED NUMBER information element is to indicate the
     * number or extension being called. It carries UTF-8-encoded data. The
     * CALLED NUMBER information element MUST use UTF-8 encoding and not numeric
     * data because destinations are not limited to E.164 numbers ([E164]),
     * national numbers, or even digits. It is possible for a number or
     * extension to include non-numeric characters. The CALLED NUMBER IE MAY
     * contain a SIP URI, [RFC3261] or a URI in any other format. The ability to
     * serve a CALLED NUMBER is server dependent.
     * 
     * The CALLED NUMBER information element is generally sent with IAX NEW,
     * DPREQ, DPREP, DIAL, and TRANSFER messages.
     */
    public void setCalledNumber(String number) {
        if (number != null) {
            InformationElement ie = new InformationElement(IEType.CALLED_NUMBER);
            ie.setUtfString(number);
            this.addInformationElement(ie);
        }

    }

    public String getCalledNumber() {
        String number = null;
        InformationElement ie = this
                .getInformationElement(IEType.CALLED_NUMBER);
        if (ie != null) {
            number = ie.getUtfString();
        }
        return number;
    }

    /*
     * 8.6.2. CALLING NUMBER
     * 
     * The purpose of the CALLING NUMBER information element is to indicate the
     * number or extension of the calling entity to the remote peer. It carries
     * UTF-8-encoded data.
     * 
     * The CALLING NUMBER information element is usually sent with IAX NEW
     * messages.
     */
    public void setCallingNumber(String number) {
        if (number != null) {
            InformationElement ie = new InformationElement(
                    IEType.CALLING_NUMBER);
            ie.setUtfString(number);
            this.addInformationElement(ie);
        }
    }

    public String getCallingNumber() {
        String number = null;
        InformationElement ie = this
                .getInformationElement(IEType.CALLING_NUMBER);
        if (ie != null) {
            number = ie.getUtfString();
        }
        return number;
    }

    /*
     * 8.6.4. CALLING NAME
     * 
     * The purpose of the CALLING NAME information element is to indicate the
     * calling name of the transmitting peer. It carries UTF-8-encoded data.
     * 
     * The CALLING NAME information element is usually sent with IAX NEW
     * messages.
     */
    public void setCallingName(String name) {
        if (name != null) {
            InformationElement ie = new InformationElement(IEType.CALLING_NAME);
            ie.setUtfString(name);
            this.addInformationElement(ie);
        }
    }

    public String getCallingName() {
        String name = null;
        InformationElement ie = this.getInformationElement(IEType.CALLING_NAME);
        if (ie != null) {
            name = ie.getUtfString();
        }
        return name;
    }

    /*
     * 8.6.5. CALLED CONTEXT
     * 
     * The purpose of the CALLED CONTEXT information element is to indicate the
     * context (or partition) of the remote peer's dialplan that the CALLED
     * NUMBER is interpreted. It carries UTF-8-encoded data.
     * 
     * The CALLED CONTEXT information element MAY be sent with IAX NEW or
     * TRANSFER messages, though it is not required.
     */
    public void setCalledContext(String context) {
        if (context != null) {
            InformationElement ie = new InformationElement(
                    IEType.CALLED_CONTEXT);
            ie.setUtfString(context);
            this.addInformationElement(ie);
        }
    }

    public String getCalledContext() {
        String context = null;
        InformationElement ie = this
                .getInformationElement(IEType.CALLED_CONTEXT);
        if (ie != null) {
            context = ie.getUtfString();
        }
        return context;
    }

    /*
     * Sets CODEC PREF, FORMAT and CAPABILITY. The parameter 'formats' should be
     * in order of preference.
     */
    public void setSupportedMediaFormats(Collection<MediaFormat> formats) {
        String codecPrefs = "";
        long capability = 0;
        MediaFormat firstFormat = null;
        boolean first = true;

        if (formats != null) {
            Iterator<MediaFormat> iter = formats.iterator();
            while (iter.hasNext()) {
                MediaFormat f = iter.next();
                if (first) {
                    first = false;
                    firstFormat = f;
                }
                char c = formatToCodecPref(f);
                if (c > 0) {
                    codecPrefs = codecPrefs + c;
                }
                capability = (capability | f.getValue());
            }
        }

        this.setCodecsPrefs(codecPrefs);
        this.setFormat(firstFormat);
        this.setCapability(capability);
    }

    /*
     * Returns the supported MediaFormat, in order of preference (if
     * applicable).
     */
    public List<MediaFormat> getSupportedMediaFormats() {
        ArrayList<MediaFormat> list = new ArrayList<MediaFormat>();
        String codecPrefs = this.getCodecsPrefs();
        if (codecPrefs != null) {
            for (int i = 0; i < codecPrefs.length(); i++) {
                char c = codecPrefs.charAt(i);
                MediaFormat format = this.codecPrefToFormat(c);
                if (format != null) {
                    list.add(format);
                }
            }
        } else {
            MediaFormat firstFormat = this.getFormat();
            if (firstFormat != null) {
                list.add(firstFormat);
            }

            long capability = this.getCapability();
            for (MediaFormat f : MediaFormat.values()) {
                if (f != firstFormat) {
                    long value = f.getValue();
                    if ((value & capability) > 0) {
                        list.add(f);
                    }
                }
            }
        }
        return list;
    }

    /*
     * 8.6.35. CODEC PREFS
     * 
     * The purpose of the CODEC PREFS information element is to indicate the
     * CODEC preferences of the calling peer. The data field consists of a list
     * of CODECs in the peer's order of preference as UTF-8-encoded data.
     * 
     * The CODEC PREFS information element MAY be sent with IAX NEW messages.
     * 
     * If the CODEC PREFS information element is absent, CODEC negotiation takes
     * place via the CAPABILITY and FORMAT information elements.
     * 
     * For some reason Asterisk/Digium implements the list starting with 'B',
     * not 'A'! See, https://issues.asterisk.org/view.php?id=18397
     * 
     * Codec (1 << 0) == 'B' == G723_1
     * 
     * Codec (1 << 1) == 'C' == GSM
     */

    protected char formatToCodecPref(MediaFormat format) {
        long value = format.getValue();
        char startCodecPref = 'B';

        // Keep shifting 'value' to the right, until it is 0x01
        // whilst increasing 'c'
        while (value > 1) {
            value = (value >>> 1);
            startCodecPref++;
        }

        // IaxLog.getLog().debug(this.getClass().getSimpleName() + ".formatToCodecPref(): "
        // + format.toString() + " == " + startCodecPref);
        return startCodecPref;
    }

    protected MediaFormat codecPrefToFormat(char codecPref) {
        // find the (long) value that corresponds to the codecPref
        char startCodecPref = 'B';
        long value = 0x01;
        int diff = (codecPref - startCodecPref);
        if (diff > 0) {
            value = (value << diff);
        }

        MediaFormat format = VoiceFrame.findMediaFormat(value);
        IaxLog.getLog().debug(this.getClass().getSimpleName() + ".codecPrefToFormat(): "
                + format.toString() + " == " + codecPref);
        return format;
    }

    public void setCodecsPrefs(String codecPrefs) {
        if (codecPrefs != null) {
            InformationElement ie = new InformationElement(IEType.CODEC_PREFS);
            ie.setUtfString(codecPrefs);
            this.addInformationElement(ie);
        }
    }

    public String getCodecsPrefs() {
        String codecPrefs = null;
        InformationElement ie = this.getInformationElement(IEType.CODEC_PREFS);
        if (ie != null) {
            codecPrefs = ie.getUtfString();
        }
        return codecPrefs;
    }

    /*
     * 8.6.7. CAPABILITY
     * 
     * The purpose of the CAPABILITY information element is to indicate the
     * media CODEC capabilities of an IAX peer. Its data is represented in a
     * 4-octet bitmask according to Section 8.7. Multiple CODECs MAY be
     * specified by logically OR'ing them into the CAPABILITY information
     * element.
     * 
     * The CAPABILITY information element is sent with IAX NEW messages if
     * appropriate for the CODEC negotiation method the peer is using.
     */
    public void setCapability(long capability) {
        InformationElement ie = new InformationElement(IEType.CAPABILITY);
        ie.set4Octets(capability);
        this.addInformationElement(ie);
    }

    public long getCapability() {
        long capability = 0;
        InformationElement ie = this.getInformationElement(IEType.CAPABILITY);
        if (ie != null) {
            capability = ie.get4Octets();
        }
        return capability;
    }

    /*
     * 8.6.29. CALLINGPRES
     * 
     * The purpose of the CALLINGPRES information element is to indicate the
     * calling presentation of a caller. The data field is 1 octet long and
     * contains a value from the table below.
     * 
     * The CALLINGPRES information element MUST be sent with IAX NEW messages.
     */
    protected void setCallingPresentation(CallingPresType type) {
        if (type != null) {
            InformationElement ie = new InformationElement(IEType.CALLINGPRES);
            ie.set1Octet(type.getValue());
            this.addInformationElement(ie);
        }

    }

    /*
     * 8.6.30. CALLINGTON
     * 
     * The purpose of the CALLINGTON information element is to indicate the
     * calling type of number of a caller, according to ITU-T Recommendation
     * Q.931 specifications. The data field is 1 octet long and contains data
     * from the table below.
     * 
     * The CALLINGTON information element MUST be sent with IAX NEW messages.
     */
    protected void setCallingTon(CallingTonType type) {
        if (type != null) {
            InformationElement ie = new InformationElement(IEType.CALLINGTON);
            ie.set1Octet(type.getValue());
            this.addInformationElement(ie);
        }
    }

    /*
     * 8.6.31. CALLINGTNS
     * 
     * The CALLINGTNS information element indicates the calling transit network
     * selected for a call. Values are chosen according to ITU-T Recommendation
     * Q.931 specifications. The data field is two octets long.
     * 
     * The first octet stores the network identification plan in the least
     * significant four bits according to the first table below, and the type of
     * network in the next three least significant bits according to the second
     * table below.
     * 
     * The second octet stores the actual network identification in
     * UTF-8-encoded data.
     * 
     * The CALLINGTNS information element MUST be sent with IAX NEW messages.
     * 
     * http://www.itu.int/itu-t/recommendations/rec.aspx?rec=Q.763
     */
    /*
     * public void setCallingTNS(NetworkType networkType, NetworkIdType id,
     * String networkID) {
     */
    protected void setCallingTNS() {
        // I haven't got a clue what to do!
        // I don't understand 'actual network identification'
        InformationElement ie = new InformationElement(IEType.CALLINGTNS);
        ie.set2Octets(0);
        this.addInformationElement(ie);
    }

}
