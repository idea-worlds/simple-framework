package dev.simpleframework.dag.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 运行状态
 *
 * @author loyayz
 **/
@Getter
@RequiredArgsConstructor
public enum RunStatus {

    /**
     * 等待运行
     */
    WAIT(false),
    /**
     * 运行中
     */
    RUNNING(false),
    /**
     * 成功结束
     */
    COMPLETE(true),
    /**
     * 失败
     */
    FAIL(true),
    /**
     * 中止（手动停止运行）
     */
    ABORT(true),
    /**
     * 取消（前置失败自动停止运行）
     */
    CANCEL(true);

    private final boolean finish;

}
