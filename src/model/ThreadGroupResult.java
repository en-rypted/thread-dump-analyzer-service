package model;

import java.util.*;

public class ThreadGroupResult {
    public Map<String,Map<String,ThreadInfo>> inferredDeadlocks = new HashMap<>();
    public List<List<ThreadInfo>> jvmReportedDeadlocks = new ArrayList<>();
    public List<ThreadInfo> waiting = new ArrayList<>();
    public List<ThreadInfo> runnable = new ArrayList<>();
    public List<ThreadInfo> timedWaiting = new ArrayList<>();
    public List<ThreadInfo> blocked = new ArrayList<>();
}