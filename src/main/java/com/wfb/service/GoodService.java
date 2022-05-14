package com.wfb.service;

import com.alibaba.fastjson.JSON;
import com.wfb.pojo.Good;
import com.wfb.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoodService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //解析数据，放入 es 索引中
    public Boolean parseGood(String keywords) throws Exception {
        List<Good> goods = HtmlParseUtil.parseJD(keywords);

        //把查询到的数据放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2s");

        for(int i=0; i<goods.size(); i++){
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                            .source(JSON.toJSONString(goods.get(i)), XContentType.JSON));
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        return !bulk.hasFailures();
    }

    // 获取数据实现搜索功能
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo < 1){
            pageNo = 1;
        }

        SearchRequest searchRequest = new SearchRequest("jd_goods");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //搜索条件
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);        //使得一个结果中只出现一个高亮
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //超时
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            //解析高亮字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            if(title != null){
                Text[] fragments = title.fragments();
                String name = "";
                //将原来的字段换成我们高亮的字段
                for(Text fragement : fragments) {
                    name += fragement;
                }
                sourceAsMap.put("title",name);
            }

            list.add(sourceAsMap);
        }

        return  list;
    }
}
