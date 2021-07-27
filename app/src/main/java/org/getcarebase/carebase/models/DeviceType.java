package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

/**
 * A class representing the types and various tags in the entity
 */
public class DeviceType {
    private String type;
    private List<String> tags;

    public DeviceType() {}

    public DeviceType(String type, List<String> tags) {
        this.type = type;
        this.tags = tags;
    }

    @DocumentId
    public String getType() {
        return type;
    }

    @DocumentId
    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("tags")
    public List<String> getTags() {
        return tags;
    }

    @PropertyName("tags")
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
