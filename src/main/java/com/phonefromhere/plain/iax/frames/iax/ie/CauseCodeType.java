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
package com.phonefromhere.plain.iax.frames.iax.ie;

public enum CauseCodeType {
    UNALLOCATED(1, "Unassigned/unallocated number"),
    NO_ROUTE_TRANSIT_NET(2, "No route to specified transit network"),
    NO_ROUTE_DESTINATION(3, "No route to destination"),
    CHANNEL_UNACCEPTABLE(6, "Channel unacceptable"),
    CALL_AWARDED_DELIVERED(7, "Call awarded and delivered"),
    NORMAL_CLEARING(16, "Normal call clearing"),
    USER_BUSY(17, "User busy"),
    NO_USER_RESPONSE(18, "No user response"),
    NO_ANSWER(19, "No answer"),
    CALL_REJECTED(21, "Call rejected"),
    NUMBER_CHANGED(22, "Number changed"),
    DESTINATION_OUT_OF_ORDER(27, "Destination out of order"),
    INVALID_NUMBER_FORMAT(28, "Invalid number format/incomplete number"),
    FACILITY_REJECTED(29, "Facility rejected"),
    RESPONSE_TO_STATUS_ENQUIRY(30, "Response to status enquiry"),
    NORMAL_UNSPECIFIED(31, "Normal, unspecified"),
    NORMAL_CIRCUIT_CONGESTION(34, "No circuit/channel available"),
    NETWORK_OUT_OF_ORDER(38, "Network out of order"),
    NORMAL_TEMPORARY_FAILURE(41, "Temporary failure"),
    SWITCH_CONGESTION(42, "Switch congestion"),
    ACCESS_INFO_DISCARDED(43, "Access information discarded"),
    REQUESTED_CHAN_UNAVAIL(44, "Requested channel not available"),
    PRE_EMPTED(45, "Preempted (causes.h only)"),
    RESOUCE_UNAVAIL(47, "Resource unavailable, unspecified (Q.931 only)"),
    FACILITY_NOT_SUBSCRIBED(50, "Facility not subscribed (causes.h only)"),
    OUTGOING_CALL_BARRED(52, "Outgoing call barred (causes.h only)"),
    INCOMING_CALL_BARRED(54, "Incoming call barred (causes.h only)"),
    BEARER_CAPABILITY_NOTAUTH(57, "Bearer capability not authorized"),
    BEARER_CAPABILITY_NOTAVAIL(58, "Bearer capability not available"),
    SERVICE_NOT_AVAIL(63, "Service or option not available (Q.931 only)"),
    BEARER_CAPABILITY_NOTIMPL(65, "Bearer capability not implemented"),
    CHAN_NOT_IMPLEMENTED(66, "Channel type not implemented"),
    FACILITY_NOT_IMPLEMENTED(69, "Facility not implemented"),
    RESTRICTED_BEARER_CAPABILITYT(
            70,
            "Only restricted digital information bearer capability is available (Q.931 only)"),
    SERVICE_NOT_AVAIL2(79, "Service or option not available (Q.931 only)"),
    INVALID_CALL_REFERENCE(81, "Invalid call reference"),
    CHANNEL_NOT_EXIST(82, "Identified channel does not exist (Q.931 only)"),
    CALL_IDENTITY_NOT_EXIST(83,
            "A suspended call exists, but this call identity does not (Q.931 only)"),
    CALL_IDENTITY_IN_USE(84, "Call identity in use (Q.931 only)"),
    NO_CALL_SUSPENDED(85, "No call suspended (Q.931 only)"),
    CALL_CLEARED(86, "Call has been cleared (Q.931 only)"),
    INCOMPATIBLE_DESTINATION(88, "Incompatible destination"),
    INVALID_TRANSIT_NET(91, "Invalid transit network selection (Q.931 only)"),
    INVALID_MSG_UNSPECIFIED(95, "Invalid message, unspecified"),
    MANDATORY_IE_MISSING(96,
            "Mandatory information element missing (Q.931 only)"),
    MESSAGE_TYPE_NONEXIST(97, "Message type nonexistent/not implemented"),
    WRONG_MESSAGE(98, "Message not compatible with call state"),
    IE_NONEXIST(99, "Information element nonexistent"),
    INVALID_IE_CONTENTS(100, "Invalid information element contents"),
    WRONG_CALL_STATE(101, "Message not compatible with call state"),
    RECOVERY_ON_TIMER_EXPIRE(102, "Recovery on timer expiration"),
    MANDATORY_IE_LENGTH_ERROR(103,
            "Mandatory information element length error (causes.h only)"),
    PROTOCOL_ERROR(111, "Protocol error, unspecified"),
    INTERWORKING(127, "Internetworking, unspecified");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: CauseCodeType.java,v 1.1 2011/02/10 16:16:25 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _message;

    private CauseCodeType(int value, String name) {
        _value = value;
        _message = name;
    }

    public int getValue() {
        return _value;
    }

    public String getMessage() {
        return _message;
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + ": " + _message + "("
                + _value + ")";
        return str;
    }
}
