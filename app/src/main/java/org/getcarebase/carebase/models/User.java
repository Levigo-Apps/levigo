package org.getcarebase.carebase.models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

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

}
