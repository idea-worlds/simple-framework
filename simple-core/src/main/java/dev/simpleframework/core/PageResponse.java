package dev.simpleframework.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends AbstractResponse<PageData<T>> {

    public static <R> PageResponse<R> of(int pageNum, int pageSize) {
        return of(pageNum, pageSize, 0, Collections.emptyList());
    }

    public static <R> PageResponse<R> of(int pageNum, int pageSize, long total) {
        return of(pageNum, pageSize, total, Collections.emptyList());
    }

    public static <R> PageResponse<R> of(int pageNum, int pageSize, long total, Collection<R> items) {
        PageResponse<R> result = new PageResponse<>();
        PageData<R> data = PageData.of(pageNum, pageSize, total, items);
        result.setData(data);
        return result;
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
