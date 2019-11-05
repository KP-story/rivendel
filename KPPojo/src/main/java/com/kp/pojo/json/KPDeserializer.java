package com.kp.pojo.json;


import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public interface KPDeserializer {
    Object from(JsonParser data) throws IOException;

}
