package com.codingshuttle.razorpay.payment_service.processor;


import com.codingshuttle.razorpay.payment_service.processor.dto.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment_service.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
