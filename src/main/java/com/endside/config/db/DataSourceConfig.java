package com.endside.config.db;

import com.endside.config.ssh.SshTunneling;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;
@Slf4j
@Configuration
public class DataSourceConfig {
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    private final SshTunneling tunnel;

    public DataSourceConfig(SshTunneling tunnel) {
        this.tunnel = tunnel;
    }

    @Value("${ssh.use:false}")
    private Boolean isUse = false;

    private void sshTunnelingInit(){
        if (!tunnel.init()){
            System.exit(0);
        }
    }

    @PreDestroy
    public void end() {
        try {
            if(tunnel != null) {
                tunnel.shutdown();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @FlywayDataSource
    public DataSource routeDataSource() {
        if(isUse) {
            sshTunnelingInit();
        }
        return new RoutingDataSource() {{
            setDefaultTargetDataSource(writeDataSource());
            setTargetDataSources(new HashMap<>() {{
                put("write", writeDataSource());
                put("read", readDataSource());
            }});
        }};
    }

    @Bean
    public LazyConnectionDataSourceProxy lazyConnectionDataSourceProxy() {
        return new LazyConnectionDataSourceProxy(routeDataSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(lazyConnectionDataSourceProxy());
        entityManagerFactoryBean.setPackagesToScan("com.endside");
        HibernateJpaVendorAdapter  vendorAdapter = new HibernateJpaVendorAdapter();
        String profile = System.getProperty("spring.profiles.active");
        if (!"prod".equals(profile)) {
            vendorAdapter.setShowSql(true);
        }

        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        Properties properties = new Properties();
        properties.put("hibernate.jdbc.time_zone", "Asia/Seoul");
        properties.put("hibernate.physical_naming_strategy", new CamelCaseToUnderscoresNamingStrategy());
        properties.put("hibernate.implicit_naming_strategy", new SpringImplicitNamingStrategy());
        entityManagerFactoryBean.setJpaProperties(properties);
        return entityManagerFactoryBean;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }
}
