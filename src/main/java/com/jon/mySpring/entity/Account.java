package com.jon.mySpring.entity;

import com.jon.mySpring.Autowired;
import com.jon.mySpring.Component;
import com.jon.mySpring.Qualifier;
import com.jon.mySpring.Value;
import lombok.Data;

@Data
@Component
public class Account {
    @Value("1")
    private Integer id;
    @Value("张三")
    private String name;
    @Value("22")
    private Integer age;

    @Autowired
    //@Qualifier("myOrder")
    private Order order;
}
