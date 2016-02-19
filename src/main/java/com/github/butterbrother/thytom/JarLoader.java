package com.github.butterbrother.thytom;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Осуществляет подзагрузку библиотек после запуска приложения.
 */
public class JarLoader {
    private Method addURL;

    /**
     * Инициализация
     *
     * @throws Exception
     */
    public JarLoader() throws Exception {
        //URLClassLoader.addURL(URL url) - protected void
        addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
    }

    /**
     * Выполняет загрузку одиночной библиотеки
     *
     * @param library путь к библиотеке
     * @throws Exception ошибка загрузки
     */
    public void load(Path library) throws Exception {
        if (Files.exists(library) && library.toString().endsWith(".jar"))
            addURL.invoke(ClassLoader.getSystemClassLoader(), library.toUri().toURL());
    }

    /**
     * Выполняет загрузку всех библиотек из указанного каталога.
     *
     * @param librariesPath Каталог с библиотеками
     * @throws Exception ошибка при загрузке библиотек
     */
    public void loadAll(Path librariesPath) throws Exception {
        if (Files.exists(librariesPath) && Files.isDirectory(librariesPath))
            for (Path library : Files.newDirectoryStream(librariesPath, "*.jar")) {
                load(library);
            }
    }
}
