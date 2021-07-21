package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class ProductCode {
    private String id;
    private String type;
    private List<String> tags;

    @DocumentId
    public String getId() {
        return id;
    }

    @DocumentId
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
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
