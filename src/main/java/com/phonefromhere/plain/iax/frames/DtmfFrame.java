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

import com.phonefromhere.plain.util.Arithmetic;

public class DtmfFrame extends FullFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: DtmfFrame.java,v 1.2 2011/02/10 16:16:24 uid1003 Exp $ Copyright Westhawk Ltd";

    private char _dtmfDigit;

    protected DtmfFrame() {
        this((short) 0);
    }

    public DtmfFrame(short sourceCallNumber) {
        super(sourceCallNumber);
        super.setFrameType(FrameType.DTMF);
    }
    
    public void setDtmfDigit(char dtmfDigit) {
        _dtmfDigit = dtmfDigit;
        byte b = Arithmetic.toByte(dtmfDigit);
        super.setSubClass(b);
    }

    public char getDtmfDigit() {
        return _dtmfDigit;
    }

    public void setSubClass(int subClass) {
        char dtmfDigit = Arithmetic.toChar((byte) subClass);
        this.setDtmfDigit(dtmfDigit);
    }
}
