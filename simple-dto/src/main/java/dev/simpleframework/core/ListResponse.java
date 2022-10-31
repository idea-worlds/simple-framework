package dev.simpleframework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> items;

    public static <R> ListResponse<R> of(Collection<R> items) {
        ListResponse<R> response = new ListResponse<>();
        response.setItems(items);
        return response;
    }

    public List<T> getItems() {
        return this.items;
    }

    public void setItems(List<T> data) {
        this.items = data;
    }

    public void setItems(Collection<T> data) {
        if (null == data) {
            this.items = Collections.emptyList();
        } else if (data instanceof List) {
            this.items = (List<T>) data;
        } else {
            this.items = new ArrayList<>(data);
        }
    }

    public boolean isEmpty() {
        return this.items == null || this.items.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
