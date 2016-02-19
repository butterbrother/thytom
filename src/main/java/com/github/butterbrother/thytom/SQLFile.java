package com.github.butterbrother.thytom;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Считывает запрос из SQL-файла.
 * Считывание производится при первом вызове {@link #getQuery(Map)}.
 * Выполняет подмену, если они указаны.
 */
public class SQLFile implements Closeable, AutoCloseable {
    private String originalQuery = null;
    private Path file;
    private Charset fileEncoding;

    /**
     * Инициализация.
     * При инициализации не считывается содержимое файла.
     *
     * @param file         Файл с запросом
     * @param fileEncoding Кодировка файла. См. {@link ConfigFileLoader#PARAM_SQL_FILE_ENC}.
     */
    protected SQLFile(Path file, Charset fileEncoding) {
        this.file = file;
        this.fileEncoding = fileEncoding;
    }

    /**
     * Возвращает имя файла запроса
     *
     * @return имя файла.
     */
    public String getFileName() {
        return file.getName(file.getNameCount() - 1).toString();
    }

    /**
     * Получение оригинального либо модифицированного подменами SQL-запроса.
     * Производит считывание из файла, если данный метод вызывается первый раз.
     *
     * @param substitutions Подмены. Может быть Null и пустым.
     *                      Ожидается, что шаблоны уже заключены в парные кавычки
     *                      и очищены при необходимости от лишних пробелов.
     * @return Запрос
     * @throws IOException Ошибка считывания запроса из файла при первом вызове.
     */
    public String getQuery(Map<String, String> substitutions) throws IOException {
        if (originalQuery == null) {
            StringBuilder rawQuery = new StringBuilder();

            try (BufferedReader reader = Files.newBufferedReader(file, fileEncoding)) {
                for (String buffer; (buffer = reader.readLine()) != null; ) {
                    rawQuery.append(buffer).append('\n');
                }
            }

            this.originalQuery = rawQuery.toString();
        }

        if (substitutions != null && substitutions.size() > 0) {
            String tmp = originalQuery;
            for (Map.Entry<String, String> rule : substitutions.entrySet()) {
                tmp = tmp.replace(rule.getKey(), rule.getValue());
            }
            return tmp;
        } else {
            return originalQuery;
        }
    }

    /**
     * Выполнение запроса завершено.
     * Если выполнить после данного метода {@link #getQuery(Map)},
     * то запрос будет считан заново
     */
    @Override
    public void close() {
        originalQuery = null;
    }
}
