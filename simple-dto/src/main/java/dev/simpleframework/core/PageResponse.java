package dev.simpleframework.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends ListResponse<T> {
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 页码
     */
    private int pageNum;
    /**
     * 每页数量
     */
    private int pageSize;

    public static <R> PageResponse<R> of(int pageNum, int pageSize) {
        return of(pageNum, pageSize, 0, Collections.emptyList());
    }

    public static <R> PageResponse<R> of(int pageNum, int pageSize, long total) {
        return of(pageNum, pageSize, total, Collections.emptyList());
    }

    public static <R> PageResponse<R> of(int pageNum, int pageSize, long total, Collection<R> items) {
        PageResponse<R> result = new PageResponse<>();
        result.setItems(items);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages(calcPages(total, pageSize));
        return result;
    }

    public static int calcPages(long total, int pageSize) {
        if (total <= 0 || pageSize <= 0) {
            return 0;
        } else {
            int pages = (int) (total / (long) pageSize);
            if (total % pageSize != 0) {
                ++pages;
            }
            return pages;
        }
    }

}
