package com.codingshuttle.razorpay.common_lib.enums;

public enum PaymentEvent {
    AUTHORIZE_ATTEMPT,
    AUTHORIZE_SUCCESS,
    AUTHORIZE_FAIL,
    CAPTURE_ATTEMPT,
    CAPTURE_REQUEST,
    CAPTURE_FAIL,
    CAPTURE_SUCCESS,
    REFUND_INIT,
    REFUND_COMPLETE,
    SETTLE,
    CANCEL,
    CAPTURE_TIMEOUT
}
