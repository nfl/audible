package com.nfl.util.mapper.service

import static org.apache.commons.beanutils.PropertyUtils.getPropertyType
import static org.apache.commons.beanutils.PropertyUtils.setNestedProperty

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

import com.nfl.util.mapper.CustomMappingObject
import com.nfl.util.mapper.MappingType
import com.nfl.util.mapper.MultipleReturnObject
import com.nfl.util.mapper.annotation.MappedClass
import com.nfl.util.mapper.annotation.Mapping
import com.nfl.util.mapper.annotation.MappingClass

@Component
class DomainTransformer implements ApplicationContextAware {
	
	private static final Log log = LogFactory.getLog(DomainTransformer.class);
	
	private ApplicationContext applicationContext;
	

    public  <From, To> List<To> transformList(Class<To> toClass, Collection<From> list) {
        return list.collect {doTransform(toClass, MappingType.FULL, it) }
    }
	
	public  <From, To> List<To> transformList(Class<To> toClass, Collection<From> list, MappingType mappingType) {
		return list.collect {doTransform(toClass, mappingType, it) }
	}

    public  <To> To transform(Class<To> toClass, Object... from) {
         doTransform(toClass, MappingType.FULL, from)
    }
	
	public  <To> To transformMin(Class<To> toClass, Object... from) {
		doTransform(toClass, MappingType.MIN, from)
	}

    private  <To> To doTransform(Class<To> toClass, MappingType mappingType, Object... from) {
        if (isNull(from)) return null

        Object to = toClass.newInstance()
		
		def overrideMappingType = null;
		def customMappingName = null
		
		if (from[0] instanceof CustomMappingObject) {
			overrideMappingType = from[0].mappingType;
			customMappingName = from[0].mappingName;
		}
		
		from = checkForCustomMappingType(from);
		from = checkForMultipleReturnObject(from);
		

		def mapping = getMapping(toClass, mappingType, overrideMappingType, customMappingName, from)

        mapping.each { toPropertyName, fromExpression ->
			
			try {
			
				def toPropertyType = getPropertyType(to, toPropertyName)
				
				//If the field is collection
				if (Collection.class.isAssignableFrom(toPropertyType)) {
					
					Field field = toClass.getDeclaredField(toPropertyName);
					
					Type type = field.getGenericType();
					
					if (type instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) type;
						Class typeForCollection = pt.getActualTypeArguments()[0];
						
						def fromList = eval(toPropertyType, fromExpression, from)
						
						overrideMappingType = null;
						customMappingName = null
						
						if (fromList instanceof CustomMappingObject) {
							overrideMappingType = fromList.mappingType;
							customMappingName = fromList.mappingName;
						}
						
						fromList = checkForCustomMappingTypeList(fromList);

						//Check to see if they are already providing toClass in the closure
						boolean isOverride = fromList != null && !fromList.isEmpty() && !typeForCollection.isAssignableFrom(fromList.iterator().next().getClass())
						
						
						if (fromList == null || fromList.isEmpty()) {
							return null;
						} else if (isOverride && typeForCollection.isAnnotationPresent(MappedClass.class)) {
							
							
							//Recursively call transform
							to."$toPropertyName" = transformList(typeForCollection, fromList, overrideMappingType ? overrideMappingType : MappingType.MIN);
							
						} else {
							if (toPropertyType.isAssignableFrom(fromList.getClass())) {
								to."$toPropertyName" = fromList
								
							} else {
								if (List.class.isAssignableFrom(toPropertyType)) {
									def list = new ArrayList()
									list.addAll(fromList)
									to."$toPropertyName" = list
									
								} else if (Set.class.isAssignableFrom(toPropertyType)){
									def set = new HashSet()
									set.addAll(fromList)
									to."$toPropertyName" = set
								} else {
									throw new Exception("Unable to find find property collection type on " + toPropertyType)
								}
								
							}
							
						}
						
					}
	
				} else {
				
					def value = eval(toPropertyType, fromExpression, from)
					
					setNestedProperty(to, toPropertyName, value.asType(toPropertyType))
				}
			} catch (Exception e) {
			 	log.warn("unable to set " + toPropertyName + " on " + toClass, e);
			}
			
        }

        return to
    }
	
	private Object[] checkForMultipleReturnObject(Object[] from) {
		if (from.size() == 1 && from[0] instanceof MultipleReturnObject) {
			return from[0].objects;
		} else {
			return from;
		}
	}
	
	private def checkForCustomMappingTypeList(def fromList) {
		if (fromList instanceof CustomMappingObject) {
			return fromList.object
			
		} else {
			return fromList;
		}
	}
	
	private Object[] checkForCustomMappingType(Object[] from) {
		if (from.size() == 1 && from[0] instanceof CustomMappingObject) {
			return [from[0].object] as Object []
			
		} else {
			return from;
		}
	}
	
	private Map getMapping(Class toClass, MappingType mappingType, def overrideMappingType, def customMappingName, Object... from) {
		
		Class [] originalClasses = from.collect{it.getClass()}

		
		if (overrideMappingType != null) {
			mappingType = overrideMappingType;
		}
		
		
		if (toClass.isAnnotationPresent(MappingClass.class)) {
			MappingClass mappingClass = toClass.getAnnotation(MappingClass.class);
			
			Class mappingClassClass = mappingClass.value();
			
			return getMappingFromMappingObject(mappingClassClass, mappingType, customMappingName, originalClasses)
			
		} else {
		
			return getMappingFromStatic(toClass, mappingType, customMappingName, originalClasses);
			
		}
	}
	
	private Map getMappingFromMappingObject(Class mappingClassClass, MappingType mappingType, String mappingName, Class... originalClasses) {
		
		Object mappingObject = applicationContext.getBean(mappingClassClass);
		
		Method [] mappingMethods = mappingClassClass.getMethods();
		def mappingMethodsList = mappingMethods.findAll { Method method ->
				method.isAnnotationPresent(Mapping.class) && isOriginalClassesMatch(originalClasses, method.getAnnotation(Mapping.class).originalClasses()) && isMappingNameMatch(mappingName, method.getAnnotation(Mapping.class).name())}
		
		
		if (mappingMethodsList.size() == 0) {
			throw new RuntimeException("No Mapping Found from " + mappingClassClass);
		} else {
		
			return getMappingByType(mappingMethodsList, mappingType, mappingObject);
		}
	}
	
	private Map getMappingFromStatic(Class toClass, MappingType mappingType, String mappingName, Class... originalClasses) {
		Method [] mappingMethods = toClass.getMethods();
		def mappingMethodsList = mappingMethods.findAll { Method method ->
				Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Mapping.class) && isOriginalClassesMatch(originalClasses, method.getAnnotation(Mapping.class).originalClasses()) && isMappingNameMatch(mappingName, method.getAnnotation(Mapping.class).name())}
		
		if (mappingMethodsList.size() == 0) {
			throw new RuntimeException("No Mapping Found from " + toClass);
		} else {
		
			return getMappingByType(mappingMethodsList, mappingType, null);
		}
	}
	
	private Map getMappingByType(def mappingMethodsList, MappingType mappingType, Object mappingObject) {
		
		def mapping = null;
		
		if (MappingType.MIN.equals(mappingType)) {
			mapping = getMappingFromMethods(MappingType.MIN, mappingMethodsList, mappingObject)
			
			if (mapping == null) {
				return mapping = getMappingFromMethods(MappingType.FULL, mappingMethodsList, mappingObject)
			} else {
				return mapping;
			}
			
		} else {
			mapping = getMappingFromMethods(MappingType.MIN, mappingMethodsList, mappingObject)
			
			if (mapping != null) {
				return mapping + getMappingFromMethods(MappingType.ADDITIONAL, mappingMethodsList, mappingObject)
			} else {
				return getMappingFromMethods(MappingType.FULL, mappingMethodsList, mappingObject)
			}
		}
		
	}
	
	
	private Map getMappingFromMethods(MappingType type, List mappingMethodsList, Object mappingObject) {
		for (Method method : mappingMethodsList) {
			if (type.equals(method.getAnnotation(Mapping.class).value())) {
				return method.invoke(mappingObject, null);
			}
		}		
		return null;
	}
	
	private boolean isOriginalClassesMatch(Class [] fromOriginalClasses, Class [] methodOriginalClasses) {
		if (fromOriginalClasses.length != methodOriginalClasses.length) {
			return false;
		}
		
		for (int i = 0; i < fromOriginalClasses.length; i++) {
			if (!org.codehaus.groovy.runtime.NullObject.class.equals(fromOriginalClasses[i]) && !methodOriginalClasses[i].isAssignableFrom(fromOriginalClasses[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isMappingNameMatch(String fromMappingName, String methodMappingName) {
		if (fromMappingName == null) {
			return true;
		} else if (fromMappingName.equals(methodMappingName)) {
			return true;
		} else {
			return false;
		}
		
	}

    private  Object eval(Class toPropertyClass, Closure fromExpression, Object... from) {
        def rhs = safelyEvaluateClosure(fromExpression, from)
        return (toPropertyClass.isAnnotationPresent(MappedClass.class) && !isAlreadyProvided(toPropertyClass, rhs)) ? doTransform(toPropertyClass, MappingType.MIN, rhs) : rhs
    }
	
	private boolean isAlreadyProvided(Class toPropertyClass, Object rhs) {
		return toPropertyClass.isAssignableFrom(rhs.getClass());
	}

    private  Object safelyEvaluateClosure(Closure fromExpression, Object... from) {
        try {
			
			switch (from.size()) {
				case 1 : return fromExpression(from[0])
				case 2 : return fromExpression(from[0], from[1])
				case 3 : return fromExpression(from[0], from[1], from[2])
				default: return null;
					
			}

        } catch (NullPointerException npe) {
            return null
        }
    }
	
	private boolean isNull(Object... from) {
		if (from == null) {
			return true
		} else if(from.size() == 0) {
			return true
		} else if(from.size() == 1 && from[0] == null) {
			return true
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
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}


}
