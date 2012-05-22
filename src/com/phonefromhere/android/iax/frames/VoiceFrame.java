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

import com.phonefromhere.android.codec.MediaFormat;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt

 8.2.2.  Voice Frame

 The frame carries voice data.
 The subclass specifies the audio format of the data.  Predefined
 voice formats can be found in Section 8.7.
 */

public class VoiceFrame extends FullFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: VoiceFrame.java,v 1.3 2011/02/14 15:51:28 uid1003 Exp $ Copyright Westhawk Ltd";

    private MediaFormat _mSubclass = null;

    protected VoiceFrame() {
        this((short) 0, null);
    }

    public VoiceFrame(short sourceCallNumber, MediaFormat mSubclass) {
        super(sourceCallNumber);
        super.setFrameType(FrameType.VOICE);
        this.setSubClass(mSubclass);
    }

    @Override
    public void setSubClass(long subClass) {
        MediaFormat subclass2 = findMediaFormat(subClass);
        this.setSubClass(subclass2);
    }

    public void setSubClass(MediaFormat mSubclass) {
        _mSubclass = mSubclass;
        if (mSubclass != null) {
            super.setSubClass(_mSubclass.getValue());
        }
    }

    public MediaFormat getSubClassM() {
        return _mSubclass;
    }

    public static MediaFormat findMediaFormat(long value) {
        MediaFormat type = null;
        for (MediaFormat type2 : MediaFormat.values()) {
            if (value == type2.getValue()) {
                type = type2;
                break;
            }
        }
        return type;
    }
}
