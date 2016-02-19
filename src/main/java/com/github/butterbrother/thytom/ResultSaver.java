package com.github.butterbrother.thytom;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Сохраняет результат вызова SQL-запроса в файл.
 * Если, конечно, есть результаты.
 */
public class ResultSaver {
    private boolean showTitle;
    private boolean headPerLine;
    private String columnDelimiter;
    private String headDataDelimiter;
    private boolean trimResults;
    private boolean showNull;
    private Charset fileEncoding;
    private Path fileName;

    /**
     * Символы, недопустимые в именах файлов большинства ОС.
     */
    public static final char[] incompatibleChars = {
            '<', '>', ':', '\"', '/', '\\', '|', '?', '*', '%'
    };

    /**
     * Инициализация.
     * @param cli           Параметры, переданные в командной строке
     * @param config        Параметры, полученные из файла конфигурации
     * @param SQLFileName   Имя SQL-файла. Можно получить из {@link SQLFile#getFileName()}
     * @param rowID         ID подмены из файла подмен. Можно получить из {@link SubsFileLoader#getRowID()}
     * @throws IOException  В процессе инициализации проверяется, существует ли каталог для сохранения
     * результатов. Это каталог out в том же каталоге, что и jar-файл. Если каталога нет, он будет создан.
     * Ошибка появляется при невозможности это сделать.
     */
    public ResultSaver(
            CLIOptions cli,
            ConfigFile config,
            String SQLFileName,
            String rowID
    ) throws IOException {
        this.showTitle = cli.needShowTitleHeader();
        this.headPerLine = cli.needShowHeadersPerLine();
        this.columnDelimiter = cli.getColumnDelimiter();
        this.headDataDelimiter = cli.getHeadDataDelimiter();
        this.trimResults = cli.needTrimResult();
        this.showNull = cli.needShowNull();
        this.fileEncoding = config.getResultsFileCharset();

        String Name = rowID + '_' + SQLFileName;
        Name = Name.replace(".sql", ".txt");

        for (char c : incompatibleChars) {
            Name = Name.replace(c, '_');
        }

        Path resultsPath;
        try {
            Path rootPath = Paths.get(ResultSaver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            resultsPath = Paths.get(rootPath.getParent().toString(), "out");
        } catch (URISyntaxException ignore) {
            resultsPath = Paths.get("./out/");
        }

        if (Files.notExists(resultsPath))
                Files.createDirectories(resultsPath);

        fileName = Paths.get(resultsPath.toString(), Name);
    }

    /**
     * Запись результатов запроса в файл.
     * Каждый вызов создаёт/пересоздаёт файл с результатами, поэтому
     * вызывается однократно.
     * @param results       Результаты запроса. ResultSet не закрывается данным методом
     *                      и должен быть закрыт извне
     * @throws SQLException Ошибка получения данных
     * @throws IOException  Ошибка ввода-вывода при сохранении файла
     */
    public void writeResults(ResultSet results) throws SQLException, IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileName, fileEncoding)) {
            ResultSetMetaData metaData = results.getMetaData();

            int columnsCount = metaData.getColumnCount();
            if (columnsCount > 0) {
                // Вначале извлекаем имена заголовков и список столбцов, которые необходимо заключать в кавычки
                String headers[] = new String[columnsCount+1];

                boolean stringData[] = new boolean[columnsCount+1];
                for (int i = 1; i <= columnsCount; i++) {
                    headers[i] = trimResults ? metaData.getColumnLabel(i).trim() : metaData.getColumnLabel(i);
                    switch (metaData.getColumnType(i)) {
                        case Types.LONGNVARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.NVARCHAR:
                        case Types.VARCHAR:
                        case Types.CLOB:
                        case Types.NCLOB:
                            stringData[i] = true;
                            break;
                        default: stringData[i] = false;
                    }
                }

                headers[0] = ""; stringData[0] = false;

                // И далее пишем результат
                if (showTitle) {
                    for (int i = 1; i <= columnsCount; i++) {
                        writer.append(headers[i]);
                        if (i < columnsCount)
                            writer.append(columnDelimiter);
                    }
                }

                boolean firstLine = ! showTitle;

                String cell;
                boolean nullCell;
                while (results.next()) {
                    if (firstLine) {
                            firstLine = false;
                        } else {
                            writer.newLine();
                        }

                    for (int i = 1; i <= columnsCount; i++) {
                        if (headPerLine)
                            writer.append(headers[i]).append(headDataDelimiter);

                        cell = results.getString(i);
                        nullCell = cell == null;
                        if (nullCell) {
                            cell = showNull ? "null" : "";
                        }

                        if (stringData[i] && !nullCell) writer.append('\"');

                        writer.append(trimResults ? cell.trim() : cell);

                        if (stringData[i] && !nullCell) writer.append('\"');

                        if (i < columnsCount)
                            writer.append(columnDelimiter);
                    }
                }
            }
        }
    }

    /**
     * Получение имени файла с результатами
     * @return  имя файла
     */
    public Path getFileName() {
        return fileName;
    }
}
