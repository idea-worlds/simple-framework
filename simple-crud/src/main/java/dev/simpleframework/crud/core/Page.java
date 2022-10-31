package dev.simpleframework.crud.core;

import dev.simpleframework.core.PageResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
@NoArgsConstructor
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 数据集
     */
    private List<T> items;
    /**
     * 页码
     */
    private int pageNum;
    /**
     * 每页数量
     */
    private int pageSize;
    /**
     * 总记录数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 当前页偏移量（起始行）
     */
    private long offset;

    public static <R> Page<R> of(int pageNum, int pageSize, long total) {
        return of(pageNum, pageSize, total, Collections.emptyList());
    }

    public static <R> Page<R> of(int pageNum, int pageSize, long total, List<R> items) {
        Page<R> result = new Page<>();
        result.setItems(items);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages(calcPages(total, pageSize));
        result.setOffset(calcStartRow(pageNum, pageSize));
        return result;
    }

    public <R> Page<R> convert(Function<? super T, ? extends R> mapper) {
        Page<R> result = new Page<>(this);
        List<R> collect = this.items.stream().map(mapper).collect(toList());
        result.setItems(collect);
        return result;
    }

    public PageResponse<T> toResponse() {
        return PageResponse.of(this.pageNum, this.pageSize, this.total, this.items);
    }

    public <R> PageResponse<R> toResponse(Function<? super T, ? extends R> mapper) {
        List<R> items = this.items.stream().map(mapper).collect(toList());
        return PageResponse.of(this.pageNum, this.pageSize, this.total, items);
    }

    /**
     * 计算总页数
     *
     * @param total    总数量
     * @param pageSize 每页数量
     * @return 总页数
     */
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

    /**
     * 计算当前页偏移量（起始行）
     *
     * @param pageNum  页数
     * @param pageSize 每页数量
     * @return 起始行
     */
    public static long calcStartRow(int pageNum, int pageSize) {
        if (pageNum <= 0) {
            return 0L;
        }
        return (long) (pageNum - 1) * pageSize;
    }

    private Page(Page<?> other) {
        this.items = new ArrayList<>();
        this.pageNum = other.getPageNum();
        this.pageSize = other.getPageSize();
        this.total = other.getTotal();
        this.pages = other.getPages();
        this.offset = other.getOffset();
    }

}
