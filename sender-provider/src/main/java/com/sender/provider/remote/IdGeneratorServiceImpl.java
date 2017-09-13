package com.sender.provider.remote;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sender.client.container.Scale;
import com.sender.client.generator.dubbo.IdGeneratorService;
import com.sender.provider.biz.IdGeneratorBiz;

/**
 * @author haibo Date: 17-9-7 Time: 上午11:48
 */
@Component("idGeneratorService")
public class IdGeneratorServiceImpl implements IdGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGeneratorServiceImpl.class);

    @Resource
    private IdGeneratorBiz idGeneratorBiz;

    public String generatorId(Scale scale) {
        try {
            long id = idGeneratorBiz.getGenerator().generator();
            if (scale == Scale.HEXADECIMAL) {
                return Long.toHexString(id).toUpperCase();
            }
            return Long.toString(id);
        } catch (Exception e) {
            LOGGER.error("ID构建失败", e);
            return StringUtils.EMPTY;
        }
    }
}