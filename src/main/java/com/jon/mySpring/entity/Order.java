package com.jon.mySpring.entity;

import com.jon.mySpring.Component;
import com.jon.mySpring.Value;
import lombok.Data;

@Data
@Component
public class Order {

    @Value("jon123")
    private String orderId;

    @Value("1000.34")
    private Float price;
}
