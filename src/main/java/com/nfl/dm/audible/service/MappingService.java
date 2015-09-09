package com.nfl.dm.audible.service;

import com.nfl.dm.audible.ClassMappings;
import com.nfl.dm.audible.MappingType;
import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.MappingFunction;
import com.nfl.dm.audible.annotation.PostProcessor;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/3/15.
 */
@Service
public class MappingService implements ApplicationContextAware {

    private Map<Class, ClassMappings> cacheMap = new HashMap<>();

    // class -> object of corresponding mapping class
    private Map<Class, Object> mappingInstanceMap = new HashMap<>();

    private ApplicationContext applicationContext;


    @PostConstruct
    void loadClasses() throws Exception {
        Map<String,Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(MappingTo.class);

        for (Object object : beansWithAnnotation.values()) {
            Class mappingClass = object.getClass();
            MappingTo mappingTo = (MappingTo) mappingClass.getAnnotation(MappingTo.class);
            Class toClass = mappingTo.value();
            ClassMappings value = new ClassMappings(toClass);
            value.setMappingClass(mappingClass);

            mappingInstanceMap.put(toClass, object);

            for (Method method : mappingClass.getMethods()) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    Mapping mapping = method.getAnnotation(Mapping.class);

                    MappingType type = mapping.type();
                    Class originalClass = mapping.originalClass();
                    String name = mapping.name();
                    Map<String, Function> functionMapping = (Map<String, Function>) method.invoke(object);

                    final Object to = toClass.newInstance();
                    for (Map.Entry<String, Function> entry : functionMapping.entrySet()) {
                        try {
                            PropertyUtils.setNestedProperty(to, entry.getKey(), null);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("Invalid Mapping in " + mappingClass.getName() + "." + method.getName() + ": Cannot set field '" + entry.getKey() + "' in class " + toClass.getName());
                        } catch (IllegalArgumentException e) {
                            //Do nothing setting nulls on primitives //TODO: Fix
                        }

                    }

                    value.addMapping(originalClass, name, type, functionMapping);
                } else if (method.isAnnotationPresent(PostProcessor.class)) {
                    PostProcessor postProcessor = method.getAnnotation(PostProcessor.class);

                    if (!method.getReturnType().equals(Void.TYPE)) {
                        throw new RuntimeException("Post Processor's return type must be void");
                    }

                    Class<?>[] parameterTypes = method.getParameterTypes();

                    if (parameterTypes.length != 2) {
                        throw new RuntimeException("Post Processor must have 2 parameters of type toClass, fromClass");
                    }

                    if (!parameterTypes[0].equals(toClass) || !parameterTypes[1].equals(postProcessor.originalClass())) {
                        throw new RuntimeException("Post Processor originalClass mismatches the parameter types");
                    }

                    value.addPostProcessors(method, postProcessor);
                }
            }


            cacheMap.put(toClass, value);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    public boolean hasMappingForClass(Class toClass) {
        return cacheMap.containsKey(toClass);
    }

    public Object getMappingClassObject(Class toClass) {
        return mappingInstanceMap.get(toClass);
    }

    public MappingFunction getMappingFunction(Class toClass, Class originalClass, String name, MappingType type) {

        MappingFunction mappingFunction = new MappingFunction();

        ClassMappings classMappings = cacheMap.get(toClass);

        if (classMappings == null) {
            //TODO setMapping to empty map so that Orika automap still works
            throw new RuntimeException("No Mapping found for type : " + toClass.getName());
        }

        mappingFunction.setMapping(classMappings.getMapping(originalClass, name, type));
        mappingFunction.setMappingType(type);


        return mappingFunction;
    }

    public List<Method> getPostProcessors(Class toClass, Class originalClass, String name) {
        return cacheMap.get(toClass).getPostProcessors(originalClass, name);
    }
}
