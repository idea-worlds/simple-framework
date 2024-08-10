package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.JobRecord;
import dev.simpleframework.util.Jsons;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 作业：转换数据
 *
 * @author loyayz
 **/
public abstract non-sealed class BaseSinkPipelineJob extends PipelineJob {

    public BaseSinkPipelineJob(String id) {
        super(id);
    }

    /**
     * 转换，不为 null 的数据自动发送至后置作业
     *
     * @param data 前置作业传过来的数据值 {@link JobRecord#getData}
     */
    protected abstract Object doTransform(Object data);

    /**
     * 接收前置作业的数据后执行转换 {@link #doTransform}，不为 null 的数据自动发送至后置作业
     */
    @Override
    protected void onData(JobRecord data) {
        Object value = this.doTransform(data.getData());
        if (value != null) {
            data = new JobRecord(data.getSource(), super.id(), value);
            this.emitData(data);
        }
    }

    /**
     * 前置作业全成功，说明本作业已经处理完所有接收到的数据，此时应结束本作业
     */
    @Override
    protected void onFinishWithAllComplete() {
        this.emitComplete();
    }

    public static Object getObjectValue(Object data, String key) {
        if (data == null || "".equals(key)) {
            return data;
        }
        Map<String, Object> map = parseToMap(data);
        return map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(Object data) {
        if (data == null) {
            return null;
        }
        Map<String, Object> result;
        try {
            if (data instanceof Map) {
                result = (Map<String, Object>) data;
            } else if (data instanceof Collection<?>) {
                result = Collections.emptyMap();
            } else {
                result = Jsons.toMap(data);
            }
        } catch (Exception ignore) {
            result = new LinkedHashMap<>();
        }
        return result;
    }

}
