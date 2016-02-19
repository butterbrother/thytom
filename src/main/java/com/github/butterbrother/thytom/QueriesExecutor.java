package com.github.butterbrother.thytom;

import java.io.Closeable;
import java.sql.*;

/**
 * Выполняет переданные SQL-запросы, отдаёт результаты исполнения.
 */
public class QueriesExecutor implements AutoCloseable, Closeable {
    private Connection connection;
    private Statement statement;
    private ResultSet latestResultSet = null;

    /**
     * Инициализация и подключение к БД
     * @param cli       аргументы командной строки
     * @param config    параметры из файла конфигурации
     * @throws SQLException Ошибка при выполнении подключения к БД, либо ошибка инициализации драйвера.
     */
    public QueriesExecutor(CLIOptions cli, ConfigFile config) throws SQLException {

        // Регистрируем драйвер, если он указан
        if (! config.getDriver().isEmpty())
            try {
                DriverManager.registerDriver((Driver) Class.forName(config.getDriver()).newInstance());
            } catch (ClassNotFoundException e ) {
                throw new SQLException("Unable to load JDBC driver " + config.getDriver() + ".");
            } catch (InstantiationException | IllegalAccessException e) {
                throw new SQLException("Error loading JDBC driver: " + e.getMessage());
            }

        // Инициируем подключение в зависимости от того, указан ли логин и/или пароль, или нет
        if (! config.getLogin().isEmpty() || ! config.getPassword().isEmpty()) {
            connection = DriverManager.getConnection(config.getUrl(), config.getLogin(), config.getPassword());
        } else {
            connection = DriverManager.getConnection(config.getUrl());
        }

        connection.setAutoCommit(true);
        statement = connection.createStatement();
    }

    /**
     * Выполнение SQL-запроса.
     * Автоматически закрывается предыдущий результат (если он был).
     * т.е. нет необходимости закрывать ResultSet.
     * @param sqlQuery      SQL-запрос
     * @return              результат выполнения SQL-запроса. Если запрос обновляющий/не возвращающий результата,
     * то вернётся null.
     * @throws SQLException Ошибка выполнения запроса
     */
    public ResultSet execute(String sqlQuery) throws SQLException {
        closeLastResult();

        if (statement.execute(sqlQuery)) {
            latestResultSet = statement.getResultSet();
        } else {
            latestResultSet = null;
        }

        return latestResultSet;
    }

    /**
     * Проверка, что последний запрос возвращает результаты
     * @return  последний вызов {@link #execute(String)} имеет результаты.
     */
    public boolean hasResults() {
        return latestResultSet != null;
    }

    /**
     * Отключение от БД. При этом закрывается последний результат (если он был).
     */
    @Override
    public void close() {
        try {
            closeLastResult();
            statement.close();
            connection.close();
        } catch (SQLException ignore) {}
    }

    /**
     * Закрытие последнего результата.
     */
    private void closeLastResult() {
        if (latestResultSet != null)
            try {
                latestResultSet.close();
            } catch (SQLException ignore) {}
    }
}
