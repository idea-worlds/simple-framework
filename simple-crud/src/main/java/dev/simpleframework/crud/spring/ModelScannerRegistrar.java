package dev.simpleframework.crud.spring;

import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("all")
public class ModelScannerRegistrar implements ImportBeanDefinitionRegistrar {

    private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        return ClassUtils.getPackageName(importingClassMetadata.getClassName());
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attrs =
                AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ModelScan.class.getName()));
        if (attrs != null) {
            String defaultPackage = getDefaultBasePackage(metadata);
            this.register(attrs, defaultPackage);
        }
    }

    @SneakyThrows
    private void register(AnnotationAttributes attrs, String defaultPackage) {
        Class<?> superClass = attrs.getClass("superClass");
        String[] packages = basePackages(attrs, defaultPackage);

        Set<String> classNames = scanModels(packages, superClass);
        DatasourceType dsType = attrs.getEnum("datasourceType");
        String dsName = attrs.getString("datasourceName");
        ClassLoader classLoader = ModelScannerRegistrar.class.getClassLoader();
        for (String className : classNames) {
            Class modelClass = ClassUtils.forName(className, classLoader);
            Models.registerModel(modelClass, superClass, dsType, dsName);
        }
    }

    private static Set<String> scanModels(String[] packages, Class<?> superClass) {
        Set<BeanDefinition> beanNames = new HashSet<>();
        ClassPathModelScanner scanner = new ClassPathModelScanner(superClass);
        for (String p : packages) {
            beanNames.addAll(scanner.findCandidateComponents(p));
        }
        return beanNames.stream().map(BeanDefinition::getBeanClassName).collect(Collectors.toSet());
    }

    private static String[] basePackages(AnnotationAttributes attrs, String defaultPackage) {
        List<String> result = new ArrayList<>();
        for (String p : attrs.getStringArray("value")) {
            result.add(p);
        }
        for (String p : attrs.getStringArray("basePackages")) {
            result.add(p);
        }
        if (result.isEmpty()) {
            result.add(defaultPackage);
        }
        result = result.stream().filter(StringUtils::hasText).distinct().collect(Collectors.toList());
        return StringUtils.tokenizeToStringArray(
                StringUtils.collectionToCommaDelimitedString(result),
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
    }

    static class ClassPathModelScanner extends ClassPathScanningCandidateComponentProvider {
        ClassPathModelScanner(Class superClass) {
            super(false);
            super.addIncludeFilter(new AssignableTypeFilter(superClass));
        }
    }

    static class RepeatingRegistrar extends ModelScannerRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes mapperScansAttrs =
                    AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(ModelScans.class.getName()));
            String defaultBasePackage = getDefaultBasePackage(importingClassMetadata);
            if (mapperScansAttrs != null) {
                AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
                for (AnnotationAttributes annotation : annotations) {
                    super.register(annotation, defaultBasePackage);
                }
            }
        }
    }

}
