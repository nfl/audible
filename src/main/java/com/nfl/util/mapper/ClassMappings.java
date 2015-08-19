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

    private Map<String, Map<String, Function>> minMapping;
    private Map<String, Map<String, Function>> fullMapping;
    private Map<String, Map<String, Function>> additionalMapping;
    private Map<String, Map<String, Function>> fullAutoMapping;
    private Map<String, List<Method>> postProcessors;

    private Map<String, Boolean> parallelProcessCollections;

    public ClassMappings(Class toClass) {
        minMapping = new HashMap<>();
        fullMapping = new HashMap<>();
        additionalMapping = new HashMap<>();
        fullAutoMapping = new HashMap<>();
        parallelProcessCollections = new HashMap<>();
        postProcessors = new HashMap<>();
        this.toClass = toClass;
    }

    public Class getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
    }

    public boolean isParallel(Class originalClass, String mappingName, MappingType type) {
        String key = originalClass + "#" + mappingName + "#" + type;
        return parallelProcessCollections.get(key) != null ? parallelProcessCollections.get(key) : false;
    }

    public void addMapping(Class originalClass, String mappingName, MappingType type, Map<String, Function> functionMapping, boolean parallelCollections) {
        String key = originalClass + "#" + mappingName;
        String parallelKey = key + "#" + type;
        this.parallelProcessCollections.put(parallelKey, parallelCollections);
        switch(type) {
            case MIN:
                minMapping.put(key, functionMapping);
                break;
            case FULL:
                fullMapping.put(key, functionMapping);
                break;
            case ADDITIONAL:
                additionalMapping.put(key, functionMapping);
                break;
            case FULL_AUTO:
                fullAutoMapping.put(key, functionMapping);
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
        return fullMapping;
    }

    public Map<String, Function> getMapping(Class originalClass, String mappingName, MappingType type) {

        Map<String, Function> requestedMapping = null;

        String key = originalClass + "#" + mappingName;

        switch (type) {
            case MIN:
                if(minMapping.containsKey(key)) {
                    requestedMapping = minMapping.get(key);
                } else if (fullMapping.containsKey(key)) {
                    requestedMapping = fullMapping.get(key);
                } else if (fullAutoMapping.containsKey(key)) {
                    requestedMapping = fullAutoMapping.get(key);
                } else {
                    requestedMapping = null;
                }
                break;
            case FULL:
                if(fullMapping.containsKey(key)) {
                    requestedMapping = fullMapping.get(key);
                } else if (minMapping.containsKey(key) && additionalMapping.containsKey(key)) {
                    requestedMapping = new HashMap<>();
                    requestedMapping.putAll(minMapping.get(key));
                    requestedMapping.putAll(additionalMapping.get(key));
                } else if (fullAutoMapping.containsKey(key)) {
                    requestedMapping = fullAutoMapping.get(key);
                } else {
                    requestedMapping = null;
                }
                break;
            case ADDITIONAL:
                requestedMapping = additionalMapping.get(key);
                break;
            case FULL_AUTO:
                if(fullAutoMapping.containsKey(key)) {
                    requestedMapping = fullAutoMapping.get(key);
                } else if(fullMapping.containsKey(key)) {
                    requestedMapping = fullMapping.get(key);
                } else if (minMapping.containsKey(key) && additionalMapping.containsKey(key)) {
                    requestedMapping = new HashMap<>();
                    requestedMapping.putAll(minMapping.get(key));
                    requestedMapping.putAll(additionalMapping.get(key));
                } else if (fullAutoMapping.containsKey(key)) {
                    requestedMapping = fullAutoMapping.get(key);
                } else {
                    requestedMapping = null;
                }
                break;
        }

        if (requestedMapping == null) {
            throw new RuntimeException(type + " mapping not found from " + originalClass.getName() + " to " + toClass.getName() + " named " + mappingName);
        }

        return requestedMapping;

    }

}
