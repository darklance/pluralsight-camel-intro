package com.pluralsight.orderfulfillment.config;

import com.pluralsight.orderfulfillment.generated.FulfillmentCenter;
import com.pluralsight.orderfulfillment.order.OrderStatus;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.sql.DataSource;


@Configuration
public class IntegrationConfig extends CamelConfiguration {

    @Inject
    private Environment environment;

    @Inject
    private DataSource dataSource;


    @Bean
    public ConnectionFactory jmsConnectionFactory() {
        return new ActiveMQConnectionFactory(environment.getProperty("activemq.broker.url"));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory pooledConnectionFactory() {
        PooledConnectionFactory factory = new PooledConnectionFactory();
        factory.setConnectionFactory(jmsConnectionFactory());
        factory.setMaxConnections(Integer.parseInt(environment.getProperty("pooledConnectionFactory.maxConnections")));
        return factory;
    }

    @Bean
    public JmsConfiguration jmsConfiguration() {
        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        jmsConfiguration.setConnectionFactory(pooledConnectionFactory());
        return jmsConfiguration;
    }

    @Bean
    public ActiveMQComponent activeMq() {
        ActiveMQComponent activeMq = new ActiveMQComponent();
        activeMq.setConfiguration(jmsConfiguration());
        return activeMq;
    }


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
                        .beanRef("orderItemMessageTranslator", "transformToOrderItemMessage")
                        .to("activemq:queue:ORDER_ITEM_PROCESSING");
            }
        };
    }

    @Bean
    public RouteBuilder fulfillmentCenterContentBasedRouter() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                Namespaces namespace = new Namespaces("o", "http://www.pluralsight.com/orderfullfillment/Order");

                from("activemq:queue:ORDER_ITEM_PROCESSING")
                        .choice()
                        .when()
                        .xpath("/o:Order/o:OrderType/o:FulfillmentCenter = '" + FulfillmentCenter.ABC_FULFILLMENT_CENTER.value()
                                + "'", namespace)
                        .to("activemq:queue:ABC_FULFILLMENT_REQUEST")
                        .when()
                        .xpath("/o:Order/o:OrderType/o:FulfillmentCenter = '" + FulfillmentCenter.FULFILLMENT_CENTER_ONE.value()
                                + "'", namespace)
                        .to("activemq:queue:FC1_FULFILLMENT_REQUEST").otherwise().to("activemq:queue:ERROR_FULFILLMENT_REQUEST");
            }
        };
    }
}
