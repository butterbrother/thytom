package com.github.butterbrother.thytom;

import org.apache.commons.cli.*;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Парсит аргументы командной строки.
 * Проверяет их валидность.
 * Отображает справку.
 * <p>
 * Вначале необходимо удостовериться, что среди аргументов
 * командной строки имеется ключ справки. Для этого вызывается
 * {@link #cliHasHelpKey()}.
 * Если ключ справки указан, то дальнейшая валидация прекращается,
 * нужно вызвать {@link #showHelpUsage()} и завершить работу приложения.
 * <p>
 * Далее необходимо произвести валидацию
 * аргументов командной строки, вызвав {@link #validateCli()}.
 * В случае неудачной валидации необходимо получить и вывести ошибку
 * валидации, вызвав {@link #getLastError()}.
 * Так же необходимо после этого отобразить справку, вызвав
 * {@link #showHelpUsage()}. После этого работа приложения аварийно завершается.
 * <p>
 * Если валидация прошла успешно, можно вызывать {@link #parseCLI()}.
 * <p>
 * Используются следующие ключи командной строки:<br>
 * <table>
 * <thead>
 * <tr>
 * <th>Агрумент</th>
 * <th>Длинный аргумент</th>
 * <th>Описание</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>-h</td>
 * <td>--help</td>
 * <td>Отобразить справку</td>
 * </tr>
 * <tr>
 * <td>-d</td>
 * <td>--col-delim</td>
 * <td>Альтернативный разделитель колонок в результате. По-умолчанию используется точка с запятой</td>
 * </tr>
 * <tr>
 * <td>-s</td>
 * <td>--title-head</td>
 * <td>
 * Отображать заголовок таблицы в результате. Отключается, если указать отображение заголовка
 * на каждой строке
 * </td>
 * </tr>
 * <tr>
 * <td>-e</td>
 * <td>--head-per-line</td>
 * <td>
 * Отображение заголовка на каждой строке. Т.е. вместо таблицы:<br>
 * столбец1;столбец2;столбец3<br>
 * AAA;BBB;CCC<br>
 * DDD;EEE;FFF<br><br>
 * отображается таблица вида:<br>
 * столбец1:AAA;столбец2:BBB;столбец3:CCC<br>
 * столбец1:DDD;столбец2:EEE;столбец3:FFF<br><br>
 * Данный параметр автоматически выключает отображение заголовка таблицы, -s/--title-head.<br>
 * По-умолчанию используется разделитель - двоеточие. Но его можно изменить через -t/--per-line-sep
 * </td>
 * </tr>
 * <tr>
 * <td>-t</td>
 * <td>--per-line-sep</td>
 * <td>
 * Использовать иной разделитель данных и заголовков в режиме отображения заголовка на каждой строке<br>
 * Указание этого параметра автоматически активирует отображение заголовка на каждой строке и
 * деактивирует отображение заголовка таблицы.
 * </td>
 * </tr>
 * <tr>
 * <td>-f</td>
 * <td>--subs-file</td>
 * <td>
 * Использование подстановок в SQL-запросы из файла.<br>
 * Предполагается, что в запросах есть шаблон {PARAM}. Например:<br>
 * select * from data where some="{PARAM}"<br><br>
 * Вместо этого шаблона будет по-очереди
 * подставляться значение из файла подстановок.<br>
 * Возможно использование собственных шаблонов при указании -a/--cust-templ<br>
 * </td>
 * </tr>
 * <tr>
 * <td>-a</td>
 * <td>--cust-templ</td>
 * <td>
 * Использование собственного шаблона вместо стандартного {PARAM}.<br>
 * В этом режиме имя шаблона указывается в первой строке в файле.<br>Возможно одновременное
 * применение нескольких шаблонов. Тогда входной файл должен представлять из себя
 * таблицу, где шаблоны указаны в шапке данной таблицы<br>
 * По-умолчанию разделитель в такой таблице входного файла - точка с запятой. Он меняется
 * через параметр -l/--templ-sep<br>
 * Параметр бесполезен без указания файла (игнорируется).
 * </td>
 * </tr>
 * <tr>
 * <td>-l</td>
 * <td>--templ-sep</td>
 * <td>
 * Использование альтернативного разделителя столбцов при использовании собственных
 * шаблонов, -a/--cust-templ.<br>
 * Параметр автоматически активирует использование собственных шаблонов, -a/--cust-templ.
 * </td>
 * </tr>
 * <tr>
 * <td>-n</td>
 * <td>--nulls</td>
 * <td>
 * Прописывать null текстом в результатах, вместо использования пустой строки.
 * </td>
 * </tr>
 * <tr>
 * <td>-w</td>
 * <td>--trim-data</td>
 * <td>
 * Удалять лишние пробелы в каждой ячейке результатов. Используется {@link String#trim()}.
 * </td>
 * </tr>
 * <tr>
 * <td>-c</td>
 * <td>--trim-subs</td>
 * <td>
 * Удалять лишние пробелы в каждой подстановке из файла подстановок.
 * Используется {@link String#trim()}.
 * </td>
 * </tr>
 * </tbody>
 * </table>
 */
public class CLIParser {
    private String[] cli;
    private String ownJarName;

    private Options options = new Options();
    private Options onlyHelp = new Options();
    private CommandLineParser parser = new DefaultParser();

    // Доступные ключи:
    // ключ вызова справки
    private Option help;
    // ключ, указывающий на файл с подменами в запросах
    private Option substitutionFile;
    // разделитель столбцов в результатах запросов
    private Option delimiter;
    // отображать заголовок столбцов в шапке таблицы с результатами
    private Option showHeader;
    // отображать заголовок на каждой строке перед данными вместо шапки
    private Option headersPerLine;
    // разделитель данных и заголовка, если включено отображение на каждой строке
    private Option headerDataDelimiter;
    // использовать заголовки в файле с подменами в качестве собственных шаблонов
    private Option customSubstitutionTemplate;
    // разделитель в файле с подменами в случае, если используются собственные заголовки
    private Option customSubstitutionDelim;
    // выводить null текстом
    private Option showNull;
    // удалять пробелы до и после в результатах
    private Option trimResulst;
    // удалять пробелы до и после в данных для подмены
    private Option trimSubstitutions;

    // последняя ошибка при валидации аргументов командной строки
    private String lastError = "";

    /**
     * Инициализация парсера
     *
     * @param args аргументы командной строки из функции запуска
     */
    public CLIParser(String... args) {
        cli = args;
        help = Option.builder("h")
                .longOpt("help")
                .desc("Show this help.")
                .build();
        options.addOption(help);
        onlyHelp.addOption(help);

        delimiter = Option.builder("d")
                .longOpt("col-delim")
                .hasArg()
                .argName("delimiter")
                .desc("Alternative separator of columns in results of requests. " +
                        "The semicolon is by default used.")
                .build();
        options.addOption(delimiter);

        showHeader = Option.builder("s")
                .longOpt("title-head")
                .desc("Display headers in the results of queries. " +
                        "By default, headings are not displayed.")
                .build();
        options.addOption(showHeader);

        headersPerLine = Option.builder("e")
                .longOpt("head-per-line")
                .desc("Show headers on each line.\n" +
                        "i.e. instead of one general header:\n" +
                        "col1;col2;col3\n" +
                        "AAA;BBB;CCC\n" +
                        "DDD;EEE;FFF\n" +
                        "\n" +
                        "each row header will be displayed in front of each field:\n" +
                        "col1:AAA;col2:BBB;col3:CCC\n" +
                        "col1:DDD;col2:EEE;col3:FFF\n" +
                        "\n" +
                        "This parameter automatically disable \"" + showHeader.getLongOpt() + "\".")
                .build();
        options.addOption(headersPerLine);

        headerDataDelimiter = Option.builder("t")
                .longOpt("per-line-sep")
                .hasArg()
                .argName("delimiter")
                .desc("Alternate separator of headers and fields, if switch on" +
                        "show headers on each line. " +
                        "The colon is by default used.\n" +
                        "This parameter automatically activate \"" + headersPerLine.getLongOpt() + "\".")
                .build();
        options.addOption(headerDataDelimiter);

        substitutionFile = Option.builder("f")
                .longOpt("subs-file")
                .hasArg()
                .argName("file name")
                .desc("File with the data for substitution into SQL queries. " +
                        "Each parameter from this file will be in turn inserted" +
                        " into the SQL query and executed if in request there is" +
                        " a template {PARAM}.\n" +
                        "For sample file:\n" +
                        "AAA\n" +
                        "BBB\n" +
                        "\n" +
                        "SQL query:\n" +
                        "select * from data where owner={PARAM}\n" +
                        "If the file is not set, but there is a template {PARAM} in queries, " +
                        "then an empty string will be substituted for the {PARAM} (or another" +
                        "custom) template.")
                .build();
        options.addOption(substitutionFile);

        customSubstitutionTemplate = Option.builder("a")
                .longOpt("cust-templ")
                .desc("Alternative template for the substitution in the parameter file. " +
                        "It assumes that uses one or more of their own, instead of a" +
                        " basic template {PARAM}. " +
                        "In this case templates - column headings in the file" +
                        " with substitutions (and file - is table).\n" +
                        "For sample template file:\n" +
                        "col1;col2\n" +
                        "AAA;BBB\n" +
                        "\n" +
                        "Sql query:\n" +
                        "Select * from data where owner={col1} and key={col2}\n" +
                        "This parameter depend of \"" + substitutionFile.getLongOpt() + "\". " +
                        "And is not activated, if it is not specified.")
                .build();
        options.addOption(customSubstitutionTemplate);

        customSubstitutionDelim = Option.builder("l")
                .longOpt("templ-sep")
                .hasArg()
                .desc("Alternative separator of columns in the file with substitutions and" +
                        "custom substitution templates.\n" +
                        "The semicolon is used by default.\n" +
                        "This parameter automatically activate \"" +
                        substitutionFile.getLongOpt() + "\" and \"" + customSubstitutionTemplate.getLongOpt() + "\". " +
                        "And of course it depend of \"" + substitutionFile.getLongOpt() + "\".")
                .build();
        options.addOption(customSubstitutionDelim);

        showNull = Option.builder("n")
                .longOpt("nulls")
                .desc("Show null as \"null\" in results.\n" +
                        "By default show empty string.")
                .build();
        options.addOption(showNull);

        trimResulst = Option.builder("w")
                .longOpt("trim-data")
                .desc("Remove the spaces before and after values for " +
                        "each cell results (trim).")
                .build();
        options.addOption(trimResulst);

        trimSubstitutions = Option.builder("c")
                .longOpt("trim-subs")
                .desc("Remove the spaces before and after values of the " +
                        "parameters in the file substitution (trim)")
                .build();
        options.addOption(trimSubstitutions);

        try {
            Path jarPath = Paths.get(CLIParser.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path jarName = jarPath.getNameCount() > 1 ? jarPath.getName(jarPath.getNameCount() - 1) : jarPath;
            ownJarName = jarName.toString();
        } catch (URISyntaxException ignore) {
            ownJarName = "thytom.jar";
        }
    }

    /**
     * Выполняет проверку аргументов командной строки.
     *
     * @return true - аргументы валидны, нет неизвестных аргументов
     * false - есть невалидные либо неизвестные аргументы. Ошибку
     * можно получить из {@link #getLastError()}
     */
    public boolean validateCli() {
        try {
            CommandLine cmdLine = parser.parse(options, cli);

            // Проверяем, что параметр для аргумента файла с подстановками
            // есть и такой файл существует.
            if (cmdLine.hasOption(substitutionFile.getOpt())) {
                String file = cmdLine.getOptionValue(substitutionFile.getOpt());
                if (file == null) {
                    lastError = "Substitution file not set.";
                    return false;
                }

                Path subFile = Paths.get(file);
                if (Files.notExists(subFile)) {
                    lastError = "Substitution file " + file + " not found.";
                    return false;
                }
            }

            lastError = "";
            return true;

        } catch (ParseException e) {
            lastError = e.getMessage();
        }

        return false;
    }

    /**
     * Выполняет разбор аргументов командной строки.
     * Подразумевается, что до этого аргументы были проверены
     * с помощью {@link #validateCli()}. В противном случае
     * работа без ошибок не гарантируется (данная функция не
     * вызывает {@link #validateCli()}).
     *
     * @return Разобранные аргументы командной строки,
     * пригодные для дальнейшего использования в приложении.
     * @throws ParseException ошибка при разборе параметров
     */
    public CLIOptions parseCLI() throws ParseException {
        CommandLine cmdLine = parser.parse(options, cli);

        // Наличие файла подстановки и имя файла подстановки
        boolean hasFile = cmdLine.hasOption(substitutionFile.getOpt());
        Path file;
        if (hasFile) {
            file = Paths.get(cmdLine.getOptionValue(substitutionFile.getOpt(), "./"));
        } else {
            file = Paths.get("./");
        }

        // Использовать собственный разделитель данных
        boolean hasCustomHeaderDataDelimiter = cmdLine.hasOption(headerDataDelimiter.getOpt());
        // разделитель данных и заголовков в режиме отображения на каждой строке
        String headDataDelimiter = hasCustomHeaderDataDelimiter ?
                cmdLine.getOptionValue(headerDataDelimiter.getOpt(), ":")
                : ":";
        // Показывать заголовки вместе с данными на каждой строке
        // Если используем собственный разделитель, то показываем
        boolean showHeadersPerLine =
                hasCustomHeaderDataDelimiter || cmdLine.hasOption(headersPerLine.getOpt());

        // Отображение заголовков в начале таблицы с результатами
        // запросов. Не отображаем, если включено отображение вместе с данными
        boolean showTitleHeader = !showHeadersPerLine && cmdLine.hasOption(showHeader.getOpt());

        // Разделитель колонок в таблице с результатами
        String columnsDelimiter = cmdLine.hasOption(delimiter.getOpt()) ?
                cmdLine.getOptionValue(delimiter.getOpt(), ";") :
                ";";

        // Показывать null как текст, если он есть в результате.
        // По-умолчанию отображается пустая строка
        boolean showNullAsText = cmdLine.hasOption(showNull.getOpt());

        // Применять trim к ячейкам в результатах
        boolean trimCellDataResults = cmdLine.hasOption(trimResulst.getOpt());

        // Применять trim к данным подстановки в запросах
        boolean trimSubstitutionData = cmdLine.hasOption(trimSubstitutions.getOpt());

        // Использовать собственный разделитель колонок в файле с
        // подстановками. Подразумевается, что используем несколько шаблонов вместо
        // стандартного
        boolean hasCustomTemplatesDelimiter = cmdLine.hasOption(customSubstitutionDelim.getOpt());
        // Собственный разделитель колонок
        String templatesDelimiter = hasCustomTemplatesDelimiter ?
                cmdLine.getOptionValue(customSubstitutionDelim.getOpt(), ";") : ";";
        // Использовать собственные шаблоны вместо стандартного
        boolean useCustomSubstitutionTemplates =
                hasCustomTemplatesDelimiter
                        || cmdLine.hasOption(customSubstitutionTemplate.getOpt());

        return new CLIOptions(
                hasFile,
                file,
                showHeadersPerLine,
                showTitleHeader,
                headDataDelimiter,
                columnsDelimiter,
                showNullAsText,
                trimCellDataResults,
                trimSubstitutionData,
                templatesDelimiter,
                useCustomSubstitutionTemplates
        );
    }

    /**
     * Возвращает последнюю ошибку, найденную при валидации
     * аргументов командной строки.
     * Вызывается, если {@link #validateCli()} либо {@link #cliHasHelpKey()}
     * вернули false.
     *
     * @return Последняя ошибка. Если ошибки не было, то
     * поле должно быть пустым
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Поиск ключа показа справки среди аргументов командной строки.
     *
     * @return наличие ключа справки
     */
    public boolean cliHasHelpKey() {
        try {
            CommandLine cmdLine = parser.parse(onlyHelp, cli);
            if (cmdLine.hasOption(help.getOpt())) {
                lastError = "";
                return true;
            }
        } catch (ParseException e) {
            lastError = e.getMessage();
        }

        return false;
    }

    /**
     * Вывод справки
     */
    public void showHelpUsage() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(ownJarName, "\nJDBC-client for bulk export data.\n\n", options,
                "\n\nPlease report issues at <o.bobukh@yandex.ru>", true);
    }
}
