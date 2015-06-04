package com.nfl.util.mapper.service;

import com.nfl.util.mapper.CustomMappingObject;
import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.MultipleReturnObject;
import com.nfl.util.mapper.annotation.MappedClass;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingClass;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DomainTransformer uses Map<String, Function> to transform one pojo to another
 */
@SuppressWarnings("unused")
@Component
public class DomainTransformer implements ApplicationContextAware {

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

            Map<String, Function> mapping = getMapping(toClass, mappingType, overrideMappingType, customMappingName, from);

            final Object[] finalFrom = from;


            assert mapping != null;
            mapping.entrySet().parallelStream().forEach(entry ->
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

                                    List fromList = new ArrayList((Collection)eval(toPropertyType, fromExpression, finalFrom));

                                    MappingType overrideMappingTypeNested = null;

                                    if (fromList instanceof CustomMappingObject) {
                                        CustomMappingObject cmo = (CustomMappingObject) fromList;
                                        overrideMappingTypeNested = cmo.getMappingType();
                                    }

                                    fromList = (List) checkForCustomMappingTypeList(fromList);

                                    //Check to see if they are already providing toClass in the closure
                                    boolean isOverride = fromList != null && !fromList.isEmpty() && !typeForCollection.isAssignableFrom(fromList.iterator().next().getClass());

                                    if (fromList == null || fromList.isEmpty()) {
                                        PropertyUtils.setProperty(to, toPropertyName, null);
                                    } else if (isOverride && typeForCollection.isAnnotationPresent(MappedClass.class)) {

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

            return to;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object[] checkForMultipleReturnObject(Object[] from) {
        if (from.length == 1 && from[0] instanceof MultipleReturnObject) {
            return ((MultipleReturnObject) from[0]).getObjects();
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


    private Map<String, Function> getMapping(Class toClass, MappingType mappingType, MappingType overrideMappingType, String customMappingName, Object... from) throws Exception {

        List<Class> classesList = new ArrayList<>();

        for (Object o : from) {
            classesList.add(o.getClass());
        }

        Class[] originalClasses = new Class[classesList.size()];

        originalClasses = classesList.toArray(originalClasses);

        if (overrideMappingType != null) {
            mappingType = overrideMappingType;
        }

        if (toClass.isAnnotationPresent(MappingClass.class)) {
            MappingClass mappingClass = (MappingClass) toClass.getAnnotation(MappingClass.class);

            String className = mappingClass.value();

            Class mappingClassClass = Class.forName(className);

            return getMappingFromMappingObject(mappingClassClass, mappingType, customMappingName, originalClasses);

        } else {
            return null;
        }
    }

    private Map<String, Function> getMappingFromMappingObject(Class mappingClassClass, MappingType mappingType, final String mappingName, final Class... originalClasses) throws Exception {

        Object mappingObject = applicationContext.getBean(mappingClassClass);

        Method[] mappingMethods = mappingClassClass.getMethods();

        List<Method> mappingMethodsList = Arrays.stream(mappingMethods).filter(method -> method.isAnnotationPresent(Mapping.class) && isOriginalClassesMatch(originalClasses, method.getAnnotation(Mapping.class).originalClasses()) && isMappingNameMatch(mappingName, method.getAnnotation(Mapping.class).name()))
                .collect(Collectors.toList());

        if (mappingMethodsList.size() == 0) {
            throw new RuntimeException("No Mapping Found from " + mappingClassClass);
        } else {

            return getMappingByType(mappingMethodsList, mappingType, mappingObject);
        }

    }


    private Map<String, Function> getMappingByType(List<Method> mappingMethodsList, MappingType mappingType, Object mappingObject) throws Exception {

        Map<String, Function> mapping;

        if (MappingType.MIN.equals(mappingType)) {
            mapping = getMappingFromMethods(MappingType.MIN, mappingMethodsList, mappingObject);

            if (mapping == null) {
                return getMappingFromMethods(MappingType.FULL, mappingMethodsList, mappingObject);
            } else {
                return mapping;
            }

        } else {
            mapping = getMappingFromMethods(MappingType.MIN, mappingMethodsList, mappingObject);

            if (mapping != null) {
                Map<String, Function> mappingFromMethods = getMappingFromMethods(MappingType.ADDITIONAL, mappingMethodsList, mappingObject);
                if (mappingFromMethods != null) {
                    mapping.putAll(mappingFromMethods);
                }

                return mapping;
            } else {
                return getMappingFromMethods(MappingType.FULL, mappingMethodsList, mappingObject);
            }

        }
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
        return (toPropertyClass.isAnnotationPresent(MappedClass.class) && !isAlreadyProvided(toPropertyClass, rhs)) ? doTransform(toPropertyClass, MappingType.MIN, rhs) : rhs;
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
                    return fromExpression.apply(from[0]);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    private static final Log log = LogFactory.getLog(DomainTransformer.class);
    private ApplicationContext applicationContext;
}
