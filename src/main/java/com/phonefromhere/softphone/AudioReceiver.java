/*
 * Copyright 2011 Voxeo Corp.
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
package com.phonefromhere.softphone;

/**
 * The AudioReceiver interface notifies when new audio is available.
 * 
 * @see AudioFace#addAudioReceiver(AudioReceiver r)
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 1.1 $ $Date: 2011/02/03 14:33:15 $
 */
public interface AudioReceiver {

    static final String version_id = "@(#)$Id: AudioReceiver.java,v 1.1 2011/02/03 14:33:15 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * Notifies that new data is available from the microphone.
     * 
     * @param audioFace
     *            The AudioFace object that give notification
     * @param bytesAvailable
     *            The number of bytes that is available to read.
     * 
     * @see AudioFace#addAudioReceiver
     * @see AudioFace#readStampedAudio
     */
    public void newAudioDataReady(AudioFace audioFace, int bytesAvailable);

}
