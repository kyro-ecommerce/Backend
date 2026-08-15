package com.kyro.order.event;

import java.util.Map;

public record OrderConfirmedEvent(Map<String, Object> payload) {}
