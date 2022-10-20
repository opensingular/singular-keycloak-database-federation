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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class DBUserStorageProviderFactory implements UserStorageProviderFactory<DBUserStorageProvider> {
    
    private static final String PARAMETER_PLACEHOLDER_HELP = "Use '?' as parameter placeholder character (replaced only once). ";
    private static final String DEFAULT_HELP_TEXT          = "Select to query all users you must return at least: \"id\". " +
                                                             "            \"username\"," +
                                                             "            \"email\" (optional)," +
                                                             "            \"firstName\" (optional)," +
                                                             "            \"lastName\" (optional). Any other parameter can be mapped by aliases to a realm scope";
    private static final String PARAMETER_HELP             = " The %s is passed as query parameter.";
    
    
    private Map<String, ProviderConfig> providerConfigPerInstance = new HashMap<>();
    
    @Override
    public void init(Config.Scope config) {
    }
    
    @Override
    public void close() {
        for (Map.Entry<String, ProviderConfig> pc : providerConfigPerInstance.entrySet()) {
            pc.getValue().dataSourceProvider.close();
        }
    }
    
    @Override
    public DBUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        ProviderConfig providerConfig = providerConfigPerInstance.computeIfAbsent(model.getId(), s -> configure(model));
        return new DBUserStorageProvider(session, model, providerConfig.dataSourceProvider, providerConfig.queryConfigurations);
    }
    
    private synchronized ProviderConfig configure(ComponentModel model) {
        log.infov("Creating configuration for model: id={0} name={1}", model.getId(), model.getName());
        ProviderConfig providerConfig = new ProviderConfig();
        String         user           = model.get("user");
        String         password       = model.get("password");
        String         url            = model.get("url");
        RDBMS          rdbms          = RDBMS.getByDescription(model.get("rdbms"));
        providerConfig.dataSourceProvider.configure(url, rdbms, user, password, model.getName());
        providerConfig.queryConfigurations = new QueryConfigurations(
                model.get("count"),
                model.get("listAll"),
                model.get("findById"),
                model.get("findByUsername"),
                model.get("findBySearchTerm"),
                model.get("findPasswordHash"),
                model.get("hashFunction"),
                rdbms,
                model.get("allowKeycloakDelete", false),
                model.get("allowDatabaseToOverwriteKeycloak", false)
        );
        return providerConfig;
    }
    
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        try {
            ProviderConfig old = providerConfigPerInstance.put(model.getId(), configure(model));
            if (old != null) {
                old.dataSourceProvider.close();
            }
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
                                           .property()
                                           .name("allowKeycloakDelete")
                                           .label("Allow Keycloak's User Delete")
                                           .helpText("By default, clicking Delete on a user in Keycloak is not allowed.  Activate this option to allow to Delete Keycloak's version of the user (does not touch the user record in the linked RDBMS), e.g. to clear synching issues and allow the user to be synced from scratch from the RDBMS on next use, in Production or for testing.")
                                           .type(ProviderConfigProperty.BOOLEAN_TYPE)
                                           .defaultValue("false")
                                           .add()
                                           .property()
                                           .name("allowDatabaseToOverwriteKeycloak")
                                           .label("Allow DB Attributes to Overwrite Keycloak")
                                           // Technical details for the following comment: we aggregate both the existing Keycloak version and the DB version of an attribute in a Set, but since e.g. email is not a list of values on the Keycloak User, the new email is never set on it.
                                           .helpText("By default, once a user is loaded in Keycloak, its attributes (e.g. 'email') stay as they are in Keycloak even if an attribute of the same name now returns a different value through the query.  Activate this option to have all attributes set in the SQL query to always overwrite the existing user attributes in Keycloak (e.g. if Keycloak user has email 'test@test.com' but the query fetches a field named 'email' that has a value 'example@exemple.com', the Keycloak user will now have email attribute = 'example@exemple.com'). This behavior works with NO_CAHCE configuration. In case you set this flag under a cached configuration, the user attributes will be reload if: 1) the cached value is older than 500ms and 2) username or e-mail does not match cached values.")
                                           .type(ProviderConfigProperty.BOOLEAN_TYPE)
                                           .defaultValue("false")
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
                                           .options("Blowfish (bcrypt)", "MD2", "MD5", "SHA-1", "SHA-256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512", "SHA-384", "SHA-512/224", "SHA-512/256", "SHA-512", "PBKDF2-SHA256")
                                           .defaultValue("SHA-1")
                                           .add()
                                           .build();
    }
    
    private static class ProviderConfig {
        private DataSourceProvider  dataSourceProvider = new DataSourceProvider();
        private QueryConfigurations queryConfigurations;
    }
    
    
}
