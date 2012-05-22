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
package com.phonefromhere.android.iax.frames.iax.ie;

/*
 * http://www.iana.org/assignments/iax-parameters/iax-parameters.xml
 */
public enum IEType {
    CALLED_NUMBER(0x01, "CALLED_NUMBER"),
    CALLING_NUMBER(0x02, "CALLING_NUMBER"),
    CALLING_ANI(0x03, "CALLING_ANI"),
    CALLING_NAME(0x04, "CALLING_NAME"),
    CALLED_CONTEXT(0x05, "CALLED_CONTEXT"),
    USERNAME(0x06, "USERNAME"),
    PASSWORD(0x07, "PASSWORD"),
    CAPABILITY(0x08, "CAPABILITY"),
    FORMAT(0x09, "FORMAT"),
    LANGUAGE(0x0a, "LANGUAGE"),
    VERSION(0x0b, "VERSION"),
    ADSICPE(0x0c, "ADSICPE"),
    DNID(0x0d, "DNID"),
    AUTHMETHODS(0x0e, "AUTHMETHODS"),
    CHALLENGE(0x0f, "CHALLENGE"),
    MD5_RESULT(0x10, "MD5_RESULT"),
    RSA_RESULT(0x11, "RSA_RESULT"),
    APPARENT_ADDR(0x12, "APPARENT_ADDR"),
    REFRESH(0x13, "REFRESH"),
    DPSTATUS(0x14, "DPSTATUS"),
    CALLNO(0x15, "CALLNO"),
    CAUSE(0x16, "CAUSE"),
    IAX_UNKNOWN(0x17, "IAX_UNKNOWN"),
    MSGCOUNT(0x18, "MSGCOUNT"),
    AUTOANSWER(0x19, "AUTOANSWER"),
    MUSICONHOLD(0x1a, "MUSICONHOLD"),
    TRANSFERID(0x1b, "TRANSFERID"),
    RDNIS(0x1c, "RDNIS"),
    RESERVED1(0x1d, "RESERVED1"),
    RESERVED2(0x1e, "RESERVED2"),
    DATETIME(0x1f, "DATETIME"),
    RESERVED3(0x20, "RESERVED3"),
    RESERVED4(0x21, "RESERVED4"),
    RESERVED5(0x22, "RESERVED5"),
    RESERVED6(0x23, "RESERVED6"),
    RESERVED7(0x24, "RESERVED7"),
    RESERVED8(0x25, "RESERVED8"),
    CALLINGPRES(0x26, "CALLINGPRES"),
    CALLINGTON(0x27, "CALLINGTON"),
    CALLINGTNS(0x28, "CALLINGTNS"),
    SAMPLINGRATE(0x29, "SAMPLINGRATE"),
    CAUSECODE(0x2a, "CAUSECODE"),
    ENCRYPTION(0x2b, "ENCRYPTION"),
    ENCKEY(0x2c, "ENCKEY"),
    CODEC_PREFS(0x2d, "CODEC_PREFS"),
    RR_JITTER(0x2e, "RR_JITTER"),
    RR_LOSS(0x2f, "RR_LOSS"),
    RR_PKTS(0x30, "RR_PKTS"),
    RR_DELAY(0x31, "RR_DELAY"),
    RR_DROPPED(0x32, "RR_DROPPED"),
    RR_OOO(0x33, "RR_OOO"),
    OSPTOKEN(0x34, "OSPTOKEN"),
    VARIABLE(0x35, "VARIABLE"),
    CALLTOKEN(0x36, "CALLTOKEN"),
    CAPABILITY2(0x37, "CAPABILITY2"),
    FORMAT2(0x38, "FORMAT2");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: IEType.java,v 1.1 2011/02/03 14:33:15 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private IEType(int value, String name) {
        _value = value;
        _name = name;
    }

    public int getValue() {
        return _value;
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        String str = "IEType: " + _name + "(0x" + Integer.toHexString(_value)
                + ")";
        return str;
    }
}
