package com.github.butterbrother.thytom;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Проверка исполнителя SQL-запросов.
 */
public class QueriesExecutorTest {

    @Test
    public void testExec() throws IOException, ParseException, SQLException {
        CLIOptions cli = new CLIParser().parseCLI();
        ConfigFile config = new ConfigFile("jdbc:sqlite::memory:", "", "", "org.sqlite.JDBC", null, null, null);
        try (QueriesExecutor executor = new QueriesExecutor(cli, config)) {
            executor.execute("CREATE TABLE `test_table` (\n" +
                    "\t`id`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t`number`\tINTEGER NOT NULL,\n" +
                    "\t`text`\tTEXT\n" +
                    ");");
            executor.execute("insert into test_table (number, text) values (123, \"test\");");
            org.junit.Assert.assertFalse("insert must not have results", executor.hasResults());
            executor.execute("insert into test_table (number, text) values (121, \" duals \");");
            org.junit.Assert.assertFalse("insert must not have results", executor.hasResults());
            executor.execute("insert into test_table (number, text) values (121, \" some\");");
            org.junit.Assert.assertFalse("insert must not have results", executor.hasResults());
            executor.execute("insert into test_table (number, text) values (121, null);");
            org.junit.Assert.assertFalse("insert must not have results", executor.hasResults());

            ResultSet resultSet = executor.execute("select * from test_table");
            org.junit.Assert.assertTrue(executor.hasResults());
            org.junit.Assert.assertTrue(resultSet.next());
        }
    }
}
