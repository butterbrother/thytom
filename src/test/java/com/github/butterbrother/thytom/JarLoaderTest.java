package com.github.butterbrother.thytom;

import org.junit.Test;

import java.nio.file.Paths;

/**
 * Тестирование загрузчика внешних библиотек
 */
public class JarLoaderTest {

    /**
     * Инициализация должна быть без исключений.
     * Если есть - значит указали неверное имя метода
     * или тип параметров для URLClassLoader.addURL()
     */
    @Test
    public void testInit() {
        try {
            new JarLoader();
        } catch (Exception e) {
            org.junit.Assert.fail("Unable to create JarLoader, "
                    + e.getMessage()
                    + ", " + e.getCause().getMessage());
        }
    }

    /**
     * При загрузке несуществующего файла не должно
     * возникать ошибки
     */
    @Test
    public void testLoadNonExistLib() {
        try {
            new JarLoader().load(Paths.get("./test.jar"));
            new JarLoader().loadAll(Paths.get("./test/"));
        } catch (Exception e) {
            org.junit.Assert.fail("JarLoader can't skip non-exists libraries");
        }
    }
}
