package com.blog.content.controller;


import com.alibaba.fastjson.JSONObject;
//import com.blog.redis.service.RedisService;
//import com.blog.mq.entity.RocketMQMessage;
//import com.blog.mq.service.MQProducerService;
import com.blog.log.annotation.Log;
import com.blog.log.enums.BusinessType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class TestController {

//    @Resource
//    private RedisService redisService;

//    @Resource
//    private MQProducerService mqProducerService;

//    @Resource
//    private TestEsInfoMapper testEsInfoMapper;

    @GetMapping("/getTest")
    @Log(title = "网格分类数据", businessType = BusinessType.GRANT)
    public String getTest(@RequestParam String key) {

        return key;
    }

//    @GetMapping("/getTest")
//    public TestESInfo getKey(@RequestParam String key) {
//        LambdaEsQueryWrapper<TestESInfo> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(TestESInfo::getKey, key);
//        TestESInfo testESInfo = testEsInfoMapper.selectOne(wrapper);
//        return testESInfo;
//    }

//    @GetMapping("/setTest")
//    public boolean setKey(@RequestParam String key, @RequestParam String value) {
//
//        RocketMQMessage rocketMQMessage = new RocketMQMessage();
//        rocketMQMessage.setTopic("BLOG_SYSTEM_DATA");
//        rocketMQMessage.setTag("CONTENT");
//        rocketMQMessage.setMessage("11111");
//        mqProducerService.send("BLOG_SYSTEM_DATA", "CONTENT", JSONObject.toJSONString(rocketMQMessage));
////        if (!testEsInfoMapper.existsIndex("test_info")) {
////            testEsInfoMapper.createIndex();
////        }
////        testEsInfoMapper.deleteIndex("test_info");
////
////        TestESInfo testESInfo = new TestESInfo();
////        testESInfo.setKey(key);
////        testESInfo.setValue(value);
////        testEsInfoMapper.insert(testESInfo);
//
//        return redisService.setString(key, value, 10000);
//    }

//    @GetMapping("/delTest")
//    public boolean delKey(@RequestParam String key) {
//        return testEsInfoMapper.deleteIndex(key);
//    }

//    @Resource
//    private AnchorPointEsMapper anchorPointEsMapper;
//    @Resource
//    private CommonPoolEsMapper commonPoolEsMapper;
//    @Resource
//    private CustomerGroupInfoEsMapper customerGroupInfoEsMapper;
//    @Resource
//    private PrecisionCustomerPointEsMapper precisionCustomerPointEsMapper;
//
//
//    @GetMapping("/createEsTable")
//    public boolean createEsTable() {
//        anchorPointEsMapper.createIndex();
//        commonPoolEsMapper.createIndex();
//        customerGroupInfoEsMapper.createIndex();
//        precisionCustomerPointEsMapper.createIndex();
//        return true;
//    }
//
//    @GetMapping("/deleteEsTable")
//    public boolean deleteEsTable() {
//        anchorPointEsMapper.deleteIndex("gmms_ps_cust_anchor_point");
//        commonPoolEsMapper.deleteIndex("gmms_grid_allocated_cust_list");
//        customerGroupInfoEsMapper.deleteIndex("customer_group_info");
//        precisionCustomerPointEsMapper.deleteIndex("precision_customer_point_info");
//        return true;
//    }
//
//    @GetMapping("/setAnchorPointEsData")
//    public boolean setAnchorPointEsData() {
//        AnchorPointInfo anchorPointInfo = new AnchorPointInfo();
//
//        anchorPointInfo.setCustomerId("1610040624");
//        List<String> customerList = new ArrayList<>();
//        customerList.add("1");
//        customerList.add("2");
//        anchorPointInfo.setCustomerAttribute(customerList);
//        anchorPointInfo.setCustomerAddress("浙江省");
//        anchorPointInfo.setAddrType("0101");
//        anchorPointInfo.setComnAddrFlag("1");
//        anchorPointInfo.setLon("120.00000000");
//        anchorPointInfo.setLat("30.30597087");
//        anchorPointInfo.setGridCode("293653");
//        anchorPointInfo.setGridName("测试ES网格");
//        anchorPointInfo.setParentCode("");
//        anchorPointInfo.setParentName("");
//        anchorPointInfo.setBuildingId("7");
//        anchorPointInfo.setBankDeptCode("100");
//        anchorPointInfo.setCreditFlag("1");
//        anchorPointEsMapper.insert(anchorPointInfo);
//        return true;
//    }
}
