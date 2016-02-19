package com.github.butterbrother.thytom;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Осуществляет загрузку и обработку файла с подстановками.
 */
public class SubsFileLoader {
    public static final String DEFAULT_TEMPLATE = "{PARAM}";
    private Path file;
    private Charset charset;
    private boolean useCustomTemplates;
    private String[] customTemplates = null;
    private String delimiter;
    private boolean trim;
    private BufferedReader reader = null;
    private long rowNum = 0;
    private String uniqRecord = "";

    /**
     * Инициализация.
     *
     * @param options параметры, переданные из командной строки
     * @param file    параметры, считанные из файла настроек
     */
    public SubsFileLoader(CLIOptions options, ConfigFile file) throws IOException {
        this.file = options.getFilePath();
        this.charset = file.getSubstitutionsFileCharset();
        this.useCustomTemplates = options.needUseCustomTemplates();
        this.delimiter = options.getTemplatesDelimiter();
        this.trim = options.needTrimSubstitution();
        reset();
    }

    /**
     * Чтение из файла замен
     *
     * @return Строка из файла замен. Если файл был закрыт,
     * то вернётся null. Иначе вернётся результат, аналогичный
     * BufferedReader.readLine().
     * Пустые строки пропускаются.
     * @throws IOException Ошибка чтения из файла
     */
    private String readLine() throws IOException {
        if (reader != null) {
            //return reader.readLine();
            for (String buffer; (buffer = reader.readLine()) != null; ) {
                if (trim) buffer = buffer.trim();
                if (!buffer.isEmpty())
                    return buffer;
            }
        }

        return null;
    }

    /**
     * Инициализирует и открывает файл на чтение при первом обращении либо после сброса.
     * При использовании собственных шаблонов инициализирует их.
     *
     * @throws IOException ошибка открытия файла
     */
    private void reset() throws IOException {
        try {
            safeClose();
            reader = Files.newBufferedReader(file, charset);

            if (useCustomTemplates) {
                String templateHeader = reader.readLine();
                if (templateHeader == null) {
                    useCustomTemplates = false;
                } else {
                    StringTokenizer separator = new StringTokenizer(templateHeader, delimiter);
                    customTemplates = new String[separator.countTokens()];
                    int pos = 0;
                    while (separator.hasMoreTokens()) {
                        String rawTemplate = separator.nextToken();
                        customTemplates[pos] = "{" + (trim ? rawTemplate.trim() : rawTemplate) + "}";
                        pos++;
                    }
                }
            }

            rowNum = 0;
        } catch (IOException e) {
            safeClose();
            throw new IOException(e);
        }
    }

    /**
     * Закрывает файл и разрывает с ним связь.
     *
     * @throws IOException ошибка при закрытии.
     *                     При этом связь всё равно будет разорвана
     */
    private void safeClose() throws IOException {
        if (reader != null)
            try {
                reader.close();
            } finally {
                reader = null;
            }
    }

    /**
     * Извлечение следующей подстановки из файла подстановки.
     *
     * @return Карта с подстановками. Если достигнут конец файла,
     * то вернётся null.
     * @throws IOException
     */
    public Map<String, String> next() throws IOException {
        rowNum++;
        String rawSubLine = readLine();
        if (rawSubLine != null) {
            Map<String, String> substitutions = new Hashtable<>();

            if (useCustomTemplates && customTemplates != null) {
                StringTokenizer separator = new StringTokenizer(rawSubLine, delimiter);

                int pos = 0;
                while (separator.hasMoreTokens() && pos < customTemplates.length) {
                    String rawSubstitution = separator.nextToken();
                    substitutions.put(customTemplates[pos], trim ? rawSubstitution.trim() : rawSubstitution);

                    if (pos == 0) {
                        uniqRecord = rawSubstitution.trim();
                    }
                    pos++;
                }
            } else {
                substitutions.put(DEFAULT_TEMPLATE, trim ? rawSubLine.trim() : rawSubLine);

                uniqRecord = rawSubLine.trim();
            }

            return substitutions;
        } else {
            uniqRecord = "";
            return null;
        }
    }

    /**
     * Возвращает уникальный ID строки.
     * Данный ID необходим для сохранения результата в файл,
     * имя которого частично состоит из имени файла запроса,
     * и частично состоит из значения подстановки.
     * ID состовляется из номера строки, считанного из файла подстановки
     * (исключая пустые строки) и последнего значения подстановки.
     * Если используются собственные шаблоны, то это будет элемент подстановки
     * в первый шаблон (по факту - значение первого столбца в таблице файла подстановки).
     * Номер строки идёт по порядку, начиная с единицы.
     * <p>
     * ID не очищается от недопустимых символов для имени файла той или иной
     * операционной системы.
     *
     * @return  уникальный ID строки
     */
    public String getRowID() {
        return Long.toString(rowNum) + '_' + uniqRecord;
    }
}
