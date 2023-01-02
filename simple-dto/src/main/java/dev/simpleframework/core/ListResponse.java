package dev.simpleframework.core;

import java.util.Collection;
import java.util.List;

public class ListResponse<T> extends AbstractResponse<ListData<T>> {

    public static <R> ListResponse<R> of(Collection<R> items) {
        ListResponse<R> response = new ListResponse<>();
        response.setData(ListData.of(items));
        return response;
    }

    public List<T> getItems() {
        return super.getData().getItems();
    }

    public boolean isEmpty() {
        List<T> items = this.getItems();
        return items == null || items.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
