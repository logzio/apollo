package io.logz.apollo.di;

import io.logz.apollo.configuration.DatabaseConfiguration;
import io.logz.apollo.dao.BlockerDefinitionDao;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.DeploymentRoleDao;
import io.logz.apollo.dao.DeploymentPermissionDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.EnvironmentsStackDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.NotificationDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.dao.ServicesStackDao;
import io.logz.apollo.dao.SlaveDao;
import io.logz.apollo.dao.StackDao;
import io.logz.apollo.dao.UserDao;
import io.logz.apollo.database.DataSourceFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.flywaydb.core.Flyway;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.configuration.ConfigurationProvider;
import org.mybatis.guice.environment.EnvironmentProvider;
import org.mybatis.guice.session.SqlSessionFactoryProvider;
import org.mybatis.guice.session.SqlSessionManagerProvider;

import javax.sql.DataSource;

public class ApolloMyBatisModule extends MyBatisModule {

    private static final String JDBC_URL_FORMAT = "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true";

    private final DatabaseConfiguration configuration;

    private DataSource dataSource;

    public ApolloMyBatisModule(DatabaseConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void initialize() {
        // make sure database schema exists
        migrateDatabase();
        // create the data source and bind it
        createDataSource();
        init();

        addMapperClass(DeployableVersionDao.class);
        addMapperClass(DeploymentDao.class);
        addMapperClass(DeploymentRoleDao.class);
        addMapperClass(DeploymentPermissionDao.class);
        addMapperClass(EnvironmentDao.class);
        addMapperClass(ServiceDao.class);
        addMapperClass(UserDao.class);
        addMapperClass(GroupDao.class);
        addMapperClass(BlockerDefinitionDao.class);
        addMapperClass(NotificationDao.class);
        addMapperClass(EnvironmentsStackDao.class);
        addMapperClass(ServicesStackDao.class);
        addMapperClass(StackDao.class);
        addMapperClass(SlaveDao.class);
    }

    private void createDataSource() {
        this.dataSource = DataSourceFactory.create(configuration);
    }

    private void init() {
        environmentId("production");

        bind(DataSource.class).toInstance(dataSource);
        bindTransactionFactoryType(JdbcTransactionFactory.class);
        bind(SqlSessionManagerProvider.class);
        bind(EnvironmentProvider.class);
        bind(ConfigurationProvider.class);
        bind(SqlSessionFactoryProvider.class);

        mapUnderscoreToCamelCase(true);
    }

    private void migrateDatabase() {
        String jdbcUrl = String.format(JDBC_URL_FORMAT, configuration.getHost(), configuration.getPort(),
                configuration.getSchema());

        Flyway flyway = new Flyway();
        flyway.setOutOfOrder(true);
        flyway.setDataSource(jdbcUrl, configuration.getUser(), configuration.getPassword());
        flyway.setTable("schema_version");
        flyway.migrate();
    }

}
