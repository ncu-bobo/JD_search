package com.wfb.controller;

import com.wfb.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class GoodController {
    @Autowired
    private GoodService goodService;

    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws Exception {
        return goodService.parseGood(keyword);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> search(@PathVariable("keyword")String keyword,
                                            @PathVariable("pageNo")int pageNo,
                                            @PathVariable("pageSize")int pageSize) throws IOException {
        return goodService.searchPage(keyword,pageNo,pageSize);
    }
}
