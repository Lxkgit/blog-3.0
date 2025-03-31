//package com.blog.file.controller;
//
///**
// * @description: mq
// * @Author: lxk
// * @date 2023/6/27 19:34
// */
//
//@Slf4j
//@RestController
//@RequestMapping("/mq")
//public class MQTestController {
//
//
//    @Resource
//    private MQProducerService mqProducerService;
//
//    @GetMapping("/send")
//    public Result selectArticleById(@RequestParam(value = "msg") Integer msg, @RequestParam(value = "type") Integer type, @RequestParam(value = "userId") Integer userId) {
//        ContentCountVo contentCountVo = new ContentCountVo();
//        contentCountVo.setUserId(userId);
//        contentCountVo.setArticleCount(msg);
//        contentCountVo.setDocCount(msg);
//        contentCountVo.setDiaryCount(msg);
//        RocketMQMessage  rocketMQMessage = new RocketMQMessage();
//        rocketMQMessage.setTopic(RocketMQTopicEnum.BLOG_USER_DATA.getTopic());
//        rocketMQMessage.setTag(RocketMQTopicEnum.BLOG_USER_DATA.getTag());
//        rocketMQMessage.setMessage(JSON.toJSONString(contentCountVo));
//        rocketMQMessage.setMqMsgType(1);
//        mqProducerService.sendSyncOrderly(rocketMQMessage);
//        return ResultFactory.buildSuccessResult();
//    }
//
//    @PostMapping("/test")
//    public Result test(@RequestBody @Validated NettySyncBlogFileDto fileSync) throws ValidException {
//        log.info(fileSync.toString());
//        if (fileSync.getSyncType().equals(1)) {
//            throw new ValidException(ErrorMessage.BASE_FILE_DIR_NOT_CREATE);
//        } else if (fileSync.getSyncType().equals(2)) {
//            throw new ValidException(ErrorMessage.BASE_FILE_DIR_NOT_CREATE, " 测试带数据报错 ");
//        }
//
//
//        return ResultFactory.buildSuccessResult(fileSync);
//    }
//
//}
