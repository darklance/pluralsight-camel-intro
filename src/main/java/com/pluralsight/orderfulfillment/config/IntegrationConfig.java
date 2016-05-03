package com.pluralsight.orderfulfillment.config;

import com.pluralsight.orderfulfillment.order.OrderStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.sql.DataSource;


@Configuration
public class IntegrationConfig extends CamelConfiguration {

    @Inject
    private Environment environment;

    @Inject
    private DataSource dataSource;

    @Bean
    public SqlComponent sql() {
        SqlComponent component = new SqlComponent();
        component.setDataSource(dataSource);
        return component;
    }

    @Bean
    public RouteBuilder newWebsiteOrderRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("sql:select id from orders.\"order\" where status = '" + OrderStatus.NEW.getCode()
                        + "'?consumer.onConsume=update orders.\"order\" set status = '" + OrderStatus.PROCESSING.getCode()
                        + "' where id = :#id")
                        .to("log:com.pluralsite.orderfulfillment.order?level=INFO");
            }
        };
    }
}
