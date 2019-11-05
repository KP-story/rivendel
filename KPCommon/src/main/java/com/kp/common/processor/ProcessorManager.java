package com.kp.common.processor;

import org.reflections.Reflections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kukubutukandy on 30/05/2017.
 */
public class ProcessorManager {
    String[] pakages;
    private Map<String, Processor> processorMap = new ConcurrentHashMap<>();
    private Processor defaultPs;

    public ProcessorManager(String... pakages) {
        this.pakages = pakages;
    }

    public void init(Object... paramInit) throws IllegalAccessException, Exception, InstantiationException {
        Reflections reflections = new Reflections(pakages);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Command.class);
        for (Class<?> entry : annotated) {
            Processor processor = (Processor) entry.newInstance();
            processor.init(paramInit);
            Command command = entry.getAnnotation(Command.class);
            if (!command.isCommandDefault()) {
                processorMap.put(command.command(), processor);
            } else {
                defaultPs = processor;
            }
        }
    }

    public boolean containsProcesser(String command) {
        return processorMap.containsKey(command);

    }

    public Processor getProcessorOrDefault(String command) {
        if (processorMap.containsKey(command))

            return processorMap.get(command);

        else {
            return defaultPs;
        }
    }

    public Processor getProcessor(String command) {

        return processorMap.get(command);

    }

}
