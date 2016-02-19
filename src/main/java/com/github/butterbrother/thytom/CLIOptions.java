package com.github.butterbrother.thytom;

import java.nio.file.Path;

/**
 * Обработанные параметры командной строки,
 * пригодные к использованию в приложении в дальнейшем.
 * Создаются в {@link CLIParser}
 */
public class CLIOptions {
    private boolean useFile;
    private Path substitutionFile;

    private boolean showHeadersPerLine;
    private boolean showTitleHeader;
    private String headDataDelimiter;

    private String columnDelimiter;

    private boolean showNull;

    private boolean trimResult;
    private boolean trimSubstitution;

    private String templatesDelimiter;
    private boolean useCustomTemplates;

    /**
     * Инициализация.
     * Для получения обработанных параметров необходимо
     * использовать {@link CLIParser#parseCLI()}
     *
     * @param useFile            Использовать файл замен
     * @param substitutionFile   Путь к файлу замен
     * @param showHeadersPerLine Показывать заголовки на каждой строке перед данными
     * @param showTitleHeader    Показывать заголовок результатов в шапке таблицы
     * @param headDataDelimiter  Разделитель результатов и данных в режиме отображения
     *                           заголовка на каждой строке
     * @param columnDelimiter    разделитель колонок в таблице с результатами
     * @param showNull           Отображать null текстом, а не пустой строкой
     * @param trimResult         trim для ячеек в результатах
     * @param trimSubstitution   trim для входных подстановок для шаблонов
     * @param templatesDelimiter разделитель альтернативных шаблонов в файле
     *                           подстановок
     * @param useCustomTemplates использовать собственные шаблоны
     */
    protected CLIOptions(
            boolean useFile,
            Path substitutionFile,
            boolean showHeadersPerLine,
            boolean showTitleHeader,
            String headDataDelimiter,
            String columnDelimiter,
            boolean showNull,
            boolean trimResult,
            boolean trimSubstitution,
            String templatesDelimiter,
            boolean useCustomTemplates) {
        this.useFile = useFile;
        this.substitutionFile = substitutionFile;
        this.showHeadersPerLine = showHeadersPerLine;
        this.showTitleHeader = showTitleHeader;
        this.headDataDelimiter = headDataDelimiter;
        this.columnDelimiter = columnDelimiter;
        this.showNull = showNull;
        this.trimResult = trimResult;
        this.trimSubstitution = trimSubstitution;
        this.templatesDelimiter = templatesDelimiter;
        this.useCustomTemplates = useCustomTemplates;
    }

    /**
     * Используется файл для подстановок в SQL-запросы.
     *
     * @return true-используется
     */
    public boolean fileIsUsed() {
        return useFile;
    }

    /**
     * Получение пути к файлу с подстановками в SQL-запросы
     *
     * @return путь к файлу
     */
    public Path getFilePath() {
        return substitutionFile;
    }

    /**
     * Необходимость показывать заголовок на каждой строке с данными
     *
     * @return показывать заголовок
     */
    public boolean needShowHeadersPerLine() {
        return showHeadersPerLine;
    }

    /**
     * Показывать заголовок таблицы с результатами
     *
     * @return показывать заголовок таблицы
     */
    public boolean needShowTitleHeader() {
        return showTitleHeader;
    }

    /**
     * Разделитель заголовков и данных в режиме отображения заголовков на каждой
     * строке с данными
     *
     * @return разделитель
     */
    public String getHeadDataDelimiter() {
        return headDataDelimiter;
    }

    /**
     * Разделитель колонок в результатах запросов
     *
     * @return разделитель
     */
    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    /**
     * Необходимость указывать null прописью.
     * По-умолчанию указывается пустой строкой
     *
     * @return прописывать null текстом, если данные null
     */
    public boolean needShowNull() {
        return showNull;
    }

    /**
     * Срезать пробелы в начале и в конце каждой ячейки
     * в таблице с результатами выполнения запросов
     *
     * @return true - срезать
     */
    public boolean needTrimResult() {
        return trimResult;
    }

    /**
     * Срезать пробелы в начале и конце данных из файла для
     * подстановок в шаблоны SQL-запросов.
     *
     * @return true - срезать
     */
    public boolean needTrimSubstitution() {
        return trimSubstitution;
    }

    /**
     * Разделитель колонок в файле подстановок при использовании
     * собственных шаблонов
     *
     * @return разделитель
     */
    public String getTemplatesDelimiter() {
        return templatesDelimiter;
    }

    /**
     * Использование собственных шаблонов подстановок в SQL-запросы
     *
     * @return true - собственные шаблоны используются.
     * false - используется один шаблон по-умолчанию, {PARAM}
     */
    public boolean needUseCustomTemplates() {
        return useCustomTemplates;
    }
}
