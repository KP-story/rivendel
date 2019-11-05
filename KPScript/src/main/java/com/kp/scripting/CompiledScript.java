package com.kp.scripting;

import java.util.Map;

public interface CompiledScript {

    Object run(Map<String, Object> arguments);
}
