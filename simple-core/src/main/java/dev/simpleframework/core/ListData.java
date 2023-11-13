package dev.simpleframework.core;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class ListData<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> items;

    public static <R> ListData<R> of(Collection<R> items) {
        ListData<R> data = new ListData<>();
        data.setItems(items);
        return data;
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

}
