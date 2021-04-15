package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class InvitationCode {

    private String invitationCode;
    private String networkId;
    private String networkName;
    private String entityId;
    private String entityName;
    private boolean valid;

    public InvitationCode() { }

    @DocumentId
    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    @PropertyName("network_id")
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @PropertyName("network_name")
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    @PropertyName("entity_id")
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @PropertyName("entity_name")
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @PropertyName("valid")
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public boolean isValid() {
        return valid;
    }
}
