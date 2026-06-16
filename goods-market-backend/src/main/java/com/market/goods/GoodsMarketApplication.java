package com.market.goods;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.market.goods.mapper")
public class GoodsMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsMarketApplication.class, args);
    }
}
