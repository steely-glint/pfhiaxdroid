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
package com.phonefromhere.plain.iax.frames.control;

import com.phonefromhere.plain.iax.frames.FrameType;
import com.phonefromhere.plain.iax.frames.FullFrame;
import com.phonefromhere.plain.util.IaxLog;

public class ControlFrame extends FullFrame {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: ControlFrame.java,v 1.4 2011/03/06 15:10:50 uid1003 Exp $ Copyright Westhawk Ltd";

    private ControlSubclass _cSubclass = null;

    public ControlFrame() {
        this((short) 0, ControlSubclass.RESERVED1);
    }

    public ControlFrame(short sourceCallNumber, ControlSubclass cSubclass) {
        super(sourceCallNumber);
        super.setFrameType(FrameType.CONTROL);
        this.setSubClass(cSubclass);
    }

    @Override
    public void setSubClass(long subClass) {
        ControlSubclass subclass2 = findSubclass((int) subClass);
        setSubClass(subclass2);
        if (subclass2 == null) {
            IaxLog.getLog().debug(this.getClass().getSimpleName()
                    + ".setSubClass(): cannot find subclass with value "
                    + subClass);
            super.setSubClass(subClass);
        }
    }

    public void setSubClass(ControlSubclass cSubclass) {
        _cSubclass = cSubclass;
        if (cSubclass != null) {
            super.setSubClass(_cSubclass.getValue());
        }
    }

    public ControlSubclass getSubClassC() {
        return _cSubclass;
    }

    @Override
    public boolean mustSendAck() {
        return true;
    }

    public static ControlSubclass findSubclass(int value) {
        ControlSubclass type = null;
        for (ControlSubclass type2 : ControlSubclass.values()) {
            //IaxLog.getLog().verb("value ="+value+" candidate="+type2.toString());
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
        if (_cSubclass != null) {
            buf.append(_cSubclass.getName());
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
        if (_cSubclass != null) {
            buf.append(_cSubclass.toString());
        } else {
            buf.append("subclass=").append(this.getSubClass()).append("(?)");
        }
        buf.append("\n");
        return buf.toString();
    }
}
