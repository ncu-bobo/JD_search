package com.wfb.utils;

import com.wfb.pojo.Good;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlParseUtil {

    public static List<Good> parseJD(String keywords) throws Exception{
        String url = "https://search.jd.com/Search?keyword="+keywords;

        Document document = Jsoup.parse(new URL(url), 30000);
        Element j_goodsList = document.getElementById("J_goodsList");

        Elements goods = j_goodsList.getElementsByTag("li");
        ArrayList<Good> goodsList = new ArrayList<>();
        for(Element element : goods) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            String name = element.getElementsByClass("p-name").eq(0).text();
            Good good = new Good(name,price,img);
            goodsList.add(good);
        }

        return goodsList;
    }
}
