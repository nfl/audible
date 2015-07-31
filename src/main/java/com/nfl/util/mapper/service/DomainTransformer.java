package com.nfl.util.mapper.service;

import com.nfl.util.mapper.CustomMappingObject;
import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import com.nfl.util.mapper.annotation.PostProcessor;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * DomainTransformer uses Map of String, Function to transform one pojo to another
 */
@SuppressWarnings("unused")
@Component
public class DomainTransformer implements ApplicationContextAware {

    private static final Log log = LogFactory.getLog(DomainTransformer.class);

    private ApplicationContext applicationContext;

    private Map<Class, Object> toClassToMapping = new HashMap<>();

    private TypeSafeCopy typeSafeCopy = new TypeSafeCopy();


    public <From, To> List<To> transformList(Class<To> toClass, Collection<From> list)  {

        return this.transformList(toClass, list, MappingType.FULL, "");
    }


    public <From, To> List<To> transformList(Class<To> toClass, Collection<From> list, MappingType mappingType) {

        return this.transformList(toClass, list, mappingType, "");
    }

    public <From, To> List<To> transformList(Class<To> toClass, Collection<From> list, String mappingName) {
        return this.transformList(toClass, list, MappingType.FULL, mappingName);
    }

    public <From, To> List<To> transformList(Class<To> toClass, Collection<From> list, MappingType mappingType, String mappingName) {
        return list.parallelStream().map(o -> doTransform(toClass, mappingType, mappingName, o)).collect(Collectors.toList());
    }


    public <From, To> To transform(Class<To> toClass, From from) {
        return this.transform(toClass, from, "", MappingType.FULL);
    }

    public <From, To> To transform(Class<To> toClass, From from, MappingType mappingType) {
        return this.transform(toClass, from, "", mappingType);
    }

    public <From, To> To transform(Class<To> toClass, From from, String mappingName) {
        return this.transform(toClass, from, mappingName, MappingType.FULL);
    }

    public <From, To> To transform(Class<To> toClass, From from, String mappingName, MappingType mappingType) {
        return this.doTransform(toClass,mappingType, mappingName, from);
    }


    @SuppressWarnings("unchecked")
    private <From, To> To doTransform(final Class<To> toClass, MappingType mappingType, String mappingName, From from) {//Object... from) {

        try {

            if (from == null) return null;

            final To to = toClass.newInstance();

            MappingType overrideMappingType = null;
            String customMappingName = mappingName;

            if (from instanceof CustomMappingObject) {
                CustomMappingObject cmo = (CustomMappingObject) from;
                overrideMappingType = cmo.getMappingType();
                customMappingName = cmo.getMappingName();
            }

            //from = checkForCustomMappingType(from);
            //from = checkForMultipleReturnObject(from);

            MappingFunction mappingFunction = getMapping(toClass, mappingType, overrideMappingType, customMappingName, from);
            Map<String, Function> mapping = mappingFunction.mapping;
            if (mappingFunction.mappingType == MappingType.FULL_AUTO) {
                typeSafeCopy.copyProperties(to, from);
            }

            final Object finalFrom = from;


            assert mapping != null;

            boolean parallel = mappingFunction.parallel;

            (parallel ? mapping.entrySet().parallelStream() :mapping.entrySet().stream()).forEach(entry ->
                    {
                        String toPropertyName = entry.getKey();
                        Function fromExpression = entry.getValue();
                        try {

                            Class toPropertyType = PropertyUtils.getPropertyType(to, toPropertyName);

                            //If the field is collection
                            if (Collection.class.isAssignableFrom(toPropertyType)) {

                                Field field = toClass.getDeclaredField(toPropertyName);

                                Type type = field.getGenericType();

                                if (type instanceof ParameterizedType) {
                                    ParameterizedType pt = (ParameterizedType) type;
                                    Class typeForCollection = (Class) pt.getActualTypeArguments()[0];

                                    Object object = eval(toPropertyType, fromExpression, finalFrom);

                                    List fromList = null;

                                    MappingType overrideMappingTypeNested = null;

                                    if (object != null) {
                                        if (object instanceof CustomMappingObject) {
                                            CustomMappingObject cmo = (CustomMappingObject) object;
                                            overrideMappingTypeNested = cmo.getMappingType();
                                        } else {

                                            fromList = new ArrayList((Collection) object);
                                        }

                                        fromList = (List) checkForCustomMappingTypeList(fromList);
                                    }

                                    //Check to see if they are already providing toClass in the closure
                                    boolean isOverride = fromList != null && !fromList.isEmpty() && !typeForCollection.isAssignableFrom(fromList.iterator().next().getClass());

                                    if (fromList == null || fromList.isEmpty()) {
                                        PropertyUtils.setProperty(to, toPropertyName, null);
                                    } else if (isOverride && isMappingPresent(typeForCollection)) {

                                        Object toValue = transformList(typeForCollection, fromList, overrideMappingTypeNested != null ? overrideMappingTypeNested : MappingType.MIN);

                                        PropertyUtils.setProperty(to, toPropertyName, toValue);


                                    } else {
                                        if (toPropertyType.isAssignableFrom(fromList.getClass())) {

                                            PropertyUtils.setProperty(to, toPropertyName, fromList);

                                        } else {
                                            if (List.class.isAssignableFrom(toPropertyType)) {
                                                List list = new ArrayList();
                                                list.addAll(fromList);
                                                PropertyUtils.setProperty(to, toPropertyName, list);

                                            } else if (Set.class.isAssignableFrom(toPropertyType)) {
                                                Set set = new HashSet();
                                                set.addAll(fromList);
                                                PropertyUtils.setProperty(to, toPropertyName, set);
                                            } else {
                                                throw new Exception("Unable to find find property collection type on " + toPropertyType);
                                            }


                                        }


                                    }


                                }

                            } else {

                                Object value = eval(toPropertyType, fromExpression, finalFrom);

                                PropertyUtils.setNestedProperty(to, toPropertyName, value);
                            }

                        } catch (Exception e) {
                            log.warn("unable to set " + toPropertyName + " on " + toClass, e);
                        }

                    }
            );

            handlePostProcessor(to, toClass, from);


            return to;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handlePostProcessor(Object to, Class toClass, Object from) throws Exception {
        Object mappingObject = toClassToMapping.get(toClass);

        Class mappingClassClass = mappingObject.getClass();

        Method[] mappingMethods = mappingClassClass.getMethods();

        List<Class> classesList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        classesList.add(toClass);
        objectList.add(to);

        classesList.add(from.getClass());
        objectList.add(from);

        Class[] argumentClasses = new Class[classesList.size()];
        Object[] objectArray = new Object[objectList.size()];

        final Class[] finalArgumentClasses = classesList.toArray(argumentClasses);
        objectArray = objectList.toArray(objectArray);

        List<Method> methodsList = Arrays.stream(mappingMethods).filter(method -> method.isAnnotationPresent(PostProcessor.class) && Arrays.equals(method.getParameterTypes(),finalArgumentClasses))
                .collect(Collectors.toList());

        for (Method method : methodsList) {
            method.invoke(mappingObject, objectArray);
        }

    }


    private Object checkForCustomMappingTypeList(Object fromList) {
        if (fromList instanceof CustomMappingObject) {
            return ((CustomMappingObject) fromList).getObject();

        } else {
            return fromList;
        }
    }

    private Object checkForCustomMappingType(Object from) {
        if (from instanceof CustomMappingObject) {

            return ((CustomMappingObject) from).getObject();

        } else {
            return from;
        }
    }


    private <From> MappingFunction getMapping(Class toClass, MappingType mappingType, MappingType overrideMappingType, String customMappingName, From from) throws Exception {

        Class originalClass = from.getClass();

        if (overrideMappingType != null) {
            mappingType = overrideMappingType;
        }



        return getMappingFromMappingObject(toClass, mappingType, customMappingName, originalClass);


    }

    private MappingFunction getMappingFromMappingObject(Class toClass, MappingType mappingType, final String mappingName, final Class originalClass) throws Exception {

        Object mappingObject = toClassToMapping.get(toClass);

        if (mappingObject == null) {
            throw new RuntimeException("No Mapping found for " + toClass);
        }

        Class mappingClassClass = mappingObject.getClass();

        Method[] mappingMethods = mappingClassClass.getMethods();

        List<Method> mappingMethodsList = Arrays.stream(mappingMethods).filter(method ->
                        method.isAnnotationPresent(Mapping.class)
                        && method.getAnnotation(Mapping.class).originalClass().isAssignableFrom(originalClass)
                        && (mappingName == null || mappingName.equals(method.getAnnotation(Mapping.class).name())))
                .collect(Collectors.toList());

        if (mappingMethodsList.size() == 0) {
            throw new RuntimeException("No Mapping Found from " + mappingClassClass);
        } else {
            MappingFunction mapping = getMappingByType(mappingMethodsList, mappingType, mappingObject);
            mapping.parallel = mappingMethodsList.stream().allMatch(method -> method.getAnnotation(Mapping.class).parallel());
            return mapping;
        }

    }


    private MappingFunction getMappingByType(List<Method> mappingMethodsList, MappingType mappingType, Object mappingObject) throws Exception {

        MappingFunction mappingFunction = new MappingFunction();

        Map<String, Function> mapping;

        if (MappingType.MIN.equals(mappingType)) {
            mapping = getMappingFromMethods(MappingType.MIN, mappingMethodsList, mappingObject);

            if (mapping == null) {
                Map<String, Function> fullMapping = getMappingFromMethods(MappingType.FULL, mappingMethodsList, mappingObject);
                if (fullMapping == null) {
                    mappingFunction.mappingType = MappingType.FULL_AUTO;
                    mappingFunction.mapping = getMappingFromMethods(MappingType.FULL_AUTO, mappingMethodsList, mappingObject);
                } else {
                    mappingFunction.mappingType = MappingType.FULL;
                    mappingFunction.mapping = fullMapping;
                }

            } else {
                mappingFunction.mappingType = MappingType.MIN;
                mappingFunction.mapping = mapping;
            }

        } else {
            mapping = getMappingFromMethods(MappingType.MIN, mappingMethodsList, mappingObject);

            if (mapping != null) {
                Map<String, Function> mappingFromMethods = getMappingFromMethods(MappingType.ADDITIONAL, mappingMethodsList, mappingObject);
                if (mappingFromMethods != null) {
                    mapping.putAll(mappingFromMethods);
                }

                mappingFunction.mappingType = MappingType.FULL;
                mappingFunction.mapping = mapping;
            } else {
                Map<String, Function> fullMapping = getMappingFromMethods(MappingType.FULL, mappingMethodsList, mappingObject);
                if (fullMapping == null) {
                    mappingFunction.mappingType = MappingType.FULL_AUTO;
                    mappingFunction.mapping = getMappingFromMethods(MappingType.FULL_AUTO, mappingMethodsList, mappingObject);
                } else {
                    mappingFunction.mappingType = MappingType.FULL;
                    mappingFunction.mapping = fullMapping;
                }
            }

        }

        return mappingFunction;
    }

    private class MappingFunction {
        MappingType mappingType;
        Map<String, Function> mapping;
        boolean parallel;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Function> getMappingFromMethods(MappingType type, List<Method> mappingMethodsList, Object mappingObject) throws Exception {
        for (Method method : mappingMethodsList) {
            if (type.equals(method.getAnnotation(Mapping.class).value())) {
                return ((Map<String, Function>) (method.invoke(mappingObject, (Object[]) null)));
            }

        }

        return null;
    }

    private <From> Object eval(Class toPropertyClass, Function fromExpression, From from) throws Exception {
        Object rhs = safelyEvaluateClosure(fromExpression, from);
        return (rhs != null && isMappingPresent(toPropertyClass) && !isAlreadyProvided(toPropertyClass, rhs)) ? doTransform(toPropertyClass, MappingType.MIN, "", rhs) : rhs;
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyProvided(Class toPropertyClass, Object rhs) {
        return toPropertyClass.isAssignableFrom(rhs.getClass());
    }

    @SuppressWarnings("unchecked")
    private <From> Object safelyEvaluateClosure(Function fromExpression, From from) {
        try {

            return fromExpression.apply(from);

        } catch (NullPointerException npe) {
            return null;
        }

    }

    private boolean isMappingPresent(Class toClass) {
        return toClassToMapping.containsKey(toClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    @PostConstruct
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

            toClassToMapping.put(toClass, applicationContext.getBean(mappingClass));

        }

    }


}
