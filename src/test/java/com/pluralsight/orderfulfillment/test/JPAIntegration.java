package com.pluralsight.orderfulfillment.test;

import com.pluralsight.orderfulfillment.config.DataConfig;
import com.pluralsight.orderfulfillment.config.IntegrationConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by bsmith on 5/6/2016.
 */
@Configuration
@ComponentScan(basePackages = "com.pluralsight.orderfulfillment")
@PropertySource("classpath:order-fulfillment.properties")
@ContextConfiguration(classes = {DataConfig.class, IntegrationConfig.class})
public class JPAIntegration {
}
