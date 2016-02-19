package com.github.butterbrother.thytom;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Тестирование загрузчика подстановок в SQL-запросы
 * из файла подстановок
 */
public class SubsFileLoaderTest {
    public static final String fileName = "./subs.txt";
    public static final Path file = Paths.get(fileName);
    public static final Charset charset = StandardCharsets.UTF_8;
    /**
     * Файл с шаблонами по-умолчанию
     */
    public static final String[] simpleFile = {
            "one",
            "two",
            "some text",
            "text with end space ",
            "",
            " test with delim; and spaces "
    };
    /**
     * Файл с собственными шаблонами
     */
    public static final String[] customTemplatesFile = {
            "HEAD1;HEAD2",
            "one;two",
            "only one",
            "only one with delimiter;",
            "",
            " with double spaces ; some",
            "large;list;great;that;templates;size"
    };

    /**
     * Используемый файл конфигурации для теста. С кодировкой - UTF-8.
     */
    public static final ConfigFile config = new ConfigFile("", "", "", "", charset, charset, charset);
    /**
     * Параметры для командной строки - с шаблоном по-умолчанию, без trim
     */
    public static final CLIOptions cliWoCustAndWoTrim = new CLIOptions(true, file, false, false, ":", ";", true, false, false, ";", false);
    /**
     * Параметры для командной строки - с шаблоном по-умолчанию, с trim
     */
    public static final CLIOptions cliWoCustAndWithTrim = new CLIOptions(true, file, false, false, ":", ";", true, false, true, ";", false);
    /**
     * Параметры командной строки - с собственными шаблонами, без trim
     */
    public static final CLIOptions cliWithCustomAndWoTrim = new CLIOptions(true, file, false, false, ":", ";", true, false, false, ";", true);
    /**
     * Параметры командной строки - с собственными шаблонами, с trim
     */
    public static final CLIOptions cliWithCustomAndWithTrim = new CLIOptions(true, file, false, false, ":", ";", true, false, true, ";", true);

    /**
     * Проверяется корректность обработки файла с шаблоном
     * по-умолчанию. Без trim
     */
    @Test
    public void testSimpleFile() throws IOException, NoSuchAlgorithmException {
        createTestFile(simpleFile);
        List<Map<String, String>> sameAs = new LinkedList<>();
        for (String item : simpleFile) {
            if (!item.isEmpty()) {
                Map<String, String> tst = new HashMap<>();
                tst.put("{PARAM}", item);
                sameAs.add(tst);
            }
        }

        checkSame(sameAs, cliWoCustAndWoTrim);
    }

    /**
     * Проверяется корректность обработки файла с шаблоном
     * по-умолчанию. С trim
     */
    @Test
    public void testSimpleFileWithTrim() throws IOException, NoSuchAlgorithmException {
        createTestFile(simpleFile);
        List<Map<String, String>> sameAs = new LinkedList<>();
        for (String item : simpleFile) {
            if (!item.isEmpty()) {
                Map<String, String> tst = new HashMap<>();
                tst.put("{PARAM}", item.trim());
                sameAs.add(tst);
            }
        }

        checkSame(sameAs, cliWoCustAndWithTrim);
    }

    /**
     * Проверка корректности обработки файла с собственными
     * шаблонами. Без trim.
     * Hashmap не гарантирует порядок элементов, поэтому тест может завалиться.
     * Хотя тут всего по 2 элемента.
     *
     * @throws IOException  I/O ошибка
     */
    @Test
    public void testCustomFile() throws IOException, NoSuchAlgorithmException {
        createTestFile(customTemplatesFile);
        List<Map<String, String>> sameAs = new LinkedList<>();

        Map<String, String> normal = new Hashtable<>();
        normal.put("{HEAD1}", "one");
        normal.put("{HEAD2}", "two");
        sameAs.add(normal);

        Map<String, String> onlyHead1 = new Hashtable<>();
        onlyHead1.put("{HEAD1}", "only one");
        sameAs.add(onlyHead1);

        Map<String, String> withEmptyHead2 = new Hashtable<>();
        withEmptyHead2.put("{HEAD1}", "only one with delimiter");
        //withEmptyHead2.put("{HEAD2}", "");
        sameAs.add(withEmptyHead2);

        Map<String, String> withSpaces = new Hashtable<>();
        withSpaces.put("{HEAD1}", " with double spaces ");
        withSpaces.put("{HEAD2}", " some");
        sameAs.add(withSpaces);

        Map<String, String> largeList = new Hashtable<>();
        largeList.put("{HEAD1}", "large");
        largeList.put("{HEAD2}", "list");
        sameAs.add(largeList);

        checkSame(sameAs, cliWithCustomAndWoTrim);
    }

    /**
     * Проверка корректности обработки файла с собственными
     * шаблонами. C trim.
     * Hashmap не гарантирует порядок элементов, поэтому тест может завалиться.
     * Хотя тут всего по 2 элемента.
     *
     * @throws IOException  I/O ошибка
     */
    @Test
    public void testCustomFileWithTrim() throws IOException, NoSuchAlgorithmException {
        createTestFile(customTemplatesFile);
        List<Map<String, String>> sameAs = new LinkedList<>();

        Map<String, String> normal = new Hashtable<>();
        normal.put("{HEAD1}", "one");
        normal.put("{HEAD2}", "two");
        sameAs.add(normal);

        Map<String, String> onlyHead1 = new Hashtable<>();
        onlyHead1.put("{HEAD1}", "only one");
        sameAs.add(onlyHead1);

        Map<String, String> withEmptyHead2 = new Hashtable<>();
        withEmptyHead2.put("{HEAD1}", "only one with delimiter");
        //withEmptyHead2.put("{HEAD2}", "");
        sameAs.add(withEmptyHead2);

        Map<String, String> withSpaces = new Hashtable<>();
        withSpaces.put("{HEAD1}", "with double spaces");
        withSpaces.put("{HEAD2}", "some");
        sameAs.add(withSpaces);

        Map<String, String> largeList = new Hashtable<>();
        largeList.put("{HEAD1}", "large");
        largeList.put("{HEAD2}", "list");
        sameAs.add(largeList);

        checkSame(sameAs, cliWithCustomAndWithTrim);
    }

    /**
     * Проверка, что считанные данные соответствуют ожидаемым.
     *
     * @param sameAs  Ожидаемые данные
     * @param options параметры командной строки, которые будут переданы считывателю файла подмен
     * @throws IOException
     */
    private void checkSame(List<Map<String, String>> sameAs, CLIOptions options) throws IOException, NoSuchAlgorithmException {
        // Эта лютая конструкция используется для ожидания освобождения файла
        // под windows. Иначе есть неиллюзорный шанс поймать AccessDeniedException
        boolean non_except = false;
        int count = 0;
        while (!non_except) {
            try {
                SubsFileLoader loader = new SubsFileLoader(options, config);
                List<Map<String, String>> readied = new LinkedList<>();
                Map<String, String> item;
                while ((item = loader.next()) != null) {
                    readied.add(item);
                }

                org.junit.Assert.assertEquals("Must be same as", sameAs, readied);

                // А теперь строковое стравнение
                byte[] sameAsMD5 = calculateMD5(sameAs);
                byte[] readiedMD5 = calculateMD5(readied);
                org.junit.Assert.assertTrue("Must be same as", MessageDigest.isEqual(sameAsMD5, readiedMD5));

                destroyFile();
            } catch (IOException e) {
                count++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                if (count > 10)
                    throw new IOException(e);
            }
            non_except = true;
        }
    }

    /**
     * Рассчитывает MD5 для шаблонов подстановок.
     * @param large     Шаблоны для подстановок.
     * @return          md5
     * @throws NoSuchAlgorithmException на всякий случай
     */
    private byte[] calculateMD5(List<Map<String,String>> large) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        for (Map<String, String> map : large)
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    digest.update(entry.getKey().getBytes());
                    digest.update(entry.getValue().getBytes());
                }
        byte[] value = Arrays.copyOf(digest.digest(), digest.digest().length);
        digest.reset();
        return value;
    }

    /**
     * Создаёт файл с подменами, с шаблонами по-умолчанию
     *
     * @throws IOException I/O error
     */
    public void createTestFile(String[] testData) throws IOException {
        // Здесь тоже ожидаем освобождения.
        boolean non_except = false;
        int count = 0;
        while (!non_except) {
            try {
                destroyFile();
                try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
                    for (String item : testData)
                        writer.append(item).append('\n');
                }
            } catch (IOException e) {
                count++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                if (count > 10)
                    throw new IOException(e);
            }
            non_except = true;
        }
    }

    /**
     * Удаляет файл с подменами.
     *
     * @throws IOException I/O error
     */
    public void destroyFile() throws IOException {
        Files.deleteIfExists(file);
    }
}
