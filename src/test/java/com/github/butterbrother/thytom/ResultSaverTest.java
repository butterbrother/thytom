package com.github.butterbrother.thytom;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;

/**
 * Проверка сохранения результата из запроса
 */
public class ResultSaverTest {

    /**
     * Регистрация JDBC-драйвера SQLite.
     * Драйвер используется только для теста и не попадает в конечный jar.
     * @throws SQLException
     */
    private void registerDriver() throws SQLException {
        try {
            DriverManager.registerDriver((Driver)Class.forName("org.sqlite.JDBC").newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SQLException("Unable load SQLite JDBC driver", e);
        }
    }

    /**
     * Создаёт тестовую БД в памяти.
     * @return  Тестовая БД
     * @throws SQLException
     */
    private Connection getTestDBConnection() throws SQLException {
        registerDriver();

        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        // Создаём структуру и вносим данные, которые и будем извлекать.
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE `test_table` (\n" +
                    "\t`id`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t`number`\tINTEGER NOT NULL,\n" +
                    "\t`text`\tTEXT\n" +
                    ");");
            statement.executeUpdate("insert into test_table (number, text) values (123, \"test\");");
            statement.executeUpdate("insert into test_table (number, text) values (121, \" duals \");");
            statement.executeUpdate("insert into test_table (number, text) values (121, \" some\");");
            statement.executeUpdate("insert into test_table (number, text) values (121, null);");
        }
        return connection;
    }

    /**
     * Проверка, что в результат попадают заголовки.
     * Так же проверяется обработка некорректных символов при составлении имени файла результата.
     * Проверяется, что не вставляется лишних разделителей строки в файл.
     * Проверяется, что строковый тип данных оборачивается в кавычки.
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testShowHeaders() throws SQLException, IOException, ParseException {
        try (Connection connection = getTestDBConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select t.number, t.text\n" +
                    "from test_table t\n" +
                    "order by t.id")) {
                CLIOptions options = new CLIParser("-s").parseCLI();
                ConfigFile configFile = new ConfigFile("", "", "", "", StandardCharsets.UTF_8, StandardCharsets.UTF_8, StandardCharsets.UTF_8);
                ResultSaver saver = new ResultSaver(options, configFile, "<some>data*test.sql", "whdd?");
                Files.deleteIfExists(saver.getFileName());
                saver.writeResults(resultSet);

                try (BufferedReader reader = Files.newBufferedReader(saver.getFileName(), StandardCharsets.UTF_8)) {
                    org.junit.Assert.assertEquals("Headers must be same as", "number;text", reader.readLine());
                    org.junit.Assert.assertEquals("123;\"test\"", reader.readLine());
                    org.junit.Assert.assertEquals("121;\" duals \"", reader.readLine());
                    org.junit.Assert.assertEquals("121;\" some\"", reader.readLine());
                    org.junit.Assert.assertEquals("121;", reader.readLine());
                    org.junit.Assert.assertEquals(null, reader.readLine());
                }
                Files.deleteIfExists(saver.getFileName());
            }
        }
    }

    /**
     * Проверка отображения заголовков на каждой строке в таблице. С trim.
     * Так же проверяется, что считывается метка столбца, если она задана запросом.
     * Проверяется, что используются разделители данных и столбцов.
     * Проверка, что передаются параметры. И что null строкового типа не оборачивается кавычками.
     * @throws SQLException
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testShowPerLine() throws SQLException, IOException, ParseException {
        try (Connection connection = getTestDBConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select t.id as \"num\", t.number as \"uid\", t.text as \"string\"\n" +
                    "from test_table t\n" +
                    "order by t.id")) {
                CLIOptions options = new CLIParser("-e", "-d", "||", "-t", "#", "-n", "-w").parseCLI();
                ConfigFile configFile = new ConfigFile("", "", "", "", StandardCharsets.UTF_8, StandardCharsets.UTF_8, StandardCharsets.UTF_8);
                ResultSaver saver = new ResultSaver(options, configFile, "test", "test");
                Files.deleteIfExists(saver.getFileName());
                saver.writeResults(resultSet);

                try (BufferedReader reader = Files.newBufferedReader(saver.getFileName(), StandardCharsets.UTF_8)) {
                    org.junit.Assert.assertEquals("num#1||uid#123||string#\"test\"", reader.readLine());
                    org.junit.Assert.assertEquals("num#2||uid#121||string#\"duals\"", reader.readLine());
                    org.junit.Assert.assertEquals("num#3||uid#121||string#\"some\"", reader.readLine());
                    org.junit.Assert.assertEquals("num#4||uid#121||string#null", reader.readLine());
                    org.junit.Assert.assertEquals(null, reader.readLine());
                }

                Files.deleteIfExists(saver.getFileName());
            }
        }
    }
}
