package com.github.butterbrother.thytom;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Тестирование загрузки параметров из файла конфигурации.
 */
public class ConfigFileLoaderTest {

    /**
     * Проверка работы с отсутствием файла настройки.
     * Валидация должна завершиться неудачно.
     */
    @Test
    public void testNoFile() {
        ConfigFileLoader loader = new ConfigFileLoader();
        org.junit.Assert.assertFalse("detection that config file not exists, ", loader.validateConfigFile());
        org.junit.Assert.assertNotEquals("message error must be non-empty", "", loader.getLastError());
    }

    /**
     * Проверка, что загрузчик параметров умеет находить файл конфигурации относительно
     * собственного пути к jar.
     * <p>
     * Проверка, что создание и работа ConfigFile производится корректно.
     * Проверка передачи локали по-умолчанию
     * <p>
     * Проверка, что для парсинга файла конфигурации не требуется повторное считывание из файла.
     * <p>
     * В основном классе используется getCodeSource, при тестировании jar не собирается, поэтому вместо
     * пути jar-файла возвращается корневое расположение классов. В итоге загрузчик будет искать
     * конфиг в ../conf/. Это стоит учитывать здесь и в дальнейших тестах
     */
    @Test
    public void testCorrectFile() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test1");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test2");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test3");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            boolean result = loader.validateConfigFile();
            String message = loader.getLastError();
            org.junit.Assert.assertEquals("last error must be empty, ", "", message);
            org.junit.Assert.assertTrue("config must be valid, " + message + ", ", result);
            destroyConfig();

            ConfigFile configFile = loader.parseConfigFile();
            org.junit.Assert.assertEquals("url must be as is", "test1", configFile.getUrl());
            org.junit.Assert.assertEquals("login must be as is", "test2", configFile.getLogin());
            org.junit.Assert.assertEquals("password must be as is", "test3", configFile.getPassword());
            org.junit.Assert.assertEquals("driver must be as is", "", configFile.getDriver());

            org.junit.Assert.assertEquals("default result file encoding is utf-8", Charset.forName("UTF-8"), configFile.getResultsFileCharset());
            org.junit.Assert.assertEquals("default sql file encoding is utf-8", Charset.forName("UTF-8"), configFile.getSqlFileCharset());
            org.junit.Assert.assertEquals("default substitutions file encoding is utf-8", Charset.forName("UTF-8"), configFile.getSubstitutionsFileCharset());
        } catch (IOException e) {
            e.printStackTrace();
            org.junit.Assert.fail(e.toString());
        }
    }

    /**
     * Создаёт файл конфигурации с заданными параметрами
     *
     * @throws IOException I/O
     */
    public void createConfig(Properties properties) throws IOException {
        destroyConfig();

        Files.createDirectories(Paths.get("conf"));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("conf/thytom.properties"), StandardCharsets.UTF_8)) {
            properties.store(writer, "Test suite config file");
        }
    }

    /**
     * Удаляет пустой файл конфигурации
     *
     * @throws IOException I/O
     */
    public void destroyConfig() throws IOException {
        if (Files.exists(Paths.get("conf/thytom.properties"))) {
            Files.delete(Paths.get("conf/thytom.properties"));
            Files.delete(Paths.get("conf"));
        }
    }

    /**
     * Проверка на отсутствие параметра url в файле конфигурации
     */
    @Test
    public void testUrlEmpty() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - url not set", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    /**
     * Проверка на отсутствие параметра login в файле конфигурации
     */
    @Test
    public void testLoginEmpty() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - login not set", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    /**
     * Проверка на отсутствие параметра password в файле конфигурации
     */
    @Test
    public void testPasswordEmpty() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - password not set", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    /**
     * Проверка на отсутствие параметра driver в файле конфигурации
     */
    @Test
    public void testDriverEmpty() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - driver not set", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }


    /**
     * Проверка на указание некорректной кодировки файлов SQL-запросов
     */
    @Test
    public void testSqlIncorrectEncoding() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");
            properties.put(ConfigFileLoader.PARAM_SQL_FILE_ENC, "SOME");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - sql files encoding is not valid", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    /**
     * Проверка на указание некорректной кодировки файлов результатов
     */
    @Test
    public void testResultIncorrectEncoding() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");
            properties.put(ConfigFileLoader.PARAM_RESULT_FILE_ENC, "SOME");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - results file encoding is not valid", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.toString());
        }
    }

    /**
     * Проверка на указание некорректной кодировки файлов с подменами
     */
    @Test
    public void testSubstitutionsIncorrectEncoding() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");
            properties.put(ConfigFileLoader.PARAM_SUBS_FILE_ENC, "SOME");
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertFalse("invalid config - substitutions file encoding invalid", loader.validateConfigFile());
            org.junit.Assert.assertNotEquals("last error must be non-empty", "", loader.getLastError());

            destroyConfig();
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    /**
     * Проверка на корректность передачи кодировки из параметров
     */
    @Test
    public void testEncodingParameters() {
        try {
            Properties properties = new Properties();
            properties.put(ConfigFileLoader.PARAM_URL, "test");
            properties.put(ConfigFileLoader.PARAM_LOGIN, "test");
            properties.put(ConfigFileLoader.PARAM_PASSWORD, "test");
            properties.put(ConfigFileLoader.PARAM_DRIVER, "test");

            properties.put(ConfigFileLoader.PARAM_SUBS_FILE_ENC, StandardCharsets.UTF_16.displayName());
            properties.put(ConfigFileLoader.PARAM_RESULT_FILE_ENC, StandardCharsets.US_ASCII.displayName());
            properties.put(ConfigFileLoader.PARAM_SQL_FILE_ENC, StandardCharsets.ISO_8859_1.displayName());
            createConfig(properties);

            ConfigFileLoader loader = new ConfigFileLoader();
            org.junit.Assert.assertTrue("this config is valid", loader.validateConfigFile());

            destroyConfig();

            ConfigFile file = loader.parseConfigFile();
            org.junit.Assert.assertEquals("substitutions file must be utf-16", StandardCharsets.UTF_16, file.getSubstitutionsFileCharset());
            org.junit.Assert.assertEquals("results file must me ascii", StandardCharsets.US_ASCII, file.getResultsFileCharset());
            org.junit.Assert.assertEquals("sql file must be iso", StandardCharsets.ISO_8859_1, file.getSqlFileCharset());
        } catch (IOException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }
}
