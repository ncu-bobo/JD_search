package com.wfb;

import com.wfb.service.GoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
class EsJingdongApplicationTests {

    @Autowired
    private GoodService goodService;
    @Test
    void contextLoads() throws Exception {
        goodService.parseGood("java");
    }

    @Test
    void testSearch() throws IOException {
        goodService.searchPage("java", 1, 10).forEach(System.out::println);
    }

}
