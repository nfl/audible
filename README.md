#Audible


##Introduction

Audible is a mapper library that converts a POJO to another POJO using Java 8 lambdas to implement the mapping logic. It depends on Java 8 and Spring and uses [Orika](https://github.com/orika-mapper/orika) to support default mapping. 

###Why Another Mapper?

There are other POJO mapping libraries out there such as [Dozer](http://dozer.sourceforge.net/), [Orika](https://github.com/orika-mapper/orika), and [Apache BeanUtils](http://commons.apache.org/proper/commons-beanutils/). One of the assumptions that these libraries assume is that the source domain contains all necessary information to convert to the target domain. If you need to call external services such as retrieving additional information from a database during the conversion process, you would instead have to aggregate it into the source domain before you can use the mapper library. Audible provides the ability to have complete control including calling additional service methods during the conversion. You can also define multiple case-specific mappings for a single type. You can also define mappings for different source domains to one target domain. Audible combines the flexibility of manual conversions with some of the convenience of other mapping libraries.

|       | Audible   | Dozer   | BeanUtils   |
|-------|:---------:|:-------:|:-----------:|
|Java 8 Required | 	✓	| 		| 		|
|Spring Requred 	|	✓	|		|		|
|Type Safe Mapping |✓	|		|	✓	|
|Built-in Concurrent Processing|	✓	|		|		|
|Call Other Service Within Mapping|	✓	|		|		|
|Automatic Mapping of Embedded Objects|	✓	|	✓	|	✓	|
|Multiple Mapping Versions|	✓	|		|		|
|Multiple Source Type Support|✓	|		|		|
|Post Processor|	✓	|		|		|


##Configuration

In order to configure this service first create a Spring configuration class like the one shown. All classes that are annotated with `@MappingTo` should be in your Spring context. In addition `MappingService` and `DomainMapper` should be defined as Spring Beans

###[Example] ApplicationConfig.java
```java
import com.nfl.util.mapper.service.DomainMapper;
import com.nfl.util.mapper.service.MappingService;

@Configuration
@ComponentScan(basePackages = {"package.of.mapping.classes"})
public class ApplicationConfig {

    @Bean
    public MappingService domainService() {
        return new MappingService();
    }

    @Bean
    public DomainMapper domainMapper() {
        return new DomainMapper();
    }

}
```

###Main.java
```java
@ContextConfiguration(classes = {ApplicationConfig.class})
public class Main() {

	@AutoWired
	DomainMapper dm;
	
	...
}
```

##Annotations
There are three annotations that are used to denote a mapping of one class to another.

###@MappingTo

The first is the `@MappingTo` annotation takes one parameter designating the class that it maps to. It is used on the declaration of the mapping class. This class should contain all of the methods mapping from objects of other classes to an object of the designated class.

###@Mapping

The second annotation is the `@Mapping` annotation. It is used on the individual methods within the mapping class that differ based on the parameters supplied to the annotation. The parameters this annotation takes are `type`, `name`, `originalClass`, and `parallelCollections`.

* `type`: The type of the mapping, it is a value of the enum `MappingType` with the following possible values:
	* `FULL`
	* `MIN`
	* `ADDITIONAL`
	* `FULL_AUTO`
	
	The default value is `MappingType.FULL`. 
	
	`MappingType.FULL_AUTO` is a special type of mapping with it's own unique and helpful behavior. When a `FULL_AUTO` mapping is used the mapper will automatically, via reflection, map fields of the two objects that share the same name and type. It will then take the mapping provided in the user-created definition and override any specified fields using this custom logic.
* `name`: The name of the mapping, it is represented as a `String`. It does not need to be unique but there should be a unique pair of `originalClass` and `name`. The default value is the empty string `""`.
* `originalClass`: The class of the object that is the source of the mapping. This is a required value.
* `parallelCollections`: A boolean value telling whether inner-collections, that is fields of the `originalClass` which implement the `Collection` interface, should be copied in parallel or sequentially. Things to consider when setting this parameter include speed and other issues related to concurrency. The default value is `false`.

The method with the `@Mapping` annotation should have the following signature: `public Map<String, java.util.Function<toClass, ?>> funcName()`
One entry in the Map corresponds to a mapping for one field in the destination object. The keys in the map are the names of the setters of the desintation object. The values of the map can be defined with lambda expressions that takes in the source object as the parameter.

```java
@Component
@MappingTo(ToStudent.class)
public class ToStudentMapping {

    @Mapping(type = MappingType.FULL, originalClass = FromStudent.class)
    public Map<String, Function<FromStudent, ?>> getMapping() {
        Map<String, Function<FromStudent, ?>> map = new HashMap<>();
        map.put("firstName", (FromStudent s) -> s.getName().split(" ")[0]);
        map.put("lastName", (FromStudent s) -> s.getName().split(" ")[1]);

        return map;
    }
    
    ...
}
```

###@PostProcessor

The third annotation is the `@PostProcessor` annotation. This annotation is used when you want to execute some function following the mapping of one object to another. It is used inside of the class annotated with `@MappingTo`. 

This annotation takes two parameters, `originalClass` and `mappingName`. The `originalClass` parameter is required and should be the class of the object that is the source of the mapping. The `mappingName` parameter is a `String` and is the name of the mapping. It has a default value of the empty string `""`. 

A `@PostProcessor` method will be invoked after any mapping which has the same `originalClass` and `mappingName` as specified in this annotation. Methods annotated with `@PostProcessor` should have a return type of `void` and take two parameters. The first should have type `toClass` which matches the parameter in `@MappingTo`. The second should have type `fromClass` which matches the `originalClass` parameter in this `@PostProcessor` annotation. 

Modify your `toClass` object in this method.

```java
@Component
@MappingTo(ToStudent.class)
public class ToStudentMapping {

    @Mapping(type = MappingType.FULL, originalClass = FromStudent.class)
    public Map<String, Function<FromStudent, ?>> getMapping() {
        ...
    }
    
    @PostProcessor(originalClass = FromStudent.class)
	public void postProcess(ToStudent toStudent, FromStudent fromStudent) {
		toStudent.setSomeField(doSomethingThatRequiresToStudent(toStudent.getFirstName()));
	}
    
    ...
}
```

##DomainMapper.map Function
In order to perform the mapping from one object to another an instance of the `DomainMapper` class is used. The primary method used in mapping an object is `DomainMapper.map` which takes between two and four arguments. The method signatures are as follows:

* `public <From, To> To map(Class<To> toClass, From from)`
* `public <From, To> To map(Class<To> toClass, From from, MappingType mappingType)`
* `public <From, To> To map(Class<To> toClass, From from, String mappingName)`
* `public <From, To> To map(Class<To> toClass, From from, String mappingName, MappingType mappingType)`

If the `mappingType` parameter is not present it will default to a value of `MappingType.FULL`, if the `mappingName` parameter is not present it will default to the empty string `""`. If the `mappingType` is `MappingType.FULL` and a full mapping is not found, it will then search for mappings with `MappingType.MIN` and `MappingType.ADDITIONAL` and combine them only if both are found.


##Simple Mapping Example
###ToStudentMapping.java

```java
@Component
@MappingTo(ToStudent.class)
public class ToStudentMapping {

   @Autowired
   private SSNService ssnService;

   @Mapping(type = MappingType.FULL, originalClass = FromStudent.class)
    public Map<String, Function<FromStudent, ?>> getMapping() {
        Map<String, Function<FromStudent, ?>> map = new HashMap<>();
        map.put("firstName", (FromStudent s) -> s.getName().split(" ")[0]);
        map.put("lastName", (FromStudent s) -> s.getName().split(" ")[1]);
        //Ability to call any other Spring managed service bean
        map.put("ssn", (FromStudent s) -> ssnService.findByName(s.getName()));
        
        map.put("courses", (FromStudent s) -> s.getCourses());
        map.put("address", (FromStudent s) -> s.getAddress();
        return map;
    }
    
    @PostProcessor(originalClass = FromStudent.class)
	public void postProcess(ToStudent toStudent, FromStudent fromStudent) {
		doSomething();
	}
    
    ...
}
```

###ToCourseMapping.java
```java
@Component
@MappingTo(ToCourse.class)
public class ToCourseMapping {

	@Mapping(originalClass = FromCourse.class)
	public Map<String, Function<FromCourse, ?>> getMapping() {
		Map<String, Function<FromStudent, ?>> map = new HashMap<>();
		map.put("name", (FromCourse fc) -> parseCourseTitle(fc.getName()));
		map.put("courseNum", (FromCourse fc) -> parseCourseNumber(fc.getname()));
		map.put("gpa", (FromCourse fc) -> {
			if(fc.getLetterGrade().equals("A") return 4;
			else if(fc.getLetterGrade().equals("B") return 3;
			else if(fc.getLetterGrade().equals("C") return 2;
			else if(fc.getLetterGrade().equals("D") return 1;
			else if(fc.getLetterGrade().equals("F") return 0;
		}
	}
	
	public String parseCourseTitle(String name) {
		...
	}
	
	public String parseCourseNumber(String name) {
		...
	}
}
```

###ToAddressMapping.java

```java
@Component
@MappingTo(ToAddress.class)
public class ToAddressMapping {

	@AutoWired
	private GeoService geoService;

	@Mapping(originalClass = FromAddress.class)
	public Map<String, Function<FromAddress, ?>> getMapping() {
		Map<String, Function<FromAddress, ?>> map = new HashMap<>();
		map.put("street", (FromAddress fa) -> fa.getStreet());
		map.put("zip", (FromAddress fa) -> fa.getZip());
		map.put("state", (FromAddress fa) -> geoService.getStateFromZip(fa.getZip()));
		
		return map;
	}
}

```

###FromStudent.java

```java
public class FromStudent {

    private String name;

    List<FromCourse> courses;
    
    FromAddress address;
    
    ...
}
```

####FromCourse.java

```java
public class FromCourse {

	private String name;
	
	private String letterGrade
	
	...
}
```

###FromAddress.java

```java
public class FromAddress {

	private String street;
	
	private String zip;
	
	...
}
```

###ToStudent.java

```java
public class ToStudent {

    private String firstName;

    private String lastName;
    
    private String ssn;
    
    private List<ToCourse> courses;
    
    private ToAddress address;
    
    ...
}
```

###ToCourse.java

```java
public class ToCourse {

	private String name;
	
	private String courseNum;
	
	private Integer gpa;
	
	...
}
```

###ToAddress.java

```java
public class ToAddress {
	
	private String street;
	
	private String zip;
	
	private String state;
	
	...
}
```

###Driver.java
```java
public class Driver {

	@AutoWired
	DomainMapper dm;
	
	public static void main() {
		FromStudent fromStudent = new FromStudent();
		fromStudent.setName("Tom Brady");
		
		Student toStudent = dm.map(ToStudent.class, fromStudent);
		//toStudent.getFirstName() == "Tom"
		//toStudent.getLastName() == "Brady"
	}
}
```

##Embedded Mapping

##Named Mapping Example
###ToStudentMapping.java
```java
@Component
@MappingTo(ToStudent.class)
public class ToStudentMapping {

    @Mapping(type = MappingType.FULL, originalClass = FromStudent.class, name = "reverse")
    public Map<String, Function<FromStudent, ?>> reverseMapping() {
        Map<String, Function<FromStudent, ?>> map = new HashMap<>();
        map.put("lastName", (FromStudent s) -> s.getName().split(" ")[0]);
        map.put("firstName", (FromStudent s) ->  s.getName().split(" ")[1]);

        return map;
    }
    
    @PostProcessor(originalClass = FromStudent.class, mappingName = "reverse")
	public void postProcess(ToStudent toStudent, FromStudent fromStudent) {
		doSomething();
	}
    
    ...
}
```

###Main.java
```java
public class Main() {

	@AutoWired
	DomainMapper dm;
	
	public static void main() {
		FromStudent fromStudent = new FromStudent();
		fromStudent.setName("Tom Brady");
		fromStudent.setAge(38);
		fromStudent.setGpa(4.0);
		
		Student toStudent = dm.map(ToStudent.class, fromStudent, "reverseName");
		//toStudent.getFirstName() == "Brady"
		//toStudent.getLastName() == "Tom"
		//toStudent.getNums.getAge() == 38
		//toStudent.getNums.getGpa() == 4.0
	}
}
```
