package com.nfl.util.mapper;

import com.nfl.util.mapper.annotation.PostProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/3/15.
 */
public class ClassMappings {

    private Class toClass;

    private Class mappingClass;

    private Map<String, Map<String, Function>> embeddedMapping;
    private Map<String, Map<String, Function>> topMapping;
    private Map<String, List<Method>> postProcessors;

    public ClassMappings(Class toClass) {
        embeddedMapping = new HashMap<>();
        topMapping = new HashMap<>();

        postProcessors = new HashMap<>();
        this.toClass = toClass;
    }

    public Class getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
    }


    public void addMapping(Class originalClass, String mappingName, MappingType type, Map<String, Function> functionMapping) {
        String key = originalClass + "#" + mappingName;
        String parallelKey = key + "#" + type;
        switch(type) {
            case EMBEDDED:
                embeddedMapping.put(key, functionMapping);
                break;
            case NORMAL:
                topMapping.put(key, functionMapping);
                break;
        }
    }

    public void addPostProcessors(Method method, PostProcessor postProcessor) {
        String key = postProcessor.originalClass() + "#" + postProcessor.mappingName();
        if (postProcessors.containsKey(key)) {
            postProcessors.get(key).add(method);
        } else {
            List<Method> methods = new ArrayList<>();
            methods.add(method);
            postProcessors.put(key, methods);
        }
    }

    public List<Method> getPostProcessors(Class originalClass, String mappingName) {
        String key = originalClass + "#" + mappingName;
        return postProcessors.get(key);
    }

    public Map<String, Map<String, Function>> getFull() {
        return topMapping;
    }

    public Map<String, Function> getMapping(Class originalClass, String mappingName, MappingType type) {

        Map<String, Function> requestedMapping = null;

        String key = originalClass + "#" + mappingName;

        switch (type) {
            case EMBEDDED:
                if(embeddedMapping.containsKey(key)) {
                    requestedMapping = embeddedMapping.get(key);
                } else if (topMapping.containsKey(key)) {
                    requestedMapping = topMapping.get(key);
                }
                break;
            case NORMAL:
                if(topMapping.containsKey(key)) {
                    requestedMapping = topMapping.get(key);
                } else if (embeddedMapping.containsKey(key)) {
                    requestedMapping = embeddedMapping.get(key);
                }
                break;
        }

        if (requestedMapping == null) {
            throw new RuntimeException(type + " mapping not found from " + originalClass.getName() + " to " + toClass.getName() + " named " + mappingName);
        }

        return requestedMapping;

    }

}
