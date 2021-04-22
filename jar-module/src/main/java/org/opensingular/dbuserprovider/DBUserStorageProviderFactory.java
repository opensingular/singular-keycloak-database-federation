package org.opensingular.dbuserprovider;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.opensingular.dbuserprovider.model.QueryConfigurations;
import org.opensingular.dbuserprovider.persistence.DataSourceProvider;
import org.opensingular.dbuserprovider.persistence.RDBMS;

import java.util.List;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class DBUserStorageProviderFactory implements UserStorageProviderFactory<DBUserStorageProvider> {

    private static final String PARAMETER_PLACEHOLDER_HELP = "Use '?' as parameter placeholder character (replaced only once). ";
    private static final String DEFAULT_HELP_TEXT          = "Select to query all users you must return at least: \"id\". " +
            "            \"username\"," +
            "            \"email\" (opcional)," +
            "            \"firstName\" (opcional)," +
            "            \"lastName\" (opcional). Any other parameter can be mapped by aliases to a realm scope";
    private static final String PARAMETER_HELP             = " The %s is passed as query parameter.";


    private DataSourceProvider  dataSourceProvider = new DataSourceProvider();
    private QueryConfigurations queryConfigurations;
    private boolean             configured         = false;

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public DBUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        if (!configured) {
            configure(model);
        }
        return new DBUserStorageProvider(session, model, dataSourceProvider, queryConfigurations);
    }

    private synchronized void configure(ComponentModel config) {
        String user     = config.get("user");
        String password = config.get("password");
        String url      = config.get("url");
        RDBMS  rdbms    = RDBMS.getByDescription(config.get("rdbms"));
        dataSourceProvider.configure(url, rdbms, user, password);
        queryConfigurations = new QueryConfigurations(
                config.get("count"),
                config.get("listAll"),
                config.get("findById"),
                config.get("findByUsername"),
                config.get("findBySearchTerm"),
                config.get("findPasswordHash"),
                config.get("hashFunction"),
                rdbms
        );
        configured = true;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        try {
            configure(config);
        } catch (Exception e) {
            throw new ComponentValidationException(e.getMessage(), e);
        }
    }

    @Override
    public String getId() {
        return "singular-db-user-provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                //DATABASE
                .property()
                .name("url")
                .label("JDBC URL")
                .helpText("JDBC Connection String")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("jdbc:jtds:sqlserver://server-name/database_name;instance=instance_name")
                .add()
                .property()
                .name("user")
                .label("JDBC Connection User")
                .helpText("JDBC Connection User")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("user")
                .add()
                .property()
                .name("password")
                .label("JDBC Connection Password")
                .helpText("JDBC Connection Password")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("password")
                .add()
                .property()
                .name("rdbms")
                .label("RDBMS")
                .helpText("Relational Database Management System")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(RDBMS.getAllDescriptions())
                .defaultValue(RDBMS.SQL_SERVER.getDesc())
                .add()

                //QUERIES

                .property()
                .name("count")
                .label("User count SQL query")
                .helpText("SQL query returning the total count of users")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("select count(*) from users")
                .add()

                .property()
                .name("listAll")
                .label("List All Users SQL query")
                .helpText(DEFAULT_HELP_TEXT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("select \"id\"," +
                        "            \"username\"," +
                        "            \"email\"," +
                        "            \"firstName\"," +
                        "            \"lastName\"," +
                        "            \"cpf\"," +
                        "            \"fullName\" from users ")
                .add()

                .property()
                .name("findById")
                .label("Find user by id SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user id") + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("select \"id\"," +
                        "            \"username\"," +
                        "            \"email\"," +
                        "            \"firstName\"," +
                        "            \"lastName\"," +
                        "            \"cpf\"," +
                        "            \"fullName\" from users where \"id\" = ? ")
                .add()

                .property()
                .name("findByUsername")
                .label("Find user by username SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user username") + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("select \"id\"," +
                        "            \"username\"," +
                        "            \"email\"," +
                        "            \"firstName\"," +
                        "            \"lastName\"," +
                        "            \"cpf\"," +
                        "            \"fullName\" from users where \"username\" = ? ")
                .add()

                .property()
                .name("findBySearchTerm")
                .label("Find user by search term SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "search term") + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("select \"id\"," +
                        "            \"username\"," +
                        "            \"email\"," +
                        "            \"firstName\"," +
                        "            \"lastName\"," +
                        "            \"cpf\"," +
                        "            \"fullName\" from users where upper(\"username\") like (?)  or upper(\"email\") like (?) or upper(\"fullName\") like (?)")
                .add()

                .property()
                .name("findPasswordHash")
                .label("Find password hash (blowfish or hash digest hex) SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user username") + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("select hash_pwd from users where \"username\" = ? ")
                .add()
                .property()
                .name("hashFunction")
                .label("Password hash function")
                .helpText("Hash type used to match passwrod (md* e sha* uses hex hash digest)")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options("Blowfish (bcrypt)", "MD2", "MD5", "SHA-1", "SHA-256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512", "SHA-384", "SHA-512/224", "SHA-512/256", "SHA-512")
                .defaultValue("SHA-1")
                .add()
                .build();
    }


}
