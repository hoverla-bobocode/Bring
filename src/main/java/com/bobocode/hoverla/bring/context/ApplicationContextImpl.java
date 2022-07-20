package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import com.bobocode.hoverla.bring.exception.NoUniqueBeanException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;


/**
 * Implementation of ApplicationContext
 */
@Slf4j
public class ApplicationContextImpl implements ApplicationContext {
    private Table<String, Class<?>, BeanDefinition> context = HashBasedTable.create();
    private final String BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE = "The argument [beanType] must be not null";
    private final String NO_SUCH_BEAN_EXCEPTION_MESSAGE = "Bean with provided name/type [%s] not found in the context";

    /**
     *  Scanners scan application to define beans configs and map them into {@link BeanDefinition} lists.
     *  Received list of bean definitions validates by validator to avoid duplicates and other problems (see {@link BeanValidator}).
     *  Beans definitions list maps to container structure. It's Table<String, Class<?>, BeanDefinition>, where
     *      - row key is a name of bean
     *      - column key is a type of bean
     *      - mapped value is bean definition
     *  Then context passes to {@link BeanInitializer} for beans instances initialization
     *
     * @param scanners      list of scanners for
     * @param validator     bean definition validator
     * @param initializer   bean initializer
     */
    public ApplicationContextImpl(List<BeanScanner> scanners,
                                  BeanValidator validator,
                                  BeanInitializer initializer) {

        List<BeanDefinition> beanDefinitionList = scanningApplication(scanners);
        validate(validator, beanDefinitionList);
        contextCreation(beanDefinitionList);
        beansInstantiation(initializer);

        log.info("Application context initialized");
    }

    private List<BeanDefinition> scanningApplication(List<BeanScanner> scanners) {
        List<BeanDefinition> beanDefinitionList = new ArrayList<>();
        log.info("Scanning application for finding bean definitions");
        for (BeanScanner beanScanner : scanners) {
            log.debug("Running bean scanner {}", beanScanner.getClass().getSimpleName());
            beanDefinitionList.addAll(beanScanner.scan());
        }
        return beanDefinitionList;
    }

    private void validate(BeanValidator validator, List<BeanDefinition> beanDefinitionList) {
        log.info("Beans definitions validating");
        validator.validate(beanDefinitionList);
    }

    private void contextCreation(List<BeanDefinition> beanDefinitionList) {
        log.debug("Context table generation");
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            log.debug("Bean definition with name = {} and type = {} will be added", beanDefinition.name(), beanDefinition.type());
            context.put(beanDefinition.name(), beanDefinition.type(), beanDefinition);
        }
    }

    private void beansInstantiation(BeanInitializer initializer) {
        log.info("Beans initialization");
        initializer.initialize(context);
    }


    @Override
    public <T> void register(String beanName, Class<T> beanType) {
        // TODO:  add implementation
    }

    @Override
    public void register(Class<?>... beanTypes) {
        // TODO:  add implementation
    }

    @Override
    public <T> String register(Class<T> beanType) {
        return null;
        // TODO: add implementation
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        Objects.requireNonNull(beanType, BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE);

        List<T> beans = context.column(beanType)
                .values()
                .stream()
                .map(beanDefinition -> beanType.cast(beanDefinition.instance()))
                .toList();
        if (beans.isEmpty()) {
            throw new NoSuchBeanException(NO_SUCH_BEAN_EXCEPTION_MESSAGE.formatted(beanType.getSimpleName()));
        } else if (beans.size() > 1) {
            throw new NoUniqueBeanException("Expected single bean of type " + beanType.getSimpleName()
                + ", but found " + beans.size());
        } else return beans.get(0);
    }

    @Override
    public Object getBean(String beanName) {
        Optional<BeanDefinition> beanDefinition = context.row(beanName)
                .values()
                .stream()
                .findFirst();
        if (beanDefinition.isPresent()) {
            return beanDefinition.get().instance();
        } else {
            throw new NoSuchBeanException(NO_SUCH_BEAN_EXCEPTION_MESSAGE.formatted(beanName));
        }
    }

    @Override
    public <T> T getBean(String beanName, Class<T> beanType) {
        Objects.requireNonNull(beanType, BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE);

        Object bean = getBean(beanName);
        if (bean.getClass().isAssignableFrom(beanType)) {
            return beanType.cast(bean);
        } else {
            throw new NoSuchBeanException(NO_SUCH_BEAN_EXCEPTION_MESSAGE.formatted(beanName));
        }
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return context.column(beanType)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> beanType.cast(entry.getValue().instance())));

    }

    @Override
    public boolean containsBean(String beanName) {
        return !context.row(beanName).isEmpty();
    }

}
