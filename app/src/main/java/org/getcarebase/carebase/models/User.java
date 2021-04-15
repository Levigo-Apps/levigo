package org.getcarebase.carebase.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private final String userId;
    private final String email;
    private final String entityId;
    private final String entityName;
    private final String networkId;
    private final String networkName;

    public User(String userId, String email, String entityId, String entityName, String networkId, String networkName) {
        this.userId = userId;
        this.email = email;
        this.entityId = entityId;
        this.entityName = entityName;
        this.networkId = networkId;
        this.networkName = networkName;
    }

    public User(String userId, String email, InvitationCode invitationCode) {
        this.userId = userId;
        this.email = email;
        this.entityId = invitationCode.getEntityId();
        this.entityName = invitationCode.getEntityName();
        this.networkId = invitationCode.getNetworkId();
        this.networkName = invitationCode.getNetworkName();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public Map<String,String> toMap() {
        Map<String,String> userMap = new HashMap<>();
        userMap.put("network_id", networkId);
        userMap.put("network_name", networkName);
        userMap.put("entity_id", entityId);
        userMap.put("entity_name", entityName);
        userMap.put("email", email);
        return userMap;
    }

}
