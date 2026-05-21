package com.minden.entity;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

public class DatabaseConfig {

    public static DataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        String dbPath = "./data/game_db";
        
        // On Windows use User Home to prevent permission issues when installed in Program Files
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            dbPath = "~/rpgadmin/data/game_db";
        }
        
        ds.setURL("jdbc:h2:" + dbPath + ";DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    public static void runMigrations(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load();

        var result = flyway.migrate();
        System.out.printf(
            "Flyway: applied %d migrations, current version: %s%n",
            result.migrationsExecuted,
            result.targetSchemaVersion
        );
    }

    public static DataSource initialize() {
        DataSource ds = createDataSource();
        runMigrations(ds);
        return ds;
    }
}