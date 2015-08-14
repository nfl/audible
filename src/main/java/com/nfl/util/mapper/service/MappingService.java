package com.nfl.util.mapper.service;

import com.nfl.util.mapper.ClassMappings;
import com.nfl.util.mapper.MappingFunction;
import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import com.nfl.util.mapper.annotation.PostProcessor;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/3/15.
 */
@Service
public class MappingService implements ApplicationContextAware{

    private Map<Class, ClassMappings> cacheMap;

    // class -> object of corresponding mapping class
    private Map<Class, Object> mappingInstanceMap;

    private ApplicationContext applicationContext;

    public MappingService() {
        cacheMap = new HashMap<>();
        mappingInstanceMap = new HashMap<>();
    }

    @PostConstruct

    void initializeCacheMap() throws Exception{
        loadClasses();
    }

    void loadClasses() throws Exception {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        // Filter to include only classes that have a particular annotation.
        provider.addIncludeFilter(new AnnotationTypeFilter(MappingTo.class));
        // Find classes in the given package (or subpackages)
        Set<BeanDefinition> beans = provider.findCandidateComponents("com.nfl");

        for (BeanDefinition object : beans) {
            Class mappingClass = Class.forName(object.getBeanClassName());
            MappingTo mappingTo = (MappingTo) mappingClass.getAnnotation(MappingTo.class);
            Class toClass = mappingTo.value();
            ClassMappings value = new ClassMappings(toClass);
            value.setMappingClass(mappingClass);
            Object mappingObject = applicationContext.getBean(mappingClass);
            mappingInstanceMap.put(toClass, mappingObject);

            for(Method method: mappingClass.getMethods()) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    Mapping mapping = method.getAnnotation(Mapping.class);
                    boolean parallelCollections = mapping.parallelProcessCollections();
                    MappingType type = mapping.type();
                    Class originalClass = mapping.originalClass();
                    String name = mapping.name();
                    Map<String, Function> functionMapping = (Map<String, Function>) method.invoke(mappingObject);

                    final Object to = toClass.newInstance();
                    for (Map.Entry<String, Function> entry : functionMapping.entrySet()) {
                        try {
                            PropertyUtils.setNestedProperty(to, entry.getKey(), null);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("Invalid Mapping in " + mappingClass.getName() + "." + method.getName() + ": Cannot set field '" + entry.getKey() + "' in class " + toClass.getName());
                        }

                    }


                    value.addMapping(type, originalClass, name, functionMapping, parallelCollections);
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

    public MappingFunction getMappingFunction(Class toClass, MappingType type, Class originalClass, String name) {
        MappingFunction mappingFunction = new MappingFunction();
        mappingFunction.setMapping(cacheMap.get(toClass).getMapping(type, originalClass, name));
        mappingFunction.setMappingType(type);
        mappingFunction.setParallelCollections(cacheMap.get(toClass).isParallel(type, originalClass, name));

        return mappingFunction;
    }

    public List<Method> getPostProcessors(Class toClass, Class originalClass, String name) {
        return cacheMap.get(toClass).getPostProcessors(originalClass, name);
    }
}
