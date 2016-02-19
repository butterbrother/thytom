package com.github.butterbrother.thytom;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Осуществляет загрузку файла конфигурации.
 * Проверяет наличие необходимых параметров. Проверяет корректность файла конфигурации.
 * <p>
 * Вначале необходимо выполнить валидацию файла конфигурации с помощью {@link #validateConfigFile()}.
 * В процессе валидации файл будет загружен во временное хранилище.
 * <p>
 * Если валидация завершилась успешно, то можно распарсить файл конфигурации с помощью метода
 * {@link #parseConfigFile()}. Результат в виде {@link ConfigFile} можно в дальнейшем
 * использовать в приложении. Парсинг не требует повторного считывания из файла.
 * <p>
 * При неудачной валидации необходимо получить ошибку из {@link #getLastError()} и
 * аварийно завершить работу приложения.
 * <p>
 * Применяются следующие параметры:<br>
 * <table>
 * <thead>
 * <tr>
 * <th>Параметр</th>
 * <th>Описание</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>db.url</td>
 * <td>JDBC url. Обязательный параметр.</td>
 * </tr>
 * <tr>
 * <td>db.login</td>
 * <td>Учётная запись БД. Обязательный параметр. Можно указать пустую строку.</td>
 * </tr>
 * <tr>
 * <td>db.password</td>
 * <td>Пароль учётной записи БД. Обязательный параметр. Можно указать пустую строку.</td>
 * </tr>
 * <tr>
 *     <td>db.driver</td>
 *     <td>Драйвер JDBC БД. Обязательный параметр. Можно указать пустую строку.</td>
 * </tr>
 * <tr>
 * <td>file.sql.encoding</td>
 * <td>Кодировка файлов с SQL-запросами.</td>
 * </tr>
 * <tr>
 * <td>file.subs.encoding</td>
 * <td>Кодировка файлов с подстановками в SQL-запросы.</td>
 * </tr>
 * <tr>
 * <td>file.result.encoding</td>
 * <td>Кодировка файлов с результатами SQL-запросов</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class ConfigFileLoader {
    /**
     * Имя параметра для JDBC URL
     */
    public static final String PARAM_URL = "db.url";
    /**
     * Имя параметра для логина БД
     */
    public static final String PARAM_LOGIN = "db.login";
    /**
     * Имя параметра для пароля БД
     */
    public static final String PARAM_PASSWORD = "db.password";
    /**
     * Имя параметра для драйвера JDBC
     */
    public static final String PARAM_DRIVER = "db.driver";
    /**
     * Имя параметра кодировки файлов с SQL-запросами
     */
    public static final String PARAM_SQL_FILE_ENC = "file.sql.encoding";
    /**
     * Имя параметра кодировки файлов подстановок
     */
    public static final String PARAM_SUBS_FILE_ENC = "file.subs.encoding";
    /**
     * Имя параметра кодировки файлов с результатами запросов
     */
    public static final String PARAM_RESULT_FILE_ENC = "file.result.encoding";
    /**
     * Кодировка по-умолчанию
     */
    public static final String PARAM_DEFAULT_ENC = "UTF-8";

    private String lastError = "";
    private Path configFilePath;
    private Properties rawProperties = new Properties();

    /**
     * Инициализация
     */
    public ConfigFileLoader() {
        try {
            // Мегакостыль для обхода слешей к начале полного пути при отдаче URI под Windows
            // Вместо сбора из Paths.get(корень, conf, thytom.properties) выполняем поиск нашего конфига
            // Иначе вылетает URI exception, т.к. путь начинается с /C:/ и содержит %20 вместо пробелов
            // ну это лучше, чем определять тип ос и выносить символы вручную
            Path ownerRoot = Paths.get(ConfigFileLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

            boolean found = false;
            for (Path dir : Files.newDirectoryStream(ownerRoot, "*conf*")) {
                if (Files.isDirectory(dir))
                    for (Path file : Files.newDirectoryStream(dir, "*thytom.properties")) {
                        if (file.toString().contains("thytom.properties")) {
                            configFilePath = file;
                            found = true;
                            break;
                        }
                    }
            }

            if (!found)
                configFilePath = Paths.get("./conf/thytom.properties");
        } catch (URISyntaxException | IOException ignore) {
            configFilePath = Paths.get("./conf/thytom.properties");
        }
    }

    /**
     * Проверка файла конфигурации.
     * Так же файл конфигурации загружается в память в виде {@link java.util.Properties}.
     * Т.е. при парсинге не требуется повторное считывание.
     *
     * @return успех проверки файла конфигурации.
     * В случае неудачи можно получить текст ошибки, вызвав {@link #getLastError()}.
     */
    public boolean validateConfigFile() {
        if (Files.notExists(configFilePath)) {
            lastError = "Configuration file " + configFilePath.toString() + " not found.";
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(configFilePath, Charset.forName("UTF-8"))) {
            rawProperties = new Properties();
            rawProperties.load(reader);
        } catch (IOException e) {
            lastError = "Unable to read configuration file " + configFilePath.toString() + ", I/O error: " + e.getMessage();
            return false;
        }

        // Обязательные параметры, не могут быть null, но могут быть пустыми
        String url = rawProperties.getProperty(PARAM_URL);
        if (url == null) {
            lastError = "Database connection URL not set. " +
                    "Please set parameter \"" + PARAM_URL + "\" " +
                    "in file " + configFilePath.toString();
            return false;
        }
        String login = rawProperties.getProperty(PARAM_LOGIN);
        if (login == null) {
            lastError = "Database connection login not set. " +
                    "Please set parameter \"" + PARAM_LOGIN + "\" " +
                    "in file " + configFilePath.toString();
            return false;
        }
        String password = rawProperties.getProperty(PARAM_PASSWORD);
        if (password == null) {
            lastError = "Database connection password not set. " +
                    "Please set parameter \"" + PARAM_PASSWORD + "\" " +
                    "in file " + configFilePath.toString();
            return false;
        }
        String driver = rawProperties.getProperty(PARAM_DRIVER);
        if (driver == null) {
            lastError = "Database driver (JDBC driver) not set. " +
                    "Please set parameter \"" + PARAM_DRIVER + "\" " +
                    "in file " + configFilePath.toString();
            return false;
        }

        // Опциональный параметры. Они уже заданы по-умолчанию, но проверяем их валидность.
        // Это кодировки
        String[] encodingSettings = new String[]{
                PARAM_SQL_FILE_ENC,
                PARAM_SUBS_FILE_ENC,
                PARAM_RESULT_FILE_ENC
        };
        for (String param : encodingSettings)
            try {
                Charset.forName(rawProperties.getProperty(param, PARAM_DEFAULT_ENC));
            } catch (Exception e) {
                lastError = "Parameter \"" + PARAM_SQL_FILE_ENC
                        + "\" in file " + configFilePath.toString() + " not valid: " + e.getMessage();
                return false;
            }

        lastError = "";
        return true;
    }

    /**
     * Выполняет разбор файла конфигурации, преобразуя в готовые параметры.
     * Перед вызовом необходимо произвести валидацию с помощью {@link #validateConfigFile()}.
     * Этот метод не предполагает собственной валидации. Так же он не считывает файл повторно
     * (при валидации производится кеширование в {@link java.util.Properties}).
     *
     * @return разобранные параметры из файла конфигурации.
     */
    public ConfigFile parseConfigFile() {
        String url = rawProperties.getProperty(PARAM_URL);
        String login = rawProperties.getProperty(PARAM_LOGIN);
        String password = rawProperties.getProperty(PARAM_PASSWORD);
        String driver = rawProperties.getProperty(PARAM_DRIVER);
        Charset sqlFileCharset = Charset.forName(rawProperties.getProperty(PARAM_SQL_FILE_ENC, PARAM_DEFAULT_ENC));
        Charset resultsFileCharset = Charset.forName(rawProperties.getProperty(PARAM_RESULT_FILE_ENC, PARAM_DEFAULT_ENC));
        Charset substitutionsFileCharset = Charset.forName(rawProperties.getProperty(PARAM_SUBS_FILE_ENC, PARAM_DEFAULT_ENC));

        return new ConfigFile(
                url,
                login,
                password,
                driver,
                sqlFileCharset,
                resultsFileCharset,
                substitutionsFileCharset
        );
    }

    /**
     * Возвращает последнюю ошибку, полученную при
     * проверке файла конфигурации.
     *
     * @return последняя ошибка при проверке
     */
    public String getLastError() {
        return lastError;
    }
}
