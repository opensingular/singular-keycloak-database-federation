package org.opensingular.dbuserprovider.persistence;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RDMS {

    POSTGRESQL("PostgreSQL 10+", org.postgresql.Driver.class.getName(), "SELECT 1"),
    MYSQL("MySQL 5.7+", com.mysql.cj.jdbc.Driver.class.getName(), "SELECT 1"),
    ORACLE("Oracle 12+", oracle.jdbc.OracleDriver.class.getName(), "SELECT 1 FROM DUAL"),
    SQL_SERVER("MS SQL Server 2008+ (jtds)", net.sourceforge.jtds.jdbc.Driver.class.getName(), "SELECT 1");

    private final String desc;
    private final String driver;
    private final String testString;

    RDMS(String desc, String driver, String testString) {
        this.desc = desc;
        this.driver = driver;
        this.testString = testString;
    }

    public static List<String> getAllDescriptions() {
        return Arrays.stream(values()).map(RDMS::getDesc).collect(Collectors.toList());
    }

    public String getDesc() {
        return desc;
    }

    public String getDriver() {
        return driver;
    }

    public String getTestString() {
        return testString;
    }


}
