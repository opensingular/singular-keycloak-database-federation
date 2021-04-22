package org.opensingular.dbuserprovider.model;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private final String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, Map<String, String> data) {
        super(session, realm, model);
        this.keycloakId = StorageId.keycloakId(model, data.get("id"));
        for (Entry<String, String> e : data.entrySet()) {
            List<String> newValues = new ArrayList<>();
            List<String> attribute = this.getAttribute(e.getKey());
            if (attribute != null) {
                newValues.addAll(attribute);
            }
            newValues.add(e.getValue());
            this.setAttribute(e.getKey(), newValues);
        }
    }


    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public String getUsername() {
        return getFirstAttribute(UserModel.USERNAME);
    }

    @Override
    public void setUsername(String username) {

    }

    @Override
    public String getEmail() {
        return getFirstAttribute(UserModel.EMAIL);
    }

    @Override
    public void setEmail(String email) {

    }

    @Override
    public String getFirstName() {
        return getFirstAttribute(UserModel.FIRST_NAME);
    }

    @Override
    public void setFirstName(String firstName) {

    }

    @Override
    public String getLastName() {
        return getFirstAttribute(UserModel.LAST_NAME);
    }

    @Override
    public void setLastName(String lastName) {

    }
}
