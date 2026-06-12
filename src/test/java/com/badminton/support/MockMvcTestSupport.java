package com.badminton.support;

import com.badminton.exception.GlobalExceptionHandler;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public final class MockMvcTestSupport {

    private MockMvcTestSupport() {
    }

    public static MockMvc buildMockMvc(Object controller) {
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json().build());

        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .setCustomArgumentResolvers(pageableResolver)
                .setValidator(validator)
                .build();
    }
}
