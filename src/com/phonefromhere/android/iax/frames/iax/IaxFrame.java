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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.phonefromhere.android.codec.MediaFormat;
import com.phonefromhere.android.iax.frames.FrameType;
import com.phonefromhere.android.iax.frames.FullFrame;
import com.phonefromhere.android.iax.frames.VoiceFrame;
import com.phonefromhere.android.iax.frames.iax.ie.AuthMethodType;
import com.phonefromhere.android.iax.frames.iax.ie.CauseCodeType;
import com.phonefromhere.android.iax.frames.iax.ie.IEType;
import com.phonefromhere.android.iax.frames.iax.ie.InformationElement;
import com.phonefromhere.android.util.Arithmetic;
import com.phonefromhere.android.util.IaxLog;

public class IaxFrame extends FullFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: IaxFrame.java,v 1.6 2011/02/23 14:20:06 uid1003 Exp $ Copyright Westhawk Ltd";

    private IaxSubclass _iSubclass = null;
    // strictly speaking, there can be multiple InfoElements with the same type
    // in one frame. :-(
    private Map<IEType, InformationElement> _informationElements = null;

    protected IaxFrame() {
        this((short) 0, IaxSubclass.RESERVED1);
    }

    public IaxFrame(short sourceCallNumber, IaxSubclass iSubclass) {
        super(sourceCallNumber);
        super.setFrameType(FrameType.IAX);
        this.setSubClass(iSubclass);
        _informationElements = new LinkedHashMap<IEType, InformationElement>();
    }

    @Override
    public void setSubClass(long subClass) {
        IaxSubclass subclass2 = findSubclass((int) subClass);
        this.setSubClass(subclass2);

        if (subclass2 == null) {
            IaxLog.getLog().debug(this.getClass().getSimpleName()
                    + ".setSubClass(): cannot find subclass with value "
                    + subClass);
            super.setSubClass(subClass);
        }
    }

    public void setSubClass(IaxSubclass iSubclass) {
        _iSubclass = iSubclass;
        if (iSubclass != null) {
            super.setSubClass(_iSubclass.getValue());
        }
    }

    public IaxSubclass getSubClassI() {
        return _iSubclass;
    }

    public void addInformationElement(InformationElement ie) {
        _informationElements.put(ie.getType(), ie);
    }

    public InformationElement getInformationElement(IEType type) {
        return _informationElements.get(type);
    }

    /** Upon receipt of this frame, should we send an ACK ? */
    @Override
    public boolean mustSendAck() {
        boolean shouldSendAck = false;
        switch (_iSubclass) {
            case NEW:
            case HANGUP:
            case REJECT:
            case ACCEPT:
            case PONG:
            case AUTHREP:
            case REGREL:
            case REGACK:
            case REGREJ:
            case TXREL:
            case LAGRP:
                shouldSendAck = true;
                break;
            default:
        }
        return shouldSendAck;
    }

    public static IaxSubclass findSubclass(int value) {
        IaxSubclass type = null;
        for (IaxSubclass type2 : IaxSubclass.values()) {
            if (value == type2.getValue()) {
                type = type2;
                break;
            }
        }
        return type;
    }

    public static CauseCodeType findCauseCode(int value) {
        CauseCodeType type = null;
        for (CauseCodeType type2 : CauseCodeType.values()) {
            if (value == type2.getValue()) {
                type = type2;
                break;
            }
        }
        return type;
    }

    public static IaxFrame createFrame(byte[] input) {
        // read the frametype and subclass and create the appropriate FullFrame
        IaxFrame frame = null;

        int subclassL = (int) readSubClass(input);
        IaxSubclass subclass = findSubclass(subclassL);
        if (subclass != null) {
            switch (subclass) {
                case NEW:
                    frame = new IaxNewFrame();
                    break;
                case ACK:
                    frame = new IaxAckFrame();
                    break;
                case HANGUP:
                    frame = new IaxHangupFrame();
                    break;
                default:
                    frame = new IaxFrame();
            }

            // IaxLog.getLog().debug(IaxFrame.class.getSimpleName()
            // + ".createFrame(): subclass " + subclass.toString());
            // frame.setSubClass(subclass);
            // called by parent class
            // frame.readFrame(input);

        } else {
            IaxLog.getLog().error(IaxFrame.class.getSimpleName()
                    + ".createFrame(): invalid subclass " + subclassL);
        }

        return frame;
    }

    @Override
    public byte[] writeFrame() {
        byte[] data = this.createData();
        super.setData(data);
        byte[] frame = super.writeFrame();
        return frame;
    }

    protected byte[] createData() {
        byte[] data = null;
        InformationElement ie = null;

        int totalLength = 0;
        Set<Entry<IEType, InformationElement>> set = _informationElements
                .entrySet();

        Iterator<Entry<IEType, InformationElement>> setIter = set.iterator();
        while (setIter.hasNext()) {
            Entry<IEType, InformationElement> entry = setIter.next();
            ie = entry.getValue();
            totalLength += ie.getTotalLength();
        }
        // IaxLog.getLog().iax(this.getClass().getSimpleName() +
        // ".createData(): data length="
        // + totalLength);

        if (totalLength > 0) {
            data = new byte[totalLength];
            int pos = 0;
            setIter = set.iterator();
            while (setIter.hasNext()) {
                Entry<IEType, InformationElement> entry = setIter.next();
                ie = entry.getValue();
                pos = ie.writeIE(data, pos);
            }
        }
        return data;
    }

    @Override
    public void setData(byte[] data) {
        super.setData(data);

        if (data != null) {
            int dataLength = data.length;
            if (dataLength > 0) {
                int posBit = 0;
                int endBit = dataLength * 8;
                while (posBit < endBit) {
                    InformationElement ie = new InformationElement();
                    posBit = ie.readIE(data, posBit);
                    this.addInformationElement(ie);
                }
            }
        }
    }

    /*
     * 8.6.8. FORMAT
     * 
     * The purpose of the FORMAT information element is to indicate a single
     * preferred media CODEC. When sent with a NEW message, the indicated CODEC
     * is the desired CODEC an IAX peer wishes to use for a call. When sent with
     * an ACCEPT message, it indicates the actual CODEC that has been selected
     * for the call. Its data is represented in a 4-octet bitmask according to
     * Section 8.7. Only one CODEC MUST be specified in the FORMAT information
     * element.
     */
    public void setFormat(MediaFormat format) {
        if (format != null) {
            InformationElement ie = new InformationElement(IEType.FORMAT);
            ie.set4Octets(format.getValue());
            this.addInformationElement(ie);
        }
    }

    public MediaFormat getFormat() {
        MediaFormat format = null;
        InformationElement ie = this.getInformationElement(IEType.FORMAT);
        if (ie != null) {
            long l = ie.get4Octets();
            format = VoiceFrame.findMediaFormat(l);
        }
        return format;
    }

    /*
     * 8.6.6. USERNAME
     * 
     * The purpose of the USERNAME information element is to specify the
     * identity of the user participating in an IAX message exchange. It carries
     * UTF-8-encoded data.
     * 
     * The USERNAME information element MAY be sent with IAX NEW, AUTHREQ,
     * REGREQ, REGAUTH, or REGACK messages, or any time a peer needs to identify
     * a user.
     */
    public void setUsername(String username) {
        if (username != null) {
            InformationElement ie = new InformationElement(IEType.USERNAME);
            ie.setUtfString(username);
            this.addInformationElement(ie);
        }
    }

    public String getUsername() {
        String username = null;
        InformationElement ie = this.getInformationElement(IEType.USERNAME);
        if (ie != null) {
            username = ie.getUtfString();
        }
        return username;
    }

    /*
     * 8.6.16. RSA RESULT
     * 
     * The purpose of the RSA RESULT information element is to offer an RSA
     * response to an authentication CHALLENGE. It carries the UTF-8- encoded
     * challenge result. The result is computed as follows: first, compute the
     * SHA1 digest [RFC3174] of the challenge string and second, RSA sign the
     * SHA1 digest using the private RSA key as specified in PKCS #1 v2.0
     * [PKCS]. The RSA keys are stored locally.
     * 
     * Upon receiving an RSA RESULT information element, its value must be
     * verified with the sender's public key to match the SHA1 digest [RFC3174]
     * of the challenge string.
     * 
     * The RSA RESULT information element MAY be sent with IAX AUTHREP and
     * REGREQ messages if an AUTHREQ or REGAUTH and appropriate CHALLENGE have
     * been received. This information element MUST NOT be sent except in
     * response to a CHALLENGE.
     */
    public boolean setRSAResult(String challenge, String password) {
        boolean isOK = false;
        if (challenge != null && password != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("RSA");
                String result = getDigest(md, challenge, password);
                this.setRSAResult(result);
                isOK = true;
            } catch (NoSuchAlgorithmException exc) {
                IaxLog.getLog().error(exc);
            }
        }
        return isOK;
    }

    public void setRSAResult(String result) {
        if (result != null) {
            InformationElement ie = new InformationElement(IEType.RSA_RESULT);
            ie.setUtfString(result);
            this.addInformationElement(ie);
        }
    }

    public String getRSAResult() {
        String result = null;
        InformationElement ie = this.getInformationElement(IEType.RSA_RESULT);
        if (ie != null) {
            result = ie.getUtfString();
        }
        return result;
    }

    /*
     * 8.6.15. MD5 RESULT
     * 
     * The purpose of the MD5 RESULT information element is to offer an MD5
     * response to an authentication CHALLENGE. It carries the UTF-8- encoded
     * challenge result. The MD5 Result value is computed by taking the MD5
     * [RFC1321] digest of the challenge string and the password string.
     * 
     * The MD5 RESULT information element MAY be sent with IAX AUTHREP and
     * REGREQ messages if an AUTHREQ or REGAUTH and appropriate CHALLENGE has
     * been received. This information element MUST NOT be sent except in
     * response to a CHALLENGE.
     */
    public boolean setMD5Result(String challenge, String password) {
        boolean isOK = false;
        if (challenge != null && password != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                String result = getDigest(md, challenge, password);
                this.setMD5Result(result);
                isOK = true;
            } catch (NoSuchAlgorithmException exc) {
                IaxLog.getLog().error(exc);
            }
        }
        return isOK;
    }

    public void setMD5Result(String result) {
        if (result != null) {
            InformationElement ie = new InformationElement(IEType.MD5_RESULT);
            ie.setUtfString(result);
            this.addInformationElement(ie);
        }
    }

    public String getMD5Result() {
        String result = null;
        InformationElement ie = this.getInformationElement(IEType.MD5_RESULT);
        if (ie != null) {
            result = ie.getUtfString();
        }
        return result;
    }

    private String getDigest(MessageDigest md, String challenge, String password) {
        byte[] challengeBytes = InformationElement.stringToUtfBytes(challenge);
        byte[] passwordByte = InformationElement.stringToUtfBytes(password);
        md.update(challengeBytes);
        md.update(passwordByte);
        byte[] digest = md.digest();

        // turn the digest into a HexString
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            buf.append(Arithmetic.toHex(digest[i]));
        }
        String result = buf.toString();
        return result;
    }

    /**
     * 8.6.13. AUTHMETHODS
     * 
     * The purpose of the AUTHMETHODS information element is to indicate the
     * authentication methods a peer accepts. It is sent as a bitmask two octets
     * long. The table below lists the valid authentication methods.
     * 
     * The AUTHMETHODS information element MUST be sent with IAX AUTHREQ and
     * REGAUTH messages.
     */
    public List<AuthMethodType> getSupportedAuthMethods() {
        ArrayList<AuthMethodType> list = new ArrayList<AuthMethodType>();
        long authMethods = this.getAuthMethods();
        for (AuthMethodType f : AuthMethodType.values()) {
            int value = f.getValue();
            if ((value & authMethods) > 0) {
                list.add(f);
            }
        }
        return list;
    }

    public void setAuthMethods(int authMethods) {
        InformationElement ie = new InformationElement(IEType.AUTHMETHODS);
        ie.set2Octets(authMethods);
        this.addInformationElement(ie);
    }

    public long getAuthMethods() {
        int authMethods = 0;
        InformationElement ie = this.getInformationElement(IEType.AUTHMETHODS);
        if (ie != null) {
            authMethods = ie.get2Octets();
        }
        return authMethods;
    }

    /**
     * 8.6.14. CHALLENGE
     * 
     * The purpose of the CHALLENGE information element is to offer the MD5 or
     * RSA challenge to be used for authentication. It carries the actual
     * UTF-8-encoded challenge data.
     * 
     * The CHALLENGE information element MUST be sent with IAX AUTHREQ and
     * REGAUTH messages.
     */
    public void getChallenge(String challenge) {
        InformationElement ie = new InformationElement(IEType.CHALLENGE);
        ie.setUtfString(challenge);
        this.addInformationElement(ie);
    }

    public String getChallenge() {
        String challenge = null;
        InformationElement ie = this.getInformationElement(IEType.CHALLENGE);
        if (ie != null) {
            challenge = ie.getUtfString();
        }
        return challenge;
    }

    /*
     * CALLTOKEN http://downloads.asterisk.org/pub/security/IAX2-security.pdf
     * 
     * 4.1. CALLTOKEN IE Payload For Asterisk, we will encode the payload of the
     * CALLTOKEN IE such that the server is able to validate a received token
     * without having to store any information after transmitting the CALLTOKEN
     * response. The CALLTOKEN IE payload will contain:
     * 
     * - A timestamp (epoch based)
     * 
     * - SHA1 hash of the remote IP address and port, the timestamp, as well
     * some random data generated when Asterisk starts.
     * 
     * When a CALLTOKEN IE is received, its validity will be determined by
     * recalculating the SHA1 hash. If it is a valid token, the timestamp is
     * checked to determine if the token is expired. The token timeout will be
     * hard coded at 10 seconds for now. However, it may be made configurable at
     * some point if it seems to be a useful addition. If the server determines
     * that a received token is expired, it will treat it as an invalid token
     * and not respond to the request.
     * 
     * By using this method, we require no additional memory to be allocated for
     * a dialog, other than what is on the stack for processing the initial
     * request, until token validation is complete. However, one thing to note
     * with this CALLTOKEN IE encoding is that a token would be considered valid
     * by Asterisk every time a client sent it until we considered it an expired
     * token.
     * 
     * However, with use of the "maxcallnumbers" option, this is not actually a
     * problem. It just means that an attacker could hit their call number limit
     * a bit quicker since they would only have to acquire a single token per
     * timeout period, instead of a token per request.
     */
    public void setCallToken(String calltoken) {
        if (calltoken != null) {
            InformationElement ie = new InformationElement(IEType.CALLTOKEN);
            ie.setUtfString(calltoken);
            this.addInformationElement(ie);
        }
    }

    public String getCallToken() {
        String calltoken = null;
        InformationElement ie = this.getInformationElement(IEType.CALLTOKEN);
        if (ie != null) {
            calltoken = ie.getUtfString();
        }
        return calltoken;
    }

    /*
     * 8.6.21. CAUSE
     * 
     * The purpose of the CAUSE information element is to indicate the reason an
     * event occurred. It carries a description of the CAUSE of the event as
     * UTF-8-encoded data. Notification of the event itself is handled at the
     * message level.
     * 
     * The CAUSE information element SHOULD be sent with IAX HANGUP, REJECT,
     * REGREJ, and TXREJ messages.
     */
    public void setCause(String cause) {
        if (cause != null) {
            InformationElement ie = new InformationElement(IEType.CAUSE);
            ie.setUtfString(cause);
            this.addInformationElement(ie);
        }
    }

    public String getCause() {
        String cause = null;
        InformationElement ie = this.getInformationElement(IEType.CAUSE);
        if (ie != null) {
            cause = ie.getUtfString();
        }
        return cause;
    }

    /*
     * 8.6.33. CAUSECODE
     * 
     * The purpose of the CAUSECODE information element is to indicate the
     * reason a call was REJECTed or HANGUPed. It derives from ITU-T
     * Recommendation Q.931. The data field is one octet long and contains an
     * entry from the table below.
     * 
     * The CAUSECODE information element SHOULD be sent with IAX HANGUP, REJECT,
     * REGREJ, and TXREJ messages.
     */
    public void setCauseCode(CauseCodeType type) {
        if (type != null) {
            InformationElement ie = new InformationElement(IEType.CAUSECODE);
            ie.set1Octet(type.getValue());
            this.addInformationElement(ie);
        }
    }

    public CauseCodeType getCauseCode() {
        CauseCodeType type = null;
        InformationElement ie = this.getInformationElement(IEType.CAUSECODE);
        if (ie != null) {
            int causeCode = ie.get1Octet();
            type = findCauseCode(causeCode);
        }
        return type;
    }

    /*
     * 8.6.22. IAX UNKNOWN
     * 
     * The purpose of the IAX UNKNOWN information element is to indicate that a
     * received IAX command was unknown or unrecognized. The 1-octet data field
     * contains the subclass of the received frame that was unrecognized.
     * 
     * The IAX UNKNOWN information element MUST be sent with IAX UNSUPPORT
     * messages.
     */
    public void setUnknown(int subclass) {
        InformationElement ie = new InformationElement(IEType.IAX_UNKNOWN);
        ie.set1Octet(subclass);
        this.addInformationElement(ie);
    }

    public int getUnknown() {
        int subclass = -1;
        InformationElement ie = this.getInformationElement(IEType.IAX_UNKNOWN);
        if (ie != null) {
            subclass = ie.get1Octet();
        }
        return subclass;
    }

    @Override
    public String toShortString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toShortString());
        buf.append(", ");
        if (_iSubclass != null) {
            buf.append(_iSubclass.getName());
        } else {
            buf.append(this.getSubClass()).append("(?)");
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append("\t ");
        if (_iSubclass != null) {
            buf.append(_iSubclass.toString());
        } else {
            buf.append("subclass=").append(this.getSubClass()).append("(?)");
        }
        buf.append("\n");

        Iterator<InformationElement> ieIterator = _informationElements.values()
                .iterator();
        while (ieIterator.hasNext()) {
            InformationElement ie = ieIterator.next();
            buf.append("\t\t").append(ie.toString()).append("\n");
        }
        return buf.toString();
    }

}
