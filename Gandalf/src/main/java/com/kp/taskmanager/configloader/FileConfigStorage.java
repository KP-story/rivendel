package com.kp.taskmanager.configloader;

import com.kp.common.constant.FieldConstant;
import com.kp.common.data.vo.VArray;
import com.kp.common.data.vo.VObject;
import com.kp.common.log.Loggable;
import com.kp.configuration.ConfigStorage;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class FileConfigStorage implements ConfigStorage, Loggable {
    String folder;

    @Override
    public void init(VObject config) {
        folder = config.getString(FieldConstant.FOLDER);
    }

    @Override
    public VObject load() throws Exception {
        VObject config = new VObject();
        VArray threadInfos = new VArray();
        File fl = new File(this.folder);
        if (fl.exists()) {
            File[] configFiles = fl.listFiles();
            getLogger().info("loading  {} thread configs", configFiles.length);
            if (configFiles.length > 0) {
                for (int var8 = 0; var8 < configFiles.length; ++var8) {
                    File cf = configFiles[var8];
                    if (!cf.isDirectory()) {
                        try {
                            String content = FileUtils.readFileToString(cf);
                            VObject vObject = VObject.fromJSON(content);
                            threadInfos.add(vObject);
                        } catch (Exception e) {
                            getLogger().error(" load failed thread {} configs error", cf.getName(), e);

                        }


                    }
                }
            }
        }
        config.put(FieldConstant.THREADS, threadInfos);
        return config;
    }

    @Override
    public void storeConfig(VObject vObject) throws Exception {

    }

    @Override
    public String define(VObject vObject) {
        return null;
    }
}
