package io.teamcode.config;

import com.jolbox.bonecp.BoneCPDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
	
	@Value("${datasource.url}")
    private String jdbcUrl;

    @Value("${datasource.username}")
    private String jdbcUsername;

    @Value("${datasource.password}")
    private String jdbcPassword;

    @Value("${datasource.driverClassName}")
    private String driverClass;

    @Value("${datasource.idleMaxAgeInMinutes}")
    private Integer idleMaxAgeInMinutes;

    @Value("${datasource.idleConnectionTestPeriodInMinutes}")
    private Integer idleConnectionTestPeriodInMinutes;

    @Value("${datasource.maxConnectionsPerPartition}")
    private Integer maxConnectionsPerPartition;

    @Value("${datasource.minConnectionsPerPartition}")
    private Integer minConnectionsPerPartition;

    @Value("${datasource.partitionCount}")
    private Integer partitionCount;

    @Value("${datasource.acquireIncrement}")
    private Integer acquireIncrement;

    @Value("${datasource.statementsCacheSize}")
    private Integer statementsCacheSize;
    
    @Value("${datasource.connectionTestStatement}")
    private String connectionTestStatement;
	
	@Bean(destroyMethod = "close")
    public DataSource dataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass(driverClass);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        dataSource.setIdleConnectionTestPeriodInMinutes(idleConnectionTestPeriodInMinutes);
        dataSource.setIdleMaxAgeInMinutes(idleMaxAgeInMinutes);
        dataSource.setMaxConnectionsPerPartition(maxConnectionsPerPartition);
        dataSource.setMinConnectionsPerPartition(minConnectionsPerPartition);
        dataSource.setPartitionCount(partitionCount);
        dataSource.setAcquireIncrement(acquireIncrement);
        dataSource.setStatementsCacheSize(statementsCacheSize);
        dataSource.setConnectionTestStatement(connectionTestStatement);
        
        return dataSource;
    }

}
