package com.playground.service;

import java.io.IOException;

public interface RefundService {
    public String getToken(String restApiKey,String restApiSecret) throws IOException;
    public void refundWithToken(String token, Long orderId) throws IOException;
}
