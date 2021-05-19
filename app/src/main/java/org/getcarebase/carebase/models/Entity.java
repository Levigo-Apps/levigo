package org.getcarebase.carebase.models;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Entity {
    private String name;
    private List<String> types;
    private List<TabType> tabs;
    private String type;

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

    @PropertyName("tabs")
    public List<TabType> getTabs() {
        return tabs;
    }

    @PropertyName("tabs")
    public void setTabs(List<TabType> tabs) {
        this.tabs = tabs;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("type")
    public String getType() {
        return type;
    }
}
