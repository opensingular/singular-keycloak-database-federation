package org.opensingular.dbuserprovider.persistence;


import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RDMS {

    POSTGRESQL("PostgreSQL 10+", org.postgresql.Driver.class.getName(), "SELECT 1", new PostgreSQL10Dialect()),
    MYSQL("MySQL 5.7+", com.mysql.cj.jdbc.Driver.class.getName(), "SELECT 1", new MySQL57Dialect()),
    ORACLE("Oracle 12+", oracle.jdbc.OracleDriver.class.getName(), "SELECT 1 FROM DUAL", new Oracle12cDialect()),
    SQL_SERVER("MS SQL Server 2012+ (jtds)", net.sourceforge.jtds.jdbc.Driver.class.getName(), "SELECT 1", new SQLServer2012Dialect());

    private final String  desc;
    private final String  driver;
    private final String  testString;
    private final Dialect dialect;

    RDMS(String desc, String driver, String testString, Dialect dialect) {
        this.desc = desc;
        this.driver = driver;
        this.testString = testString;
        this.dialect = dialect;
    }

    public static RDMS getByDescription(String desc) {
        for (RDMS value : values()) {
            if (value.desc.equals(desc)) {
                return value;
            }
        }
        return null;
    }

    public Dialect getDialect() {
        return dialect;
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
