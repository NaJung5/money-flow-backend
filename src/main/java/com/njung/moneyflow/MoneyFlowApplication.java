package com.njung.moneyflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MoneyFlowApplication {

    public static void main(String[] args) {
        log.info(String.valueOf(Math.PI));
//        SpringApplication.run(MoneyFlowApplication.class, args);
    }

}
