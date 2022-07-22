package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import com.bobocode.hoverla.bring.exception.NoUniqueBeanException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.containsNone;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * Implementation of ApplicationContext
 */
@Slf4j
public class ApplicationContextImpl implements ApplicationContext {

    private Table<String, Class<?>, BeanDefinition> beanDefinitionTable;

    /**
     * Scanners scan application packages to define {@link BeanDefinition} configs.
     * Received list of bean definitions validates by validator to avoid duplicates and other problems (see {@link BeanValidator}).
     * Beans definitions list maps to container structure. It's Table<String, Class<?>, BeanDefinition>, where
     * - row key is a name of bean
     * - column key is a type of bean
     * - mapped value is bean definition
     * Then context passes to {@link BeanInitializer} for beans instances initialization
     *
     * @param scanners    list of scanners for
     * @param validator   bean definition validator
     * @param initializer bean initializer
     */
    public ApplicationContextImpl(List<BeanScanner> scanners,
                                  BeanValidator validator,
                                  BeanInitializer initializer) {

        List<BeanDefinition> beanDefinitionList = runScanning(scanners);
        validator.validate(beanDefinitionList);
        beanDefinitionTable = populateBeansDefinitionTable(beanDefinitionList);
        initializer.initialize(beanDefinitionTable);

        log.info("Application context initialized");
    }

    private List<BeanDefinition> runScanning(List<BeanScanner> scanners) {
        log.info("Scanning application for finding bean definitions");

        return scanners.stream()
                .map(BeanScanner::scan)
                .flatMap(List::stream)
                .toList();
    }

    private Table<String, Class<?>, BeanDefinition> populateBeansDefinitionTable(List<BeanDefinition> beanDefinitionList) {
        log.debug("Store scanned bean definition in application context");
        HashBasedTable<String, Class<?>, BeanDefinition> beanDefinitionTable = HashBasedTable.create();
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            log.debug("Bean definition with name = {} and type = {} will be added", beanDefinition.name(), beanDefinition.type());
            beanDefinitionTable.put(beanDefinition.name(), beanDefinition.type(), beanDefinition);
        }
        return beanDefinitionTable;
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
        // TODO: add implementation
        return null;
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        checkNotNull(beanType, "The argument [beanType] must be not null");

        List<T> beans = beanDefinitionTable.column(beanType)
                .values()
                .stream()
                .map(beanDefinition -> beanType.cast(beanDefinition.instance()))
                .toList();
        if (beans.isEmpty()) {
            throw new NoSuchBeanException("Bean with provided name/type [%s] not found in the context".formatted(beanType.getSimpleName()));
        } else if (beans.size() > 1) {
            throw new NoUniqueBeanException("Expected single bean of type %s, but found %d".formatted(beanType.getSimpleName(), beans.size()));
        } else return beans.get(0);
    }

    @Override
    public Object getBean(String beanName) {
        checkBeanName(beanName);

        return beanDefinitionTable.row(beanName)
                .values()
                .stream()
                .findFirst()
                .map(BeanDefinition::instance)
                .orElseThrow(() -> new NoSuchBeanException("Bean with provided name/type [%s] not found in the context".formatted(beanName)));
    }

    @Override
    public <T> T getBean(String beanName, Class<T> beanType) {
        checkNotNull(beanType, "The argument [beanType] must be not null");
        checkBeanName(beanName);

        Object bean = getBean(beanName);
        if (bean.getClass().isAssignableFrom(beanType)) {
            return beanType.cast(bean);
        } else {
            throw new NoSuchBeanException("Bean with provided name/type [%s] not found in the context".formatted(beanName));
        }
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        checkNotNull(beanType, "The argument [beanType] must be not null");

        return beanDefinitionTable.column(beanType)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> beanType.cast(entry.getValue().instance())));
    }

    @Override
    public boolean containsBean(String beanName) {
        checkBeanName(beanName);
        return !beanDefinitionTable.row(beanName).isEmpty();
    }

    private void checkBeanName(String beanName) {
        checkArgument(isNotEmpty(beanName), "The argument [beanName] must be not null or empty");
        checkArgument(isNotBlank(beanName), "The argument [beanName] must be not blank");
        checkArgument(containsNone(beanName, SPACE), "The argument [beanName] must not contain spaces");
    }

}
