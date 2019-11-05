package com.kp.configuration;

import com.kp.common.data.vo.VObject;

public interface ConfigStorage {
    void init(VObject config);

    VObject load() throws Exception;

    void storeConfig(VObject vObject) throws Exception;

    String define(VObject vObject);


}
