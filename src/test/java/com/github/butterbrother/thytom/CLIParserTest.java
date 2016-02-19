package com.github.butterbrother.thytom;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Тестирование парсера аргументов командной строки.
 */
public class CLIParserTest {

    /**
     * Отсутствие аргументов командной строки - это корректно.
     * Не должна отображаться справка (т.к. нет ключа справки).
     * Все параметры должны быть по-умолчанию:
     * - файл подмен не используется
     * - разделитель столбцов - точка с запятой
     * - не отображаем заголовки на каждой строке в таблице с результатами
     * - не отображаем заголовок в таблице результатов
     * - не отображаем null текстом
     * - не выполняем trim
     */
    @Test
    public void testEmptyCmdline() {
        try {
            CLIParser parser = new CLIParser();
            boolean state = parser.validateCli();
            boolean needHelp = parser.cliHasHelpKey();
            org.junit.Assert.assertTrue("Empty cmdline must be valid", state);
            org.junit.Assert.assertFalse("Empty cmdline cannot be contain help argument", needHelp);

            CLIOptions options = parser.parseCLI();
            org.junit.Assert.assertFalse("substitutions file not be used", options.fileIsUsed());
            org.junit.Assert.assertEquals("columns delimiter must be semicolon", ";", options.getColumnDelimiter());
            org.junit.Assert.assertFalse("don't show headers per line in results", options.needShowHeadersPerLine());
            org.junit.Assert.assertFalse("don't show title headers", options.needShowTitleHeader());
            org.junit.Assert.assertFalse("don't show null as text", options.needShowNull());
            org.junit.Assert.assertFalse("don't trim data", options.needTrimResult());
        } catch (ParseException e) {
            org.junit.Assert.fail("Empty cmdline must be valid");
        }
    }

    /**
     * Проверка обнаружения аргумента отображения справки.
     */
    @Test
    public void testHelpDetection() {
        org.junit.Assert.assertTrue("If cmdline contain help argument, parser must be detect it",
                new CLIParser("-h").cliHasHelpKey());
    }

    /**
     * Фиксирование неизвестных агрументов командной строки
     */
    @Test
    public void testUnknownParameters() {
        CLIParser parser = new CLIParser("-z", "-h");
        org.junit.Assert.assertFalse("-z is unknown parameter, ", parser.validateCli());
        org.junit.Assert.assertNotSame("Error message must be exist, ", "", parser.getLastError());
    }

    /**
     * Проверка обнаружения агрумента - файла с подменами.
     * Проверяется поведение, если указанный файл существует и не существует.
     * Провряется, что возвращается строка с ошибкой, если файла не существует.
     */
    @Test
    public void testSubstitutionFile() throws IOException, ParseException {
        Path testFile = Paths.get("./test.txt");
        CLIParser parser = new CLIParser("-f", testFile.toString());

        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.append("Some").flush();
        }
        org.junit.Assert.assertTrue("File must be found and cmdline must be valid", parser.validateCli());

        CLIOptions options = parser.parseCLI();
        org.junit.Assert.assertEquals("File must be as is", testFile.toString(), options.getFilePath().toString());
        org.junit.Assert.assertTrue("We usage file, ", options.fileIsUsed());

        Files.delete(testFile);

        org.junit.Assert.assertFalse("If file is not exists - cmdline must be invalid", parser.validateCli());
        org.junit.Assert.assertNotEquals("Last error must be non-empty", "", parser.getLastError());
    }

    /**
     * Проверяется реакция на агрументы разделителей
     * Это:
     * - разделитель колонок в результатах -d
     * - разделитель данных и заголовков в режиме отображения заголовков -t
     * - разделитель колонок-шаблонов в файле с подстановками -l
     */
    @Test
    public void testDelimiters() {
        CLIParser parser = new CLIParser("-d", "||", "-t", "::", "-l", "][");
        try {
            CLIOptions options = parser.parseCLI();
            org.junit.Assert.assertEquals("Another columns delimiter in results, ", "||", options.getColumnDelimiter());
            org.junit.Assert.assertEquals("Another data and header separator, ", "::", options.getHeadDataDelimiter());
            org.junit.Assert.assertEquals("Another template delimiter, ", "][", options.getTemplatesDelimiter());
        } catch (ParseException e) {
            org.junit.Assert.fail("This cmdline must be valid");
        }
    }

    /**
     * Проверка работы переключателей.
     * - Отображение заголовков в шапке.
     * - Отображение заголовкой на каждой строке.
     * При этом должны отключаться заголовки в шапке.
     * - Использование разделителей в режиме отображения заголовков на каждой строке.
     * При этом должно включаться отображение заголовков на каждой строке и отключаться отображение в шапке.
     * - Использование собственных шаблонов в файле с подстановками.
     * - Указание собственных разделителей колонок-шаблонов в файле с подстановками.
     * При этом должно включиться использование собственных шаблонов.
     * - Отображение null текстом вместо пустой строки
     * - trim для результатов
     * - trim для подстановок
     */
    @Test
    public void testSwitches() {
        try {
            CLIParser parser = new CLIParser("-s");
            CLIOptions options = parser.parseCLI();
            org.junit.Assert.assertTrue("we show title headers with single argument, ", options.needShowTitleHeader());

            parser = new CLIParser("-s", "-e");
            options = parser.parseCLI();
            org.junit.Assert.assertTrue("we show headers each result line", options.needShowHeadersPerLine());
            org.junit.Assert.assertFalse("and dont show title", options.needShowTitleHeader());

            parser = new CLIParser("-s", "-t", "::");
            options = parser.parseCLI();
            org.junit.Assert.assertTrue("we show headers each result line, if set alternate separator, ", options.needShowHeadersPerLine());
            org.junit.Assert.assertFalse("and dont show title", options.needShowTitleHeader());

            parser = new CLIParser("-a");
            options = parser.parseCLI();
            org.junit.Assert.assertTrue("we use custom substitution templates, ", options.needUseCustomTemplates());

            parser = new CLIParser("-l", "::");
            options = parser.parseCLI();
            org.junit.Assert.assertTrue("we use custom templates, if set custom templates, ", options.needUseCustomTemplates());

            parser = new CLIParser("-n", "-w", "-c");
            options = parser.parseCLI();
            org.junit.Assert.assertTrue("we show null as text", options.needShowNull());
            org.junit.Assert.assertTrue("we trim results", options.needTrimResult());
            org.junit.Assert.assertTrue("we trim substitutions", options.needTrimSubstitution());
        } catch (ParseException e) {
            org.junit.Assert.fail("This cmdline must be valid");
        }
    }
}
