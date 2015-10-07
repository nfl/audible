package com.nfl.dm.audible.service;

import com.nfl.dm.audible.CustomMappingWrapper;
import com.nfl.dm.audible.MappingFunction;
import com.nfl.dm.audible.MappingType;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.TypeFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * DomainMapper uses Map of String, Function to map one pojo to another
 */
@SuppressWarnings("unused")
public class DomainMapper {

    private static final Logger log = LoggerFactory.getLogger(DomainMapper.class);


    private TypeSafeCopy typeSafeCopy = new TypeSafeCopy();

    private static final String EMPTY = "";

    private MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

    private final MappingType defaultEmbeddedMapping;

    private final boolean autoMapUsingOrika;

    private final boolean parallelProcessEmbeddedList;

    private final boolean ignoreNullPointerException;

    private final boolean failOnException;

    @Autowired
    private MappingService mappingService;

    DomainMapper(DomainMapperBuilder builder) {
        this.defaultEmbeddedMapping = builder.getDefaultEmbeddedMapping();
        this.autoMapUsingOrika = builder.isAutoMapUsingOrika();
        this.parallelProcessEmbeddedList = builder.isParallelProcessEmbeddedList();
        this.ignoreNullPointerException = builder.isIgnoreNullPointerException();
        this.failOnException = builder.isFailOnException();
    }

    public <From, To> List<To> mapList(Class<To> toClass, Collection<From> list) {
        return this.mapList(toClass, list, EMPTY, MappingType.NORMAL);
    }


    public <From, To> List<To> mapList(Class<To> toClass, Collection<From> list, MappingType mappingType) {
        return this.mapList(toClass, list, EMPTY, mappingType);
    }

    public <From, To> List<To> mapList(Class<To> toClass, Collection<From> list, String mappingName) {
        return this.mapList(toClass, list, mappingName, MappingType.NORMAL);
    }

    public <From, To> List<To> mapList(Class<To> toClass, Collection<From> list, String mappingName, MappingType mappingType) {
        return list.stream().map(o -> doMapping(toClass, o, mappingName, mappingType, false, null)).collect(Collectors.toList());
    }

    public <From, To> List<To> mapListParallel(Class<To> toClass, Collection<From> list) {
        return this.mapListParallel(toClass, list, EMPTY, MappingType.NORMAL);
    }


    public <From, To> List<To> mapListParallel(Class<To> toClass, Collection<From> list, MappingType mappingType) {
        return this.mapListParallel(toClass, list, EMPTY, mappingType);
    }

    public <From, To> List<To> mapListParallel(Class<To> toClass, Collection<From> list, String mappingName) {
        return this.mapListParallel(toClass, list, mappingName, MappingType.NORMAL);
    }

    public <From, To> List<To> mapListParallel(Class<To> toClass, Collection<From> list, String mappingName, MappingType mappingType) {
        return list.parallelStream().map(o -> doMapping(toClass, o, mappingName, mappingType, false, null)).collect(Collectors.toList());
    }



    public <From, To> To map(Class<To> toClass, From from) {
        return this.map(toClass, from, EMPTY, MappingType.NORMAL);
    }

    public <From, To> To map(Class<To> toClass, From from, MappingType mappingType) {
        return this.map(toClass, from, EMPTY, mappingType);
    }

    public <From, To> To map(Class<To> toClass, From from, String mappingName) {
        return this.map(toClass, from, mappingName, MappingType.NORMAL);
    }

    public <From, To> To map(Class<To> toClass, From from, String mappingName, MappingType mappingType) {
        return this.doMapping(toClass, from, mappingName, mappingType, false, null);
    }



    public <From, To> To merge(Class<To> toClass, From from, To to) {
        return this.merge(toClass, from, to, EMPTY, MappingType.NORMAL);
    }

    public <From, To> To merge(Class<To> toClass, From from, To to, MappingType mappingType) {
        return this.merge(toClass, from, to, EMPTY, mappingType);
    }

    public <From, To> To merge(Class<To> toClass, From from, To to, String mappingName) {
        return this.merge(toClass, from, to, mappingName, MappingType.NORMAL);
    }

    public <From, To> To merge(Class<To> toClass, From from, To to, String mappingName, MappingType mappingType) {
        return this.doMapping(toClass, from, mappingName, mappingType, false, to);
    }


    @SuppressWarnings("unchecked")
    private <From, To> To doMapping(final Class<To> toClass, From from, String mappingName, MappingType mappingType, boolean hasFullAutoParent, To nonFinalTo) {

        final boolean hasFullAutoParentFinal;

        if (from == null) return null;

        if (from instanceof CustomMappingWrapper) {
            CustomMappingWrapper cmo = (CustomMappingWrapper) from;
            from = (From)cmo.getObject();
            mappingType = cmo.getMappingType();
            mappingName = cmo.getMappingName();

            if (from == null) return null;

        }

        Class fromClass = from.getClass();

        MappingFunction mappingFunction = mappingService.getMappingFunction(toClass, fromClass, mappingName, mappingType);

        Map<String, Function> mapping = mappingFunction.getMapping();
        if (!hasFullAutoParent && autoMapUsingOrika) {

            //TODO move to cache
            MapperFacade orikaMapper;

            if (!mapperFactory.existsRegisteredMapper(TypeFactory.valueOf(fromClass), TypeFactory.valueOf(toClass), false)) {

                mapperFactory.classMap(fromClass, toClass)
                        .byDefault().register();
            }

            orikaMapper = mapperFactory.getMapperFacade();


            nonFinalTo = orikaMapper.map(from, toClass);

            hasFullAutoParentFinal = true;

        } else if (nonFinalTo == null){
            try {
                nonFinalTo = toClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Error creating instace of " + toClass, e);
            }
            hasFullAutoParentFinal = hasFullAutoParent;
        } else {
            hasFullAutoParentFinal = hasFullAutoParent;
        }

        final Object finalFrom = from;

        final To to = nonFinalTo;

        assert mapping != null;

        mapping.entrySet().stream().forEach(entry ->
                {
                    String toPropertyName = entry.getKey();
                    Function fromFunction = entry.getValue();
                    try {

                        Class toPropertyType = PropertyUtils.getPropertyType(to, toPropertyName);

                        //If the field is collection
                        if (Collection.class.isAssignableFrom(toPropertyType)) {
                            handleCollections(to, toClass, toPropertyType, fromFunction, finalFrom, toPropertyName, hasFullAutoParentFinal);
                        } else {

                            Object value = eval(toPropertyType, fromFunction, finalFrom, hasFullAutoParentFinal, to, toPropertyName);

                            PropertyUtils.setNestedProperty(to, toPropertyName, value);
                        }

                    } catch (Exception e) {
                        log.warn("unable to set " + toPropertyName + " on " + toClass, e);
                        if (failOnException) {
                            throw new RuntimeException(e);
                        }
                    }

                }
        );

        handlePostProcessor(to, toClass, from, mappingName);

        return to;
    }

    private void handleCollections(Object to, Class toClass, Class toPropertyType, Function fromFunction, Object finalFrom, String toPropertyName, boolean hasFullAutoParent) throws Exception {
        Field field = toClass.getDeclaredField(toPropertyName);

        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Class typeForCollection = (Class) pt.getActualTypeArguments()[0];

            Object object = eval(toPropertyType, fromFunction, finalFrom, hasFullAutoParent, to, toPropertyName);

            List fromList = null;

            MappingType overrideMappingTypeNested = null;

            if (object != null) {
                if (object instanceof CustomMappingWrapper) {
                    CustomMappingWrapper cmo = (CustomMappingWrapper) object;
                    overrideMappingTypeNested = cmo.getMappingType();
                    fromList = new ArrayList((Collection) cmo.getObject());
                } else {

                    fromList = new ArrayList((Collection) object);
                }
            }

            //Check to see if they are already providing toClass in the closure
            boolean isOverride = fromList != null && !fromList.isEmpty() && !typeForCollection.isAssignableFrom(fromList.iterator().next().getClass());


            if (fromList == null || fromList.isEmpty()) { //Set null if empty
                PropertyUtils.setProperty(to, toPropertyName, null);
            } else if (isOverride && mappingService.hasMappingForClass(typeForCollection)) { //Recursively call

                Object toValue;
                if (parallelProcessEmbeddedList) {
                    toValue = mapListParallel(typeForCollection, fromList, overrideMappingTypeNested != null ? overrideMappingTypeNested : defaultEmbeddedMapping);
                } else {
                    toValue = mapList(typeForCollection, fromList, overrideMappingTypeNested != null ? overrideMappingTypeNested : defaultEmbeddedMapping);
                }

                PropertyUtils.setProperty(to, toPropertyName, toValue);


            } else { //Already provided by the fromList
                if (toPropertyType.isAssignableFrom(fromList.getClass())) { //Collection type is assignable

                    PropertyUtils.setProperty(to, toPropertyName, fromList);

                } else { //Explicit collection type
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
    }

    private void handlePostProcessor(Object to, Class toClass, Object from, String mappingName) {
        List<Method> methodsList =  mappingService.getPostProcessors(toClass, from.getClass(), mappingName);

        if (methodsList == null) {
            return;
        }

        List<Object> objectList = new ArrayList<>();
        objectList.add(to);
        objectList.add(from);

        Object[] objectArray = new Object[objectList.size()];

        objectArray = objectList.toArray(objectArray);

        Object mappingObject = mappingService.getMappingClassObject(toClass);

        for (Method method : methodsList) {
            try {
                method.invoke(mappingObject, objectArray);
            } catch (Exception e) {
                log.warn("Problem calling post processor", e);
                if (failOnException) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")

    private <From> Object eval(Class toPropertyClass, Function fromExpression, From from, boolean hasFullAutoParent, Object to, String toPropertyName) throws Exception {
        Object rhs = safelyEvaluateClosure(fromExpression, from);

        if (rhs != null && mappingService.hasMappingForClass(toPropertyClass) && !isAlreadyProvided(toPropertyClass, rhs)) {

            Object toProperty = PropertyUtils.getProperty(to, toPropertyName);

            return doMapping(toPropertyClass, rhs, EMPTY, defaultEmbeddedMapping, hasFullAutoParent, toProperty);
        }
        else return rhs;
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

            if (ignoreNullPointerException) {
                return null;
            } else {
                throw npe;
            }

        }

    }

    public MappingType getDefaultEmbeddedMapping() {
        return defaultEmbeddedMapping;
    }

    public boolean isAutoMapUsingOrika() {
        return autoMapUsingOrika;
    }

    public boolean isParallelProcessEmbeddedList() {
        return parallelProcessEmbeddedList;
    }

    public boolean isIgnoreNullPointerException() {
        return ignoreNullPointerException;
    }
}
