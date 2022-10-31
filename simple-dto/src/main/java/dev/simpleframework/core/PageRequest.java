package dev.simpleframework.core;

import lombok.Setter;

import java.io.Serializable;

@Setter
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Integer DEFAULT_NUM = 1;
    private static final Integer DEFAULT_SIZE = 10;

    /**
     * 页码，默认 1
     */
    private Integer pageNum;
    /**
     * 每页的数量，默认 10
     */
    private Integer pageSize;

    public static PageRequest of(int pageNum, int pageSize) {
        PageRequest request = new PageRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return request;
    }

    public Integer getPageNum() {
        return this.pageNum != null && this.pageNum > 0 ? this.pageNum : DEFAULT_NUM;
    }

    public Integer getPageSize() {
        return this.pageSize != null && this.pageSize > 0 ? this.pageSize : DEFAULT_SIZE;
    }

    public Integer getPageStartIndex() {
        return (this.getPageNum() - 1) * this.getPageSize();
    }

}
