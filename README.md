# Bring

---

## What is Bring?

---
**Bring is a dependency injection framework**. It uses IoC (Inversion of Control) container, which is represented by
[ApplicationContext](src/main/java/com/bobocode/hoverla/bring/context/ApplicationContext.java),
where [beans](#bean) are managed. The framework can create beans for you and inject them as dependencies to other beans

## Quick Start

---
To install Bring locally you should:

* clone repo ```https://github.com/hoverla-bobocode/Bring.git```
* go to the root of Bring project ```cd <path_to_bring>/Bring```
* build jar with ```mvn clean install -DskipTests```
* add jar to your maven project

```
<dependency>
    <groupId>com.bobocode.hoverla</groupId>
    <artifactId>bring</artifactId>
    <version>1.0-SNAPSHOT</version> 
</dependency>
```

Sorry for inconvenience, we're planning to move to maven central soon.

To easily start an application with Bring, use the `BringApplication.loadContext("package.to.scan")` method

```java
import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;

public class DemoApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = BringApplication.loadContext("packages.to.scan"); // provide packages to be scanned for beans 
    }
}
```

If you need to customize loading of `ApplicationContext`, you can do it with `ApplicationContextBuilder`. Here's an
example of its usage:

```java
import ch.qos.logback.classic.Level;
import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;

public class DemoApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = BringApplication.getContextBuilder()
                .logLevel(Level.DEBUG)                  // provide logging level
                .packagesToScan("packages.to.scan")     // provide packages to be scanned for beans
                .build();
    }
}
```

### Bean

---
**Bean** is a class that is managed by IoC container. To define a bean you have multiple options:

- [Annotation Configuration](#annotation-configuration)
- [Java Configuration](#java-configuration)

### Annotation configuration

---
To define a bean with **annotation configuration** just annotate your desired bean class with
[@Bean](src/main/java/com/bobocode/hoverla/bring/annotation/Bean.java) annotation. Look at the next example:

```java
import com.bobocode.hoverla.bring.annotation.Bean;

@Bean
public class MySimpleBean {

}
```

By default, **Bring generates a bean name as a fully qualified class name**. You can customize it by providing a `value`
property for
the [@Bean](src/main/java/com/bobocode/hoverla/bring/annotation/Bean.java) annotation

```java
import com.bobocode.hoverla.bring.annotation.Bean;

@Bean("customBean")
public class MyCustomBean {

}
```

If you have multiple beans of the same type, you can make the one preferable for injection without specifying the bean
name over the other ones by providing a `primary` value for
the [@Bean](src/main/java/com/bobocode/hoverla/bring/annotation/Bean.java) annotation

```java
import com.bobocode.hoverla.bring.annotation.Bean;

@Bean(primary = true)
public class MyPrimaryBean {
}
```

If your class has dependencies that should be injected, you may use one of these types of injection:

- [Constructor Injection](#constructor-injection)
- [Field Injection](#field-injection)

A bean class has the following constraints:

- if a class has multiple constructors, mark the one with the fields that should be injected by Bring with
  [@Inject](src/main/java/com/bobocode/hoverla/bring/annotation/Inject.java) annotation. If a class has only one
  constructor, the annotation is not necessary
- use only [constructor injection](#constructor-injection) or [field injection](#field-injection).
  Using of both injection types **is not recommended**, because it may lead to unpredictable results

### Java configuration

---
With Java Configuration you can provide a way how a bean should be instantiated via Java code. Use
a [@Configuration](src/main/java/com/bobocode/hoverla/bring/annotation/Configuration.java)
annotation to mark a class that will contain bean declarations. To declare a bean create a method and mark it
as [@Bean](src/main/java/com/bobocode/hoverla/bring/annotation/Bean.java)

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class MyJavaConfiguration {

    @Bean
    public MyBean myBean() {
        return new MyBean();
    }
}
```

By default, **Bring will use a method name as a bean name**. The bean name can be provided via
the [@Bean](src/main/java/com/bobocode/hoverla/bring/annotation/Bean.java) annotation by specifying its value

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class MyJavaConfiguration {

    @Bean("customBeanFromJavaConfiguration")
    public MyCustomBean myCustomBean() {
        return new MyCustomBean();
    }
}
```

It's also acceptable to mark a bean as `primary` for avoiding conflicts when there are multiple beans of the same type.
In that case bean marked as primary will be injected

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class MyJavaConfiguration {

    @Bean(primary = true)
    public MyCustomBean myPrimaryCustomBean() {
        return new MyCustomBean();
    }
}
```

If you want other beans to be injected inside a method, provide them as parameters

```java
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class MyJavaConfiguration {

    @Bean
    public MyBeanWithDependency myBeanWithDependency(MySimpleBean mySimpleBean) {
        return new MyBeanWithDependency(mySimpleBean);
    }
}
```

To specify a bean name use a [@Qualifier](src/main/java/com/bobocode/hoverla/bring/annotation/Qualifier.java) annotation

```java
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class MyJavaConfiguration {

    @Bean
    public MyBeanWithCustomDependency myBeanWithCustomDependency(@Qualifier("customBean") MyCustomBean myCustomBean) {
        return new MyBeanWithCustomDependency(myCustomBean);
    }
}
```

### Constructor injection

---
To use constructor injection, provide beans, that you want to be injected, as parameters to your bean's constructor.
If the bean has multiple constructors, the
annotation [@Inject](src/main/java/com/bobocode/hoverla/bring/annotation/Inject.java)
should be provided. If only one constructor is present, the annotation is not necessary

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class MyBeanWithDependencies {

    private final MyCustomBean myCustomBean;

    @Inject
    public MyBeanWithDependencies(MyCustomBean myCustomBean) {
        this.myCustomBean = myCustomBean;
    }

    public MyBeanWithDependencies() {

    }
}
```

If you want to specify a bean name (if there are multiple beans of the same type, for example), use
a [@Qualifier](src/main/java/com/bobocode/hoverla/bring/annotation/Qualifier.java) annotation. If there are multiple
bean of the same type, and you want the one of them to be injected without specifying a bean name, mark it as `primary`

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Configuration;

@Configuration
public class MyJavaConfiguration {

    @Bean("customBeanFromJavaConfiguration")
    public MyCustomBean myCustomBean() {
        return new MyCustomBean();
    }

    @Bean(primary = true)
    public MyCustomBean myPrimaryCustomBean() {
        return new MyCustomBean();
    }
}

```

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class MyBeanWithCustomDependencies {

    private final MyCustomBean myPrimaryCustomBean;
    private final MyCustomBean myCustomBean;

    public MyBeanWithCustomDependencies(MyCustomBean myPrimaryCustomBean, @Qualifier("customBeanFromJavaConfiguration") MyCustomBean myCustomBean) {
        this.myPrimaryCustomBean = myPrimaryCustomBean;
        this.myCustomBean = myCustomBean;
    }
}
```

### Field injection

---
Beans may be injected right in the field without a constructor. To do it mark a field with
an [@Inject](src/main/java/com/bobocode/hoverla/bring/annotation/Inject.java) annotation

**Please notice**: Don't mark a field as `final` because it won't be injected and context load will be aborted

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;

@Bean
public class MyBeanWithInjectedField {

    @Inject
    private MyCustomBean myCustomBean;
}
```

If you need to provide a custom bean name, use a
[@Qualifier](src/main/java/com/bobocode/hoverla/bring/annotation/Qualifier.java) annotation.

```java
import com.bobocode.hoverla.bring.annotation.Bean;
import com.bobocode.hoverla.bring.annotation.Inject;
import com.bobocode.hoverla.bring.annotation.Qualifier;

@Bean
public class MyBeanWithCustomInjectedField {

    @Inject
    @Qualifier("customBean")
    private MyCustomBean myCustomBean;
}
```
