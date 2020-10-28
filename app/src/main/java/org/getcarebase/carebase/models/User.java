package org.getcarebase.carebase.models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private final String userId;
    private final String email;
    private final String hospitalId;
    private final String hospitalName;
    private final String networkId;
    private final String networkName;

    public User(String userId, String email, String hospitalId, String hospitalName, String networkId, String networkName) {
        this.userId = userId;
        this.email = email;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.networkId = networkId;
        this.networkName = networkName;
    }

    public User(String userId, String email, InvitationCode invitationCode) {
        this.userId = userId;
        this.email = email;
        this.hospitalId = invitationCode.getHospitalId();
        this.hospitalName = invitationCode.getHospitalName();
        this.networkId = invitationCode.getNetworkId();
        this.networkName = invitationCode.getNetworkName();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
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
        userMap.put("hospital_id", hospitalId);
        userMap.put("hospital_name", hospitalName);
        userMap.put("email", email);
        return userMap;
    }

}
