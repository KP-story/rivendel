package com.kp.scripting.groovy;

import com.kp.scripting.CompiledScript;
import com.kp.scripting.Script;
import com.kp.scripting.ScriptCompiler;
import com.kp.scripting.exception.ScriptCompileException;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class GroovyScriptCompiler implements ScriptCompiler {

    private static final ScriptEngine groovyScriptEngine = SCRIPT_ENGINE_MANAGER.getEngineByName("groovy");

    private static Compilable compilableEngine = (Compilable) groovyScriptEngine;

    @Override
    public CompiledScript compile(Script script) {
        try {
            javax.script.CompiledScript compiled = compilableEngine.compile(script.getContent());
            return new GroovyCompiledScript(compiled);
        } catch (ScriptException e) {
            throw new ScriptCompileException(e);
        }
    }

}
