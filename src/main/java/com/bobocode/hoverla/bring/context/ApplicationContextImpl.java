package com.bobocode.hoverla.bring.context;

import java.util.List;
import java.util.Map;

/**
 * Example of ApplicationContext implementation, can be removed or reimplemented when actual development starts
 */
public class ApplicationContextImpl implements ApplicationContext {
    public ApplicationContextImpl(List<BeanScanner> scanners,
                                  BeanValidator validator,
                                  BeanInitializer initializer) {
        /* 1) Scanners get all beans
           2) Merge List of bean definitions into one List
           3) Send beans list to validator to check duplicates
           4) Convert bean list to map
           5) Send beans map to initializer to instantiate beans and inject dependencies
         */
    }

    @Override
    public <T> void register(String name, Class<T> beanType) {
        System.out.println();
    }

    @Override
    public void register(Class<?>... beanTypes) {
        System.out.println();
    }

    @Override
    public <T> String register(Class<T> beanType) {
        return "null";
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return "null";
    }

    @Override
    public <T> T getBean(String beanName, Class<T> beanType) {
        return null;
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return null;
    }

    @Override
    public boolean containsBean(String name) {
        return true;
    }
}
