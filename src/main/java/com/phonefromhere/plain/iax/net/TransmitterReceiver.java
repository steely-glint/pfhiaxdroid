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
package com.phonefromhere.plain.iax.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.phonefromhere.plain.iax.CallLeg;
import com.phonefromhere.plain.iax.frames.Frame;
import com.phonefromhere.plain.iax.frames.FullFrame;
import com.phonefromhere.plain.util.IaxLog;
import com.phonefromhere.softphone.AudioFace;
import com.phono.srtplight.Log;


/**
 * This class sends and receives datagram packets from a single Peer.
 * 
 * @author birgit
 * 
 */
public class TransmitterReceiver {

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: TransmitterReceiver.java,v 1.12 2011/03/16 15:44:58 uid100 Exp $ Copyright Westhawk Ltd";
    public final static int IAX_PORT = 4569;
    public final static int MAX_PACKET_LEN = 2048;
    public final static int CALLLEG_EXPIRE_MS = 5000;
    //private static final Object MAP_LOCK = new Object();
    // we think there is only a single call, so we can take the risk.
    private String _peer = null;
    private int _port = 0;
    private Thread _recvThread;
    private Thread _sendThread;
    private InetAddress _peerAddr = null;
    private DatagramSocket _soc = null;
    private DatagramPacket _datagramIn = null;
    private DatagramPacket _datagramOut = null;
    private byte[] _datagramBuf;
    private volatile boolean _stopReceiving = false;
    private volatile boolean _stopSending = false;
    private boolean _isDestroyed = false;
    private boolean _isHangupAllCalls = false;
    private Map<Short, CallLeg> _callLegMapOnSource;
    private Map<Short, CallLeg> _callLegMapOnDest;
    private Random _randomSourceIdentifierGenerator;
    private final static boolean REALLOC = true;

    public TransmitterReceiver(String peer) throws java.io.IOException {
        this(peer, IAX_PORT);
    }

    public TransmitterReceiver(String peer, int port)
            throws java.io.IOException {
        _callLegMapOnSource = new HashMap<Short, CallLeg>();
        _callLegMapOnDest = new HashMap<Short, CallLeg>();
        _randomSourceIdentifierGenerator = new Random();
        createDatagramSocket(peer, port);
        startReceiving();
        startSending();
    }

    public TransmitterReceiver() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void recvLoop() {
        byte[] input = new byte[0];
        while (!_stopReceiving) {
            try {
                if (REALLOC) {
                     IaxLog.getLog().verb(this.getClass().getSimpleName()
                            + ".recvLoop(): alloc a fresh datagramPacket: ");
                    _datagramBuf = new byte[MAX_PACKET_LEN];
                    _datagramIn = new DatagramPacket(_datagramBuf, MAX_PACKET_LEN);
                }
                _soc.receive(_datagramIn);

                if (_datagramIn != null && _datagramIn.getLength() > 0) {
                    int len = _datagramIn.getLength();
                    if (input.length != len) {
                        input = new byte[len]; // cache for miniframes saves 49 allocs/sec
                    }
                    System.arraycopy(_datagramBuf, 0, input, 0, len);
                    IaxLog.getLog().verb(this.getClass().getSimpleName()
                            + ".recvLoop(): got frame of length: " + len);
                    Frame frame = null;
                    try {
                        frame = Frame.createFrame(input);
                    } catch (Exception x) {
                        IaxLog.getLog().error(this.getClass().getSimpleName()
                                + ".recvLoop(): messup with parsing frame ");
                        com.phonefromhere.plain.util.Arithmetic.printData(input);
                        throw (x);
                    }
                    if (frame != null) {
                        // IaxLog.getLog().iax(
                        // this.getClass().getSimpleName()
                        // + ".recvLoop(): " + frame.toString());
                        CallLeg callLeg = findCallLeg(frame);
                        if (callLeg != null) {
                            callLeg.receivedFrame(frame);
                        } else {
                            // this could be an incoming NEW (me thinks)
                            // but we don't support that

                            // TODO - do something ?
                            IaxLog.getLog().error(this.getClass().getSimpleName()
                                    + ".recvLoop(): cannot find callleg for frame "
                                    + frame.toString());
                        }
                    }
                }
            } catch (Throwable exc) {
                if (!(exc instanceof java.net.SocketTimeoutException)) {
                    IaxLog.getLog().error(exc.toString());
                    exc.printStackTrace();
                }
            }
        }
    }

    public void sendLoop() {
        while (!_stopSending) {
            sendOneLoop();
        }

        // one more loop after stopSending to give the Hangups a chance:
        sendOneLoop();
//        synchronized (MAP_LOCK) {
        _callLegMapOnDest.clear();
        _callLegMapOnSource.clear();
//        }
    }

    protected void sendOneLoop() {
        long nap = CALLLEG_EXPIRE_MS;
        // loop over the calls and make it sent its packets
        Set<Short> keys = _callLegMapOnSource.keySet();
        Iterator<Short> keysIter = keys.iterator();
        long now = new Date().getTime();


        while (keysIter.hasNext()) {
            Short key = keysIter.next();
            CallLeg callLeg = _callLegMapOnSource.get(key);

            if (callLeg.isFinished()) {
                // check how long ago it was torn down
                // if long enough, remove the callleg, so the
                // source identifier becomes available again.
                long diff = now - callLeg.getFinishedOutAt();
                if (diff > CALLLEG_EXPIRE_MS) {
//                    synchronized (MAP_LOCK) {
                    _callLegMapOnDest.remove(new Short(callLeg.getDestIdentifier()));
                    keysIter.remove();
//                    }
                }
            } /* let the leg do this itself...
            else {
            callLeg.sendFrames();
            }*/
        }
        try {
            Thread.sleep(nap);
        } catch (InterruptedException ex) {
            ; // really don't care
        }

    }

    synchronized public void sendFrame(Frame frame) {
        if (frame.isFullFrame()) {
            if (frame.getRetries() == 0) {
                IaxLog.getLog().debug(
                        this.getClass().getSimpleName() + ".sendFrame(): "
                        + frame.toString());
            } else {
                IaxLog.getLog().debug(
                        this.getClass().getSimpleName() + ".sendFrame(): "
                        + frame.toShortString());
            }
        }
        byte[] packet = frame.getEncodedFrame();

        if (_datagramOut == null) {
            _datagramOut = new DatagramPacket(packet,
                    packet.length);
        } else {
            _datagramOut.setData(packet);
            _datagramOut.setLength(packet.length);
        }
        try {
            if (_soc != null) {
                _soc.send(_datagramOut);
            }
        } catch (IOException exc) {
            IaxLog.getLog().error(exc.toString());
        }
    }

    public void destroy() {
        if (_isDestroyed == false) {
            IaxLog.getLog().debug(
                    this.getClass().getSimpleName() + ".destroy()");
            _isDestroyed = true;

            hangupAllCallsNClose();
        }
    }

    protected void createDatagramSocket(String peer, int port)
            throws java.io.IOException {
        _peer = peer;
        _port = port;
        IaxLog.getLog().debug(
                this.getClass().getSimpleName() + ".createDatagramSocket(): "
                + "peer=" + _peer + ", port=" + _port);
        try {
            _peerAddr = InetAddress.getByName(_peer);
            //_soc = new DatagramSocket(_port);
            _soc = new DatagramSocket();
            //_soc.bind(null);
            _soc.connect(_peerAddr, _port);
            //_soc.setSoTimeout(1500); turns out plain kills the socket.
            if (REALLOC) {
                _datagramBuf = new byte[MAX_PACKET_LEN];
                _datagramIn = new DatagramPacket(_datagramBuf, MAX_PACKET_LEN);
            }
        } catch (SocketException exc) {
            String str = "Transmitter: port=" + _port + " " + exc.getMessage();
            throw (new SocketException(str));
        } catch (UnknownHostException exc) {
            String str = "Transmitter: peer=" + _peer + " " + exc.getMessage();
            throw (new UnknownHostException(str));
        }
    }

    protected void startReceiving() {
        if (_recvThread == null) {
            Runnable recvRunnable = new Runnable() {

                @Override
                public void run() {
                    recvLoop();
                }
            };
            _recvThread = new Thread(recvRunnable, "recv_" + _peer + "_"
                    + _port+"_"+this._soc.getLocalSocketAddress().toString());
            //_recvThread.setPriority(Thread.NORM_PRIORITY);
            _recvThread.start();
        }
    }

    protected void startSending() {
        if (_sendThread == null) {
            Runnable sendRunnable = new Runnable() {

                @Override
                public void run() {
                    sendLoop();
                }
            };
            _sendThread = new Thread(sendRunnable, "dmh_" + _peer + "_"
                    + _port);
            //_sendThread.setPriority(Thread.NORM_PRIORITY);
            _sendThread.start();
        }

    }

    public CallLeg newCallLeg(AudioFace audio) {
        CallLeg callLeg = null;
        // find an unused source identifier and creates a new CallLeg
        short sourceId = (short) Math.abs((short) _randomSourceIdentifierGenerator.nextInt());
        Short sourceIdI = new Short(sourceId);
        while (_callLegMapOnSource.containsKey(sourceIdI)) {
            sourceId = (short) Math.abs((short) _randomSourceIdentifierGenerator.nextInt());
            sourceIdI = new Short(sourceId);
        }
        callLeg = new CallLeg(this, sourceId);
        callLeg.setAudioFace(audio);
//        synchronized (MAP_LOCK) {
        _callLegMapOnSource.put(sourceIdI, callLeg);
        // can't add to _callLegMapOnDest, since we don't know dest no yet!
//        }
        callLeg.startRetry();
        return callLeg;
    }
    private short _cachedSno = -1;
    private CallLeg _cachedCallLeg = null;

    private CallLeg findCallLeg(Frame frame) {
        CallLeg callLeg = null;
        if (frame.isFullFrame()) {
            FullFrame fullFrame = (FullFrame) frame;
            // Their destination call number == our source call number
            short theirDestNumber = fullFrame.getDestinationCallNumber();
            callLeg = findCallLegViaSource(theirDestNumber);
        } else {
            // Their source call number == our destination call number
            short theirSourceCallNumber = frame.getSourceCallNumber();
            if (_cachedSno == theirSourceCallNumber) {
                callLeg = _cachedCallLeg;
            } else {
                callLeg = findCallLegViaDest1(theirSourceCallNumber);
                if (callLeg == null) {
                    // this callLeg isn't (yet) in the _callLegMapOnDest
                    // see if we can find it by searching
                    callLeg = findCallLegViaDest2(theirSourceCallNumber);
                    if (callLeg != null) {
                        // found, now add it to _callLegMapOnDest for next time!
                        // synchronized (MAP_LOCK) {
                        Short myDestIdI = new Short(callLeg.getDestIdentifier());
                        _callLegMapOnDest.put(myDestIdI, callLeg);
                        // }
                    }
                }
                _cachedCallLeg = callLeg;
                _cachedSno = theirSourceCallNumber;
            }
        }
        return callLeg;
    }

    private CallLeg findCallLegViaSource(short mySourceId) {
        Short sourceIdI = new Short(mySourceId);
        CallLeg callLeg = _callLegMapOnSource.get(sourceIdI);
        return callLeg;
    }

    private CallLeg findCallLegViaDest1(short myDestId) {
        Short myDestIdI = new Short(myDestId);
        CallLeg callLeg = _callLegMapOnDest.get(myDestIdI);
        return callLeg;
    }

    private CallLeg findCallLegViaDest2(short myDestId) {
        CallLeg callLeg = null;
        boolean found = false;
        Iterator<CallLeg> callLegIter = _callLegMapOnSource.values().iterator();
        while (callLegIter.hasNext() && !found) {
            callLeg = callLegIter.next();
            found = (callLeg.getDestIdentifier() == myDestId);
        }
        if (!found) {
            callLeg = null;
        }
        return callLeg;
    }

    private void hangupAllCallsNClose() {
        if (_isHangupAllCalls == false) {
            IaxLog.getLog().debug(this.getClass().getSimpleName()
                    + ".hangupAllCallsNClose()");
            _isHangupAllCalls = true;
            _stopReceiving = true;

            // send hangups
            // loop over the calls and make it sent hangups
            Set<Short> keys = _callLegMapOnSource.keySet();
            Iterator<Short> keysIter = keys.iterator();
            while (keysIter.hasNext()) {
                Short key = keysIter.next();
                CallLeg callLeg = _callLegMapOnSource.get(key);
                if (callLeg.isFinished() == false) {
                    callLeg.destroy();
                }
            }

            // will do one more sendOneLoop after _stopSending is set
            _stopSending = true;

            if (_soc != null) {
                IaxLog.getLog().debug(
                        getClass().getSimpleName()
                        + ".destroy(): Closing socket ");
                _soc.close();
                _soc = null;
            }
            try {
                _recvThread.join(1000);
            } catch (InterruptedException e) {
            }
            try {
                _sendThread.join(1000);
            } catch (InterruptedException e) {
            }
            _recvThread = null;
            _sendThread = null;
        }
    }
}
