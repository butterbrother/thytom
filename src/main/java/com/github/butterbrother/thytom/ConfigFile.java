package com.github.butterbrother.thytom;

import java.nio.charset.Charset;

/**
 * Параметры из файла конфигурации, пригодные для дальнейшего использования приложением.
 */
public class ConfigFile {
    private String url;
    private String login;
    private String password;
    private String driver;

    private Charset sqlFileCharset;
    private Charset resultsFileCharset;
    private Charset substitutionsFileCharset;

    protected ConfigFile(
            String url,
            String login,
            String password,
            String driver,
            Charset sqlFileCharset,
            Charset resultsFileCharset,
            Charset substitutionsFileCharset
    ) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.driver = driver;
        this.sqlFileCharset = sqlFileCharset;
        this.resultsFileCharset = resultsFileCharset;
        this.substitutionsFileCharset = substitutionsFileCharset;
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getDriver() {
        return driver;
    }

    public Charset getSqlFileCharset() {
        return sqlFileCharset;
    }

    public Charset getResultsFileCharset() {
        return resultsFileCharset;
    }

    public Charset getSubstitutionsFileCharset() {
        return substitutionsFileCharset;
    }
}
