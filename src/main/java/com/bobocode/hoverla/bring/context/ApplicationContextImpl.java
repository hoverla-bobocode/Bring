package com.bobocode.hoverla.bring.context;

import com.bobocode.hoverla.bring.exception.NoSuchBeanException;
import com.bobocode.hoverla.bring.exception.NoUniqueBeanException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.containsNone;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public class ApplicationContextImpl implements ApplicationContext {

    private static final String BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE = "The argument [beanType] must be not null";
    private static final String BEAN_NAME_MUST_NOT_CONTAIN_SPACES = "The argument [beanName] must not contain spaces";
    private static final String BEAN_NAME_MUST_BE_NOT_NULL_MESSAGE = "The argument [beanName] must be not null or empty";
    private static final String NO_SUCH_BEAN_EXCEPTION_MESSAGE = "Bean with provided name/type [%s] not found in the context";
    private static final String NO_UNIQUE_BEAN_EXCEPTION_MESSAGE = "Expected single bean of type %s, but found %d";

    private final BeanDefinitionsContainer container;

    /**
     * Scanners scan application packages to define {@link BeanDefinition} configs.
     * Received list of bean definitions validates by validator to avoid duplicates and other problems (see {@link BeanDefinitionValidator}).
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
                                  BeanDefinitionValidator validator,
                                  BeanInitializer initializer) {

        List<BeanDefinition> beanDefinitionList = runScanning(scanners);
        validator.validate(beanDefinitionList);
        container = new BeanDefinitionsContainer(beanDefinitionList);
        initializer.initialize(container);

        log.info("Application context initialized");
    }

    private List<BeanDefinition> runScanning(List<BeanScanner> scanners) {
        return scanners.stream()
                .map(BeanScanner::scan)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        checkNotNull(beanType, BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE);

        List<T> beans = findBeanByType(beanType);
        if (beans.isEmpty()) {
            throw new NoSuchBeanException(NO_SUCH_BEAN_EXCEPTION_MESSAGE.formatted(beanType.getSimpleName()));
        }
        if (beans.size() > 1) {
            throw new NoUniqueBeanException(NO_UNIQUE_BEAN_EXCEPTION_MESSAGE.formatted(beanType.getSimpleName(), beans.size()));
        }
        return beans.get(0);
    }

    private <T> List<T> findBeanByType(Class<T> beanType) {
        return container.getBeansAssignableFromType(beanType)
                .stream()
                .map(beanDefinition -> beanType.cast(beanDefinition.getInstance()))
                .toList();
    }

    @Override
    public Object getBean(String beanName) {
        checkBeanName(beanName);
        return container.getBeanDefinitionByName(beanName)
                .map(BeanDefinition::getInstance)
                .orElseThrow(() -> new NoSuchBeanException(NO_SUCH_BEAN_EXCEPTION_MESSAGE.formatted(beanName)));
    }

    @Override
    public <T> T getBean(String beanName, Class<T> beanType) {
        checkNotNull(beanType, BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE);
        checkBeanName(beanName);

        Object bean = getBean(beanName);
        try {
            return beanType.cast(bean);
        } catch (ClassCastException ex) {
            throw new NoSuchBeanException(NO_SUCH_BEAN_EXCEPTION_MESSAGE.formatted(beanType.getName()));
        }
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        checkNotNull(beanType, BEAN_TYPE_MUST_BE_NOT_NULL_MESSAGE);
        return container.getBeansAssignableFromType(beanType)
                .stream()
                .collect(Collectors.toMap(BeanDefinition::name, bean -> beanType.cast(bean.getInstance())));
    }

    @Override
    public boolean containsBean(String beanName) {
        checkBeanName(beanName);
        return container.getBeanDefinitionByName(beanName).isPresent();
    }

    private void checkBeanName(String beanName) {
        checkArgument(isNotEmpty(beanName), BEAN_NAME_MUST_BE_NOT_NULL_MESSAGE);
        checkArgument(containsNone(beanName, SPACE), BEAN_NAME_MUST_NOT_CONTAIN_SPACES);
    }

}
