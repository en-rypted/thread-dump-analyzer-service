package model;

import java.util.ArrayList;
import java.util.List;

public class ThreadDumpSnapshot {
    public String timestamp;
    public List<ThreadInfo> threads;
    public List<List<ThreadInfo>> jvmReportedDeadlocks = new ArrayList<>();
}