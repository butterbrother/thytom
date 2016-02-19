package com.github.butterbrother.thytom;

import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Точка запуска приложения.
 * <p/>
 * Процедурное исполнение, разбитое на этапы.
 * <p/>
 * Вначале происходит считывание и проверка параметров командной строки
 * с момощью {@link CLIParser} в экземпляр {@link CLIOptions}.
 * <p/>
 * Далее происходит считывание и проверка файла конфигурации с помощью
 * {@link ConfigFileLoader} в экземпляр {@link ConfigFile}.
 * <p/>
 * После этого вполняется загрузка библиотек из ./lib.
 * <p/>
 * При помощи {@link SQLFile} и {@link SQLFilesLoader} последовательно
 * загружаются SQL-файлы. При наличии файла подстановок из него
 * извлекаются подстановки для каждого SQL-запроса. Файл подстановок
 * обрабатывается {@link SubsFileLoader}.
 * <p/>
 * Каждый получившийся запрос исполняется в {@link QueriesExecutor}.
 * Если есть результат, то он сохраняется с помощью {@link ResultSaver} в файлы,
 * расположенные в ./out.
 */
public class StartHere {
    /**
     * Успешное завершение работы
     */
    public static final int EXIT_NORMAL = 0;

    /**
     * Неверные параметры командной строки либо файла конфигурации
     */
    public static final int EXIT_ERR_PARAM = 1;

    /**
     * Какая-либо неопределённая ошибка в приложении
     */
    public static final int EXIT_INTERNAL_ERR = 2;

    public static void main(String args[]) {
        bootstrap(args);
    }

    /**
     * Начальная проверка параметров
     *
     * @param args Параметры командной строки
     */
    private static void bootstrap(String... args) {
        CLIParser parser = new CLIParser(args);

        if (parser.cliHasHelpKey()) {
            parser.showHelpUsage();
            System.exit(EXIT_NORMAL);
        }

        if (!parser.validateCli()) {
            System.err.println(parser.getLastError());
            parser.showHelpUsage();
            System.exit(EXIT_ERR_PARAM);
        }

        ConfigFileLoader loader = new ConfigFileLoader();
        if (!loader.validateConfigFile()) {
            System.err.println(loader.getLastError());
            System.exit(EXIT_ERR_PARAM);
        }
        ConfigFile config = loader.parseConfigFile();

        try {
            CLIOptions cli = parser.parseCLI();

            loadAllLibs();

            prepare(cli, config);
        } catch (ParseException pe) {
            System.err.println(pe.getMessage());
            parser.showHelpUsage();
            System.exit(EXIT_ERR_PARAM);
        }
    }

    /**
     * Этап загрузки jar-библиотек - JDBC драйверов.
     * Приложение позволяет не указывать classpath, а подгружает jar
     * динамически с помощью {@link JarLoader}.
     */
    private static void loadAllLibs() {
        try {
            Path libFiles = null;
            try {
                Path parent = Paths.get(SQLFilesLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

                boolean found = false;
                for (Path dir : Files.newDirectoryStream(parent, "*lib*")) {
                    if (Files.isDirectory(dir)) {
                        libFiles = dir;
                        found = true;
                        break;
                    }
                }

                if (!found)
                    libFiles = Paths.get("./lib/");
            } catch (URISyntaxException | IOException e) {
                libFiles = Paths.get("./lib/");
            }

            new JarLoader().loadAll(libFiles);
        } catch (Exception e) {
            System.err.println("Unable to load libraries/JDBC drivers from \"lib\":" + e.getMessage());
            System.exit(EXIT_INTERNAL_ERR);
        }
    }

    /**
     * Этап подготовки к подключению.
     * Загружается список SQL-файлов.
     * Загружается файл подстановок (если указан).
     *
     * @param cli       Параметры из командной строки
     * @param config    Параметры из файла конфигурации
     */
    public static void prepare(CLIOptions cli, ConfigFile config) {
        SQLFilesLoader sqlFilesLoader = new SQLFilesLoader(config.getSqlFileCharset());

        try {
            if (!sqlFilesLoader.hasQueries()) {
                System.err.println("No sql queries found in \"sql\" directory");
                System.exit(EXIT_NORMAL);
            }
        } catch (IOException e) {
            System.err.println("Unable to load sql queries from \"sql\" directory: " + e.getMessage());
            System.exit(EXIT_INTERNAL_ERR);
        }

        SQLFile[] sqlFiles = null;
        try {
            sqlFiles = sqlFilesLoader.getSQLFiles();
        } catch (IOException sqlReadError) {
            System.err.println("Error while fetching sql queries from *.sql files: " + sqlReadError.getMessage());
            System.exit(EXIT_INTERNAL_ERR);
        }

        SubsFileLoader subsLoader = null;
        if (cli.fileIsUsed())
            try {
                subsLoader = new SubsFileLoader(cli, config);
            } catch (IOException subsFileLoadError) {
                System.out.println("Unable to open substitutions file: " + subsFileLoadError.getMessage());
                System.exit(EXIT_ERR_PARAM);
            }

        runtime(cli, config, sqlFiles, subsLoader);
    }

    /**
     * Обработка запросов.
     * Запросы исполняются и передаются далее в
     * {@link #executeAndSave(CLIOptions, ConfigFile, QueriesExecutor, String, String, String)}
     * для сохранения результатов в файл.
     * @param cli           Параметры, полученные из командной строки
     * @param config        Параметры из файла конфигурации
     * @param sqlFiles      Список SQL-файлов
     * @param subs          Файл с подменами. Может быть null.
     */
    public static void runtime(CLIOptions cli,
                               ConfigFile config,
                               SQLFile[] sqlFiles,
                               SubsFileLoader subs) {
        System.err.println("Connecting to " + config.getUrl());
        try (QueriesExecutor executor = new QueriesExecutor(cli, config)) {

            int current = 0;

            for (SQLFile sqlFile : sqlFiles) {
                System.err.println("Processing file " + sqlFile.getFileName() + " [" + (++current) + "/" + sqlFiles.length + "]...");

                try {
                    sqlFile.getQuery(null); // Первое обращение к этому методу загружает sql-файл в память
                } catch (IOException queryLoadErr) {
                    System.err.println("Unable to load sql query from file "
                            + sqlFile.getFileName()
                            + ": " + queryLoadErr.getMessage());
                    continue;
                }

                if (cli.fileIsUsed() && subs != null) {
                    try {
                        for (Map<String, String> sub; (sub = subs.next()) != null; ) {
                            System.err.print("[" + subs.getRowID() + "]");

                            try {
                                executeAndSave(
                                        cli, config,
                                        executor,
                                        sqlFile.getQuery(sub),
                                        sqlFile.getFileName(),
                                        subs.getRowID()
                                );
                            } catch (IOException somethingWrong) {
                                System.err.println("Something wrong. Please contact to developers.");
                                somethingWrong.printStackTrace();
                                System.exit(EXIT_INTERNAL_ERR);
                            }

                        }
                    } catch (IOException subsReadErr) {
                        System.err.println("Error while reading substitution file: "
                                + subsReadErr.getMessage());
                        System.exit(EXIT_INTERNAL_ERR);
                    }

                    System.err.println();

                } else {

                    try {
                        executeAndSave(
                                cli, config,
                                executor,
                                sqlFile.getQuery(null),
                                sqlFile.getFileName(),
                                "out"
                        );
                    } catch (IOException somethingWrong) {
                        System.err.println("Something wrong. Please contact to developers.");
                        somethingWrong.printStackTrace();
                        System.exit(EXIT_INTERNAL_ERR);
                    }
                }

                sqlFile.close(); // Освобождаем строку с SQL-запросом
            }
        } catch (SQLException sqlE) {
            System.err.println("Connection error: " + sqlE.getMessage());

        }
    }

    /**
     * Исполнение одиночного запроса. С подстановкой либо без.
     *
     * @param cli           Параметры командной строки
     * @param config        Параметры из файла конфигурации
     * @param executor      Исполнитель SQL-запросов
     * @param query         Запрос, считанный из файла.
     * @param sqlFileName   Имя sql-файла
     * @param subsRowID     id записи из файла подстановки.
     */
    private static void executeAndSave(
            CLIOptions cli, ConfigFile config,
            QueriesExecutor executor,
            String query, String sqlFileName,
            String subsRowID
    ) {
        try {
            ResultSet results = executor.execute(query);
            if (executor.hasResults()) {

                ResultSaver resultSaver = null;
                try {
                    resultSaver = new ResultSaver(cli, config,
                            sqlFileName, subsRowID);
                    resultSaver.writeResults(results);
                } catch (IOException saveError) {
                    if (resultSaver != null)
                        System.err.println("Unable write to result file "
                                + resultSaver.getFileName().toString()
                                + ": " + saveError.getMessage());
                    else
                        System.err.println("Unable to create result file: "
                                + saveError.getMessage());
                } catch (SQLException fetchError) {
                    System.err.println("Unable fetch data from query result: " +
                            fetchError.getMessage());
                }
            }
        } catch (SQLException execError) {
            System.err.println("Unable to execute query from file " +
                    sqlFileName + ": " + execError.getMessage());
        }
    }
}
