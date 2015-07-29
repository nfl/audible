package com.nfl.util.mapper.service;

import com.nfl.util.mapper.CustomMappingObject;
import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.Tuple;
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
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Function;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

        return list.parallelStream().map(o -> doTransform(toClass, MappingType.FULL, o)).collect(Collectors.toList());

    }


    public <From, To> List<To> transformList(Class<To> toClass, Collection<From> list, MappingType mappingType) {

        return list.parallelStream().map(o -> doTransform(toClass, mappingType, o)).collect(Collectors.toList());
    }


    public <To> To transform(Class<To> toClass, Object... from) {
        return doTransform(toClass, MappingType.FULL, from);
    }

    public <To> To transformMin(Class<To> toClass, Object... from)  {
        return doTransform(toClass, MappingType.MIN, from);
    }

    @SuppressWarnings("unchecked")
    private <To> To doTransform(final Class<To> toClass, MappingType mappingType, Object... from) {

        try {

            if (isNull(from))
                return null;

            final To to = toClass.newInstance();

            MappingType overrideMappingType = null;
            String customMappingName = null;

            if (from[0] instanceof CustomMappingObject) {
                CustomMappingObject cmo = (CustomMappingObject) from[0];
                overrideMappingType = cmo.getMappingType();
                customMappingName = cmo.getMappingName();
            }

            from = checkForCustomMappingType(from);
            from = checkForMultipleReturnObject(from);

            MappingFunction mappingFunction = getMapping(toClass, mappingType, overrideMappingType, customMappingName, from);
            Map<String, Function> mapping = mappingFunction.mapping;
            if (mappingFunction.mappingType == MappingType.FULL_AUTO) {
                if (from.length == 1) {
                    typeSafeCopy.copyProperties(to, from[0]);
                } else {
                    log.warn("Unable to auto copy properties to " + toClass.getName() + ". FULL_AUTO mappingType can only be used when source has only 1 parameter");
                }
            }

            final Object[] finalFrom = from;


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

    private void handlePostProcessor(Object to, Class toClass, Object [] from) throws Exception {
        Object mappingObject = toClassToMapping.get(toClass);

        Class mappingClassClass = mappingObject.getClass();

        Method[] mappingMethods = mappingClassClass.getMethods();

        List<Class> classesList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        classesList.add(toClass);
        objectList.add(to);

        for (Object o : from) {
            classesList.add(o.getClass());
            objectList.add(o);
        }

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

    private Object[] checkForMultipleReturnObject(Object[] from) {
        if (from.length == 1 && from[0] instanceof Tuple) {
            return ((Tuple) from[0]).getObjects();
        } else {
            return from;
        }
    }


    private Object checkForCustomMappingTypeList(Object fromList) {
        if (fromList instanceof CustomMappingObject) {
            return ((CustomMappingObject) fromList).getObject();

        } else {
            return fromList;
        }
    }

    private Object[] checkForCustomMappingType(Object[] from) {
        if (from.length == 1 && from[0] instanceof CustomMappingObject) {

            return new Object[]{((CustomMappingObject) from[0]).getObject()};

        } else {
            return from;
        }
    }


    private MappingFunction getMapping(Class toClass, MappingType mappingType, MappingType overrideMappingType, String customMappingName, Object... from) throws Exception {

        List<Class> classesList = new ArrayList<>();

        for (Object o : from) {
            classesList.add(o.getClass());
        }

        Class[] originalClasses = new Class[classesList.size()];

        originalClasses = classesList.toArray(originalClasses);

        if (overrideMappingType != null) {
            mappingType = overrideMappingType;
        }



        return getMappingFromMappingObject(toClass, mappingType, customMappingName, originalClasses);


    }

    private MappingFunction getMappingFromMappingObject(Class toClass, MappingType mappingType, final String mappingName, final Class... originalClasses) throws Exception {

        Object mappingObject = toClassToMapping.get(toClass);

        if (mappingObject == null) {
            throw new RuntimeException("No Mapping found for " + toClass);
        }

        Class mappingClassClass = mappingObject.getClass();

        Method[] mappingMethods = mappingClassClass.getMethods();

        List<Method> mappingMethodsList = Arrays.stream(mappingMethods).filter(method -> method.isAnnotationPresent(Mapping.class) && isOriginalClassesMatch(originalClasses, method.getAnnotation(Mapping.class).originalClasses()) && isMappingNameMatch(mappingName, method.getAnnotation(Mapping.class).name()))
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

    @SuppressWarnings("unchecked")
    private boolean isOriginalClassesMatch(Class[] fromOriginalClasses, Class[] methodOriginalClasses) {
        if (fromOriginalClasses.length != methodOriginalClasses.length) {
            return false;
        }

        for (int i = 0; i < fromOriginalClasses.length; i++) {
            if (!methodOriginalClasses[i].isAssignableFrom(fromOriginalClasses[i])) {
                return false;
            }
        }

        return true;
    }

    private boolean isMappingNameMatch(String fromMappingName, String methodMappingName) {
        return fromMappingName == null || fromMappingName.equals(methodMappingName);
    }

    private Object eval(Class toPropertyClass, Function fromExpression, Object... from) throws Exception {
        Object rhs = safelyEvaluateClosure(fromExpression, from);
        return (rhs != null && isMappingPresent(toPropertyClass) && !isAlreadyProvided(toPropertyClass, rhs)) ? doTransform(toPropertyClass, MappingType.MIN, rhs) : rhs;
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyProvided(Class toPropertyClass, Object rhs) {
        return toPropertyClass.isAssignableFrom(rhs.getClass());
    }

    @SuppressWarnings("unchecked")
    private Object safelyEvaluateClosure(Function fromExpression, Object... from) {
        try {

            switch (from.length) {
                case 1:
                    return ((Func1)fromExpression).call(from[0]);
                case 2:
                    return ((Func2)fromExpression).call(from[0], from[1]);
                case 3:
                    return ((Func3)fromExpression).call(from[0], from[1], from[2]);
                default:
                    return null;
            }

        } catch (NullPointerException npe) {
            return null;
        }

    }

    private boolean isNull(Object... from) {
        if (from == null) {
            return true;
        } else if (from.length == 0) {
            return true;
        } else if (from.length == 1 && from[0] == null) {
            return true;
        } else {
            for (Object object : from) {
                if (object != null) {
                    return false;
                }

            }

            return true;
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
