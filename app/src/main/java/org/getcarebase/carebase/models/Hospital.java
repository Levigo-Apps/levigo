package org.getcarebase.carebase.models;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Hospital {
    private String name;
    private List<String> types;

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("device_types")
    public List<String> getTypes() {
        return types;
    }

    @PropertyName("device_types")
    public void setTypes(List<String> types) {
        this.types = types;
    }
}
