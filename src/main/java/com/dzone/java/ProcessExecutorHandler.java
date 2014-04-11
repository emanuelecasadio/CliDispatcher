package com.dzone.java;

public interface ProcessExecutorHandler {
    public void onStandardOutput(String msg);
    public void onStandardError(String msg);
}
