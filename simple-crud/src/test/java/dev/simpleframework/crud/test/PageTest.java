package dev.simpleframework.crud.test;

import dev.simpleframework.crud.core.Page;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Page 分页计算逻辑单元测试。
 * 验证总页数、偏移量计算，以及 Page.of() 工厂方法。
 */
public class PageTest {

    /** 场景：总数正好整除每页数量。验证点：calcPages(50,10)=5。 */
    @Test
    public void testCalcPages_exactDivisible() {
        assertEquals(5, Page.calcPages(50, 10));
    }

    /** 场景：总数不能整除。验证点：calcPages(51,10)=6（向上取整）。 */
    @Test
    public void testCalcPages_withRemainder() {
        assertEquals(6, Page.calcPages(51, 10));
    }

    /** 场景：总数为 0。验证点：calcPages(0,10)=0。边界情况，不应抛异常。 */
    @Test
    public void testCalcPages_zeroTotal_shouldReturnZero() {
        assertEquals(0, Page.calcPages(0, 10));
    }

    /** 场景：第 1 页。验证点：起始行为 0（offset = (1-1)*10 = 0）。 */
    @Test
    public void testCalcStartRow_page1_shouldReturnZero() {
        assertEquals(0, Page.calcStartRow(1, 10));
    }

    /** 场景：第 2 页。验证点：offset = (2-1)*10 = 10。 */
    @Test
    public void testCalcStartRow_page2_shouldReturnOffset() {
        assertEquals(10, Page.calcStartRow(2, 10));
    }

    /** 场景：页码 <= 0。验证点：offset = 0。防御性处理非法输入。 */
    @Test
    public void testCalcStartRow_nonPositivePage_shouldReturnZero() {
        assertEquals(0, Page.calcStartRow(0, 10));
    }

    /**
     * 场景：Page.of() 工厂方法创建完整分页对象。
     * 验证点：pageNum, pageSize, total, pages, offset, items 全部正确设置。
     */
    @Test
    public void testOf_shouldSetAllFields() {
        Page<String> page = Page.of(2, 10, 55, Arrays.asList("a", "b"));
        assertEquals(2, page.getPageNum());
        assertEquals(10, page.getPageSize());
        assertEquals(55, page.getTotal());
        assertEquals(6, page.getPages());
        assertEquals(10, page.getOffset());
        assertEquals(2, page.getItems().size());
    }

}
