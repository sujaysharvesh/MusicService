//package MusicService.example.MusicService.Music.DatabaseConfig;
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = "MusicCloud.example.MusicService.Music",
//        entityManagerFactoryRef = "musicEntityManagerFactory",
//        transactionManagerRef = "musicTransactionManager"
//)
//public class DatabaseConfig {
//
//    @Primary
//    @Bean(name = "musicDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.db")
//    public DataSource musicDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Primary
//    @Bean(name = "musicEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean musicEntityManagerFactory(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("musicDataSource") DataSource dataSource
//    ) {
//        return builder.dataSource(dataSource).packages("MusicCloud.example.MusicService.Music")
//                .persistenceUnit("music").build();
//    }
//
//    @Primary
//    @Bean(name = "musicTransactionManager")
//    public PlatformTransactionManager musicTransactionManager(
//            @Qualifier("musicEntityManagerFactory") EntityManagerFactory entityManagerFactory
//    ) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//
//
//
//}
//
//