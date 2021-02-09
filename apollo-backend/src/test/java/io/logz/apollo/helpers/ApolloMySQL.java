package io.logz.apollo.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * Created by roiravhon on 11/23/16.
 */
public class ApolloMySQL {

    class ApolloMySQLContainer extends MySQLContainer{

        public ApolloMySQLContainer(String mySqlDockerImage) {
            super(mySqlDockerImage);
        }

        public String getDriverClassName() {
            return "org.mariadb.jdbc.Driver";
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ApolloMySQL.class);
    private final MySQLContainer mysql;

    public ApolloMySQL() throws SQLException, IOException, ScriptException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Create mysql instance
        logger.info("Starting MySQL container");
        mysql = new ApolloMySQLContainer("mysql:5.7.22");
        mysql.start();
    }

    public String getContainerIpAddress() {
        return mysql.getContainerIpAddress();
    }

    public int getMappedPort() {
        return mysql.getMappedPort(3306);
    }

    public String getUsername() {
        return mysql.getUsername();
    }

    public String getPassword() {
        return mysql.getPassword();
    }

    public String getSchema() {
        // Its hard-coded into test containers, without a getter
        return "test";
    }
}
