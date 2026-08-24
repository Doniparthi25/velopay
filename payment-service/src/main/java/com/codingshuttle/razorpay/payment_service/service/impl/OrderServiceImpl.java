package com.codingshuttle.razorpay.payment_service.service.impl;


import com.codingshuttle.razorpay.common_lib.enums.EventAggregateType;
import com.codingshuttle.razorpay.common_lib.enums.OrderStatus;
import com.codingshuttle.razorpay.common_lib.exceptions.BusinessRuleViolationException;
import com.codingshuttle.razorpay.common_lib.exceptions.DuplicateResourceException;
import com.codingshuttle.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.codingshuttle.razorpay.payment_service.dto.request.CreateOrderRequest;
import com.codingshuttle.razorpay.payment_service.dto.response.OrderResponse;
import com.codingshuttle.razorpay.payment_service.dto.response.PaymentResponse;
import com.codingshuttle.razorpay.payment_service.entity.OrderRecord;
import com.codingshuttle.razorpay.payment_service.entity.Payment;
import com.codingshuttle.razorpay.payment_service.mapper.OrderMapper;
import com.codingshuttle.razorpay.payment_service.mapper.PaymentMapper;
import com.codingshuttle.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.codingshuttle.razorpay.payment_service.repository.OrderRepository;
import com.codingshuttle.razorpay.payment_service.repository.PaymentRepository;
import com.codingshuttle.razorpay.payment_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final CustomerService customerService;
    private final OutboxEventPublisher eventPublisher;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt() !=null && orderRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE","Order with receipt already exists: " + request.receipt());
        }

        UUID customerId = null;
        if(request.customer() != null) {
            customerId = customerService.findOrCreate(merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone());
        }
        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())

                .merchantId(merchantId)
                .customerId(customerId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() !=null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDER,order.getId(),"ORDER_CREATED",
                Map.of("orderId", order.getId(),
                        "merchantId",merchantId.toString(),
                        "orderStatus",order.getOrderStatus().name(),
                        "amountUnits",order.getAmount().getAmountUnits(),
                        "amountCurrency",order.getAmount().getCurrency()));

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchanId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(merchanId,orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order",orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(merchantId,orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order",orderId));

        if(order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID){
            throw  new BusinessRuleViolationException("ORDER_CANNOT_CANCEL","Cannot cancel order with status: " + order.getOrderStatus().name());

        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        order =orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CANCELLED",
                Map.of("orderId", order.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency()
                )
        );
        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(merchantId,orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order",orderId));

        List<Payment> paymentList =paymentRepository.findByOrder_id(order);
        return paymentMapper.toResponseList(paymentList);
    }
}
