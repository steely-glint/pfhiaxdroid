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

import java.io.UnsupportedEncodingException;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 8.2.7.  Text Frame

 The frame carries a non-control text message in UTF-8 [RFC3629]
 format.
 All text frames have a subclass of 0.
 */

public class TextFrame extends FullFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: TextFrame.java,v 1.3 2011/02/14 15:51:28 uid1003 Exp $ Copyright Westhawk Ltd";

    public final static String UTF8 = "UTF-8";
    private String _text;

    protected TextFrame() {
        this((short) 0);
    }

    public TextFrame(short sourceCallNumber) {
        super(sourceCallNumber);
        super.setFrameType(FrameType.TEXT);
        this.setSubClass(0);
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        byte[] data = null;
        try {
            data = text.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            data = text.getBytes();
        }
        super.setData(data);
    }

    @Override
    public void setData(byte[] data) {
        String text = "";
        try {
            text = new String(data, UTF8);
        } catch (UnsupportedEncodingException e) {
            text = new String(data);
        }
        this.setText(text);
    }

}
