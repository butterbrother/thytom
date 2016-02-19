package com.github.butterbrother.thytom;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Осуществляет загрузку SQL-запросов из директории. При условии, что они есть, конечно.
 * Перед итерацией необходимо проверить, что запросы существуют, вызвав
 * {@link #hasQueries()}. Иначе может вернуться пустой список файлов.
 */
public class SQLFilesLoader {
    private Path sqlFilesPath;
    private Charset filesEncoding;

    /**
     * Инициализация
     *
     * @param encoding кодировка файлов с запросами. Параметр загружается
     *                 из файла конфигурации. См. в {@link ConfigFileLoader}.
     */
    public SQLFilesLoader(Charset encoding) {
        this.filesEncoding = encoding;
        try {
            Path parent = Paths.get(SQLFilesLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

            boolean found = false;
            for (Path dir : Files.newDirectoryStream(parent, "*sql*")) {
                if (Files.isDirectory(dir)) {
                    sqlFilesPath = dir;
                    found = true;
                    break;
                }
            }

            if (!found)
                sqlFilesPath = Paths.get("./sql/");
        } catch (URISyntaxException | IOException e) {
            sqlFilesPath = Paths.get("./sql/");
        }
    }

    /**
     * Проверка, есть ли каталог с SQL-запросами.
     * И есть ли в нём сами .sql-файлы.
     *
     * @return результат проверки.
     */
    public boolean hasQueries() throws IOException {
        if (Files.exists(sqlFilesPath) && Files.isDirectory(sqlFilesPath)) {
            int count = 0;
            for (Path query : Files.newDirectoryStream(sqlFilesPath)) {
                if (Files.isRegularFile(query) && query.toString().toLowerCase().endsWith(".sql"))
                    count++;
            }

            return count > 0;
        } else
            return false;
    }

    /**
     * Вызов итератора SQL файлов.
     * В момент итерации повторное считывание списка файлов не производится.
     *
     * @return итератор со списком SQL-файлов
     */
    public SQLFile[] getSQLFiles() throws IOException {
        List<SQLFile> sqlFiles = new ArrayList<>();
        for (Path query : Files.newDirectoryStream(sqlFilesPath)) {
            if (Files.isRegularFile(query) && query.toString().toLowerCase().endsWith(".sql"))
                sqlFiles.add(new SQLFile(query, filesEncoding));
        }

        return sqlFiles.toArray(new SQLFile[sqlFiles.size()]);
    }
}
