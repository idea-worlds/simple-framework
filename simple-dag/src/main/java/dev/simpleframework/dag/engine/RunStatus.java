package dev.simpleframework.dag.engine;

/**
 * 运行状态
 *
 * @author loyayz
 **/
public enum RunStatus {
    WAIT, RUNNING,
    COMPLETE {
        @Override
        public boolean isFinish() {
            return true;
        }
    },
    FAIL {
        @Override
        public boolean isFinish() {
            return true;
        }
    },
    ABORT {
        @Override
        public boolean isFinish() {
            return true;
        }
    };

    public boolean isFinish() {
        return false;
    }

}
