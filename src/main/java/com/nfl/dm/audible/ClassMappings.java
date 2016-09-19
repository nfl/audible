package com.nfl.dm.audible;

import com.nfl.dm.audible.annotation.PostProcessor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class ClassMappings {

    private Class toClass;

    private Class mappingClass;

    private Map<String, Map<String, Function>> embeddedMappingCache;
    private Map<Class, Map<String, Map<String, Function>>> embeddedMappingRaw;
    private Map<String, Map<String, Function>> topMappingCache;
    private Map<Class, Map<String, Map<String, Function>>> topMappingRaw;
    private Map<String, List<Method>> postProcessors;

    public ClassMappings(Class toClass) {
        embeddedMappingCache = new HashMap<>();
        embeddedMappingRaw = new HashMap<>();
        topMappingCache = new HashMap<>();
        topMappingRaw = new HashMap<>();
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
        switch(type) {
            case EMBEDDED:
                embeddedMappingCache.put(key, functionMapping);
                embeddedMappingRaw.put(originalClass, createNamedMapping(mappingName, functionMapping));
                break;
            case NORMAL:
                topMappingCache.put(key, functionMapping);
                topMappingRaw.put(originalClass, createNamedMapping(mappingName, functionMapping));
                break;
        }
    }

    private Map<String, Map<String, Function>> createNamedMapping(String mappingName, Map<String, Function> functionMapping) {
        Map<String, Map<String, Function>> namedMapping = new HashMap<>();
        namedMapping.put(mappingName, functionMapping);
        return namedMapping;
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
        return topMappingCache;
    }

    public Map<String, Function> getMapping(Class originalClass, String mappingName, MappingType type) {

        Map<String, Function> requestedMapping = null;

        String key = originalClass + "#" + mappingName;

        switch (type) {
            case EMBEDDED:
                if(embeddedMappingCache.containsKey(key)) {
                    requestedMapping = embeddedMappingCache.get(key);
                } else if (topMappingCache.containsKey(key)) {
                    requestedMapping = topMappingCache.get(key);
                } else {
                    Map<String, Function> byAssignableEmbeddable = getByAssignable(embeddedMappingCache, embeddedMappingRaw, originalClass, mappingName);
                    return byAssignableEmbeddable != null
                            ? byAssignableEmbeddable
                            : getByAssignable(topMappingCache, topMappingRaw, originalClass, mappingName);
                }
                break;
            case NORMAL:
                if(topMappingCache.containsKey(key)) {
                    requestedMapping = topMappingCache.get(key);
                } else if (embeddedMappingCache.containsKey(key)) {
                    requestedMapping = embeddedMappingCache.get(key);
                } else {
                    Map<String, Function> byAssignableTop = getByAssignable(topMappingCache, topMappingRaw, originalClass, mappingName);
                    return byAssignableTop != null
                            ? byAssignableTop
                            : getByAssignable(embeddedMappingCache, embeddedMappingRaw, originalClass, mappingName);
                }
                break;
        }

        if (requestedMapping == null) {
            throw new RuntimeException(type + " mapping not found from " + originalClass.getName() + " to " + toClass.getName() + " named " + mappingName);
        }

        return requestedMapping;

    }

    private Map<String, Function> getByAssignable(Map<String, Map<String, Function>> cache, Map<Class, Map<String, Map<String, Function>>> rawMap, Class clazz, String name) {

        Set<Class> classes = rawMap.keySet();
        Optional<Class> candidate = classes.stream().findFirst().filter(it -> it.isAssignableFrom(clazz));

        if (candidate.isPresent()) {
            Class candidateClass = candidate.get();
            Map<String, Map<String, Function>> namedFunctionMap = rawMap.get(candidateClass);

            Map<String, Function> functionMap = namedFunctionMap.get(name);
            if (functionMap != null) {
                String key = clazz + "#" + name;
                cache.put(key, functionMap);

                return functionMap;

            }

        }

        return null;
    }

}
