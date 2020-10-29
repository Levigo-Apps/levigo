package org.getcarebase.carebase.models;

public class InvitationCode {
    private final String invitationCode;
    private final String networkId;
    private final String networkName;
    private final String hospitalId;
    private final String hospitalName;
    private final boolean valid;

    public InvitationCode(String invitationCode, String networkId, String networkName, String hospitalId, String hospitalName, boolean valid) {
        this.invitationCode = invitationCode;
        this.networkId = networkId;
        this.networkName = networkName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
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

    public String getHospitalId() {
        return hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public boolean isValid() {
        return valid;
    }
}
