package com.blog.file.service.impl;


/**
 * @Description 传感器模板服务实现类
 * @Author lxk
 * @CreateTime 2024-09-29
 */

@Service
public class SensorTemplateServiceImpl implements SensorTemplateService {

    @Resource
    private SensorTemplateDAO sensorTemplateDAO;

    @Resource
    private ChipDAO chipDAO;

    @Resource
    private SensorDAO sensorDAO;

    @Override
    public List<SensorTemplateVO> selectSensorTemplateByChipOrSensorId(Integer userId, SensorTemplateDTO sensorTemplateDTO) throws ValidException {
        LambdaQueryWrapper<SensorTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Set<String> sensorTypeSet;
        if (sensorTemplateDTO.getChipId() != null) {
            Chip chip = chipDAO.selectById(sensorTemplateDTO.getChipId());
            sensorTypeSet = sensorDAO.selectList(new LambdaQueryWrapper<Sensor>().eq(Sensor::getDeviceCode, chip.getDeviceCode())
                    .eq(Sensor::getChipCode, chip.getChipCode())).stream().map(Sensor::getSensorType).collect(Collectors.toSet());
        } else if (sensorTemplateDTO.getSensorId() != null) {
            sensorTypeSet = Stream.of(sensorDAO.selectById(sensorTemplateDTO.getSensorId())).map(Sensor::getSensorType).collect(Collectors.toSet());
        } else {
            throw new ValidException("单片机id与传感器id不能同时为空");
        }

        List<SensorTemplate> sensorTemplateList = sensorTemplateDAO.selectList(lambdaQueryWrapper.eq(SensorTemplate::getUserId, userId).in(SensorTemplate::getSensorType, sensorTypeSet));

        List<SensorTemplateVO> sensorTemplateVOList = new ArrayList<>();
        sensorTemplateList.forEach(item -> {
            SensorTemplateVO sensorTemplateVO = new SensorTemplateVO();
            BeanUtils.copyProperties(item, sensorTemplateVO);
            sensorTemplateVOList.add(sensorTemplateVO);
        });

        return sensorTemplateVOList;
    }

}
