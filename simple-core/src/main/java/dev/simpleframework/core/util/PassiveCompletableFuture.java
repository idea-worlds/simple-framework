package dev.simpleframework.core.util;

import java.util.concurrent.CompletableFuture;

/**
 * A future which prevents completion by outside caller
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class PassiveCompletableFuture <T> extends CompletableFuture<T> {

    public PassiveCompletableFuture(CompletableFuture<T> future) {
        future.whenComplete((r, t) -> {
            if (t != null) {
                internalCompleteExceptionally(t);
            } else {
                internalComplete(r);
            }
        });
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        throw new UnsupportedOperationException("This future can't be completed by an outside caller");
    }

    @Override
    public boolean complete(T value) {
        throw new UnsupportedOperationException("This future can't be completed by an outside caller");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("This future can't be cancelled by an outside caller");
    }

    @Override
    public void obtrudeException(Throwable ex) {
        throw new UnsupportedOperationException("This future can't be completed by an outside caller");
    }

    @Override
    public void obtrudeValue(T value) {
        throw new UnsupportedOperationException("This future can't be completed by an outside caller");
    }

    private void internalComplete(T value) {
        super.complete(value);
    }

    private void internalCompleteExceptionally(Throwable ex) {
        super.completeExceptionally(ex);
    }

}
