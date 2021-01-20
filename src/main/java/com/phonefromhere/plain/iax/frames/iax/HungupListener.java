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
package com.phonefromhere.plain.iax.frames.iax;

import com.phonefromhere.plain.iax.frames.iax.ie.CauseCodeType;

public interface HungupListener {
    static final String version_id = "@(#)$Id: HungupListener.java,v 1.1 2011/02/10 16:16:24 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * The other end hung up or sent an ACK to our HANGUP
     */
    public void hungup(CauseCodeType type, String message);    
}
