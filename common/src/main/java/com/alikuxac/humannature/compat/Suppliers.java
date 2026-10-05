package com.alikuxac.humannature.compat;

import java.util.function.Supplier;

public final class Suppliers {
    private Suppliers() {}

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return new MemoizingSupplier<>(delegate);
    }

    private static class MemoizingSupplier<T> implements Supplier<T> {
        private final Supplier<T> delegate;
        private volatile boolean initialized;
        private volatile T value;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        value = delegate.get();
                        initialized = true;
                    }
                }
            }
            return value;
        }
    }
}