package dev.simpleframework.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageData<T> extends ListData<T> {

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

    public static <R> PageData<R> of(int pageNum, int pageSize, long total, Collection<R> items) {
        PageData<R> data = new PageData<>();
        data.setItems(items);
        data.setPageNum(pageNum);
        data.setPageSize(pageSize);
        data.setTotal(total);
        return data;
    }

    public void setTotal(long total) {
        this.total = total;
        this.calcPages();
    }

    public void calcPages() {
        int pages = 0;
        if (this.total > 0 && this.pageSize > 0) {
            pages = (int) (this.total / (long) this.pageSize);
            if (this.total % this.pageSize != 0) {
                ++pages;
            }
        }
        this.pages = pages;
    }

}
