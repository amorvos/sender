package com.sender.client.generator;

import com.sender.client.container.Scale;

public interface IdGenerator {

    /**
     *
     * @param scale 进制单位
     * @return 号码
     */
    String generatorId(Scale scale);

}
