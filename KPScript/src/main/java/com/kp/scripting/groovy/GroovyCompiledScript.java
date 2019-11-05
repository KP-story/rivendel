package com.kp.scripting.groovy;

import com.kp.scripting.CompiledScript;
import com.kp.scripting.exception.ScriptRuntimeException;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.Map;
import java.util.Map.Entry;


public class GroovyCompiledScript implements CompiledScript {

    private final javax.script.CompiledScript compiledObject;

    public GroovyCompiledScript(javax.script.CompiledScript compiled) {
        this.compiledObject = compiled;
    }

    @Override
    public Object run(Map<String, Object> arguments) {
        ScriptContext context = new SimpleScriptContext();
        for (Entry<String, Object> entry : arguments.entrySet()) {
            context.setAttribute(entry.getKey(), entry.getValue(), ScriptContext.ENGINE_SCOPE);
        }
        try {
            return this.compiledObject.eval(context);
        } catch (ScriptException e) {
            throw new ScriptRuntimeException(e);
        }
    }

}
