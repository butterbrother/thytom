package com.github.butterbrother.thytom;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Проверка работы загрузчика SQL-файлов
 */
public class SQLFilesLoaderTest {
    public static final String sqlDirPath = "./sql/";
    public static final Path sqlDir = Paths.get(sqlDirPath);

    /**
     * Удаляет каталог с файлами SQL
     *
     * @throws IOException
     */
    public void clearSqlDir() throws IOException {
        if (Files.exists(sqlDir)) {
            if (Files.isDirectory(sqlDir)) {
                for (Path file : Files.newDirectoryStream(sqlDir))
                    if (Files.isRegularFile(file))
                        Files.delete(file);
            }
            Files.delete(sqlDir);
        }
    }

    /**
     * Проверка срабатывания функции наличия файлов с запросами
     */
    @Test
    public void testExistFile() throws IOException {
        clearSqlDir();
        Files.createDirectory(sqlDir);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(sqlDirPath, "test.sql"))) {
            writer.append("Some");
        }

        SQLFilesLoader loader = new SQLFilesLoader(StandardCharsets.UTF_8);
        org.junit.Assert.assertTrue("Check must be return that has queries", loader.hasQueries());

        clearSqlDir();
    }

    /**
     * Проверка срабатывания функции наличия файлов с запросами.
     * В данном случае файлов нет.
     *
     * @throws IOException
     */
    @Test
    public void testNoExistFile() throws IOException {
        clearSqlDir();
        SQLFilesLoader loader = new SQLFilesLoader(StandardCharsets.UTF_8);
        org.junit.Assert.assertFalse("Check must be return that has no queries", loader.hasQueries());
    }

    /**
     * Проверка соответствия содержимому, файл в с запросом одной строкой
     */
    @Test
    public void testLoadDataOneLine() throws IOException {
        boolean non_except = false;
        int count = 0;
        while (!non_except) {
            try {
                clearSqlDir();
                Files.createDirectory(sqlDir);
                String query1 = "select 1 from dual";
                String name1 = "1.sql";
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(sqlDirPath + name1), StandardCharsets.UTF_8)) {
                    writer.append(query1).append('\n');
                }

                SQLFilesLoader loader = new SQLFilesLoader(StandardCharsets.UTF_8);
                org.junit.Assert.assertTrue("Check must be return that has queries", loader.hasQueries());
                SQLFile[] sqlFiles = loader.getSQLFiles();

                org.junit.Assert.assertEquals("request must be as is", query1 + '\n', sqlFiles[0].getQuery(null));
                org.junit.Assert.assertEquals("file must be as is", name1, sqlFiles[0].getFileName());

                clearSqlDir();
            } catch (IOException e) {
                count++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                if (count > 10)
                    throw new IOException(e);
            }
            non_except = true;
        }
    }

    /**
     * Проверка соответствию содержимому. Файл в несколько строк.
     */
    @Test
    public void testLoadDataMultiline() throws IOException {
        boolean non_except = false;
        int count = 0;
        while (!non_except) {
            try {
                clearSqlDir();
                Files.createDirectory(sqlDir);

                String query2 = "select\n1\nfrom\ndual";
                String name2 = "1.sql";
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(sqlDirPath + name2), StandardCharsets.UTF_8)) {
                    writer.append(query2).append('\n');
                }

                SQLFilesLoader loader = new SQLFilesLoader(StandardCharsets.UTF_8);
                org.junit.Assert.assertTrue("Check must be return that has queries", loader.hasQueries());
                SQLFile[] sqlFiles = loader.getSQLFiles();

                org.junit.Assert.assertEquals("request must be as is", query2 + '\n', sqlFiles[0].getQuery(null));
                org.junit.Assert.assertEquals("file must be as is", name2, sqlFiles[0].getFileName());

                clearSqlDir();
            } catch (IOException e) {
                count++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                if (count > 10)
                    throw new IOException(e);
            }
            non_except = true;
        }
    }
}
