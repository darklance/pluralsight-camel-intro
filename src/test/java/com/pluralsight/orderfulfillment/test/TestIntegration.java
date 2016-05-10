package com.pluralsight.orderfulfillment.test;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by bsmith on 5/3/2016.
 */
@Configuration
@Profile("derby")
@ComponentScan(basePackages = "com.pluralsight.orderfulfillment.order")
@PropertySource("classpath:derby.properties")
public class TestIntegration {


    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        dataSource.setUrl("jdbc:derby:memory:orders;create=true");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource());
        return jdbc;
    }

    @Bean(initMethod = "create", destroyMethod = "destroy")
    public DerbyDatabaseBean derbyDatabaseBean() {
        DerbyDatabaseBean derbyDatabaseBean = new DerbyDatabaseBean();
        derbyDatabaseBean.setJdbcTemplate(jdbcTemplate());
        return derbyDatabaseBean;
    }
}
