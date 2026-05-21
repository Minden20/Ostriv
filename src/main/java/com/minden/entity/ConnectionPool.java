package com.minden.entity;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.sql.DataSource;

public class ConnectionPool {

    private final BlockingQueue<Connection> pool;
    private final DataSource dataSource;

    public ConnectionPool(int poolSize) throws SQLException {
        // ЗМІНА 1: Викликаємо initialize(), щоб Flyway автоматично створив таблиці!
        this.dataSource = DatabaseConfig.initialize(); 
        this.pool = new ArrayBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            pool.add(dataSource.getConnection());
        }
    }

    public Connection getConnection() throws InterruptedException {
        Connection realConnection = pool.take();
        
        // ЗМІНА 2: Створюємо Proxy, який "дурить" try-with-resources.
        // Коли репозиторій каже connection.close(), ми робим releaseConnection().
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        releaseConnection(realConnection);
                        return null; // Нічого не повертаємо
                    }
                    return method.invoke(realConnection, args);
                }
        );
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            pool.offer(connection);
        }
    }
}