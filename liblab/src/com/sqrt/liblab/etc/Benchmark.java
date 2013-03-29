package com.sqrt.liblab.etc;

public class Benchmark {
    public static Task record(String name) {
        return new Task(name);
    }

    public static class Task {
        public final String name;
        public final long start;
        private long end;

        private Task(String name) {
            this.name = name;
            this.start = System.currentTimeMillis();
        }

        public long elapsed() {
            return System.currentTimeMillis() - start;
        }

        public long stop() {
            end = System.currentTimeMillis();
            return end - start;
        }

        public long timeTaken() {
            return end - start;
        }
    }
}