package model;

public class ThreadSummary {
    private int totalThreads;
    private int inferredDeadlocks;
    private int jvmReportedDeadlocks;
    private int waitingThreads;
    private int runnableThreads;
    private int timedWaitingThreads;
    private int blockedThreads;
    private String topWaitingMethod;
    private String topRunnableMethod;

    public ThreadSummary() {}

    public ThreadSummary(int total, int inferredDeadlocks,int jvmReportedDeadlocks, int waiting, int runnable,int timedWaiting,int blocked, String topWaitingMethod, String topRunnableMethod) {
        this.totalThreads = total;
        this.inferredDeadlocks = inferredDeadlocks;
        this.jvmReportedDeadlocks = jvmReportedDeadlocks;
        this.waitingThreads = waiting;
        this.runnableThreads = runnable;
        this.timedWaitingThreads = timedWaiting;
        this.blockedThreads = blocked;
        this.topWaitingMethod = topWaitingMethod;
        this.topRunnableMethod = topRunnableMethod;
    }

    // Getters and Setters
    public int getTotalThreads() { return totalThreads; }
    public void setTotalThreads(int totalThreads) { this.totalThreads = totalThreads; }

    public int getJvmReportedDeadlocks() { return jvmReportedDeadlocks; }
    public void setJvmReportedDeadlocks(int jvmReportedDeadlocks) { this.jvmReportedDeadlocks = jvmReportedDeadlocks; }

    public int getInferredDeadlocks() { return inferredDeadlocks; }
    public void setInferredDeadlocks(int inferredDeadlocks) { this.inferredDeadlocks = inferredDeadlocks; }

    public int getWaitingThreads() { return waitingThreads; }
    public void setWaitingThreads(int waitingThreads) { this.waitingThreads = waitingThreads; }

    public int getRunnableThreads() { return runnableThreads; }
    public void setRunnableThreads(int runnableThreads) { this.runnableThreads = runnableThreads; }

    public String getTopWaitingMethod() { return topWaitingMethod; }
    public void setTopWaitingMethod(String topWaitingMethod) { this.topWaitingMethod = topWaitingMethod; }

    public String getTopRunnableMethod() { return topRunnableMethod; }
    public void setTopRunnableMethod(String topRunnableMethod) { this.topRunnableMethod = topRunnableMethod; }

    public int getTimedWaitingThreads() {
        return timedWaitingThreads;
    }

    public void setTimedWaitingThreads(int timedWaitingThreads) {
        this.timedWaitingThreads = timedWaitingThreads;
    }

    public int getBlockedThreads() {
        return blockedThreads;
    }

    public void setBlockedThreads(int blockedThreads) {
        this.blockedThreads = blockedThreads;
    }
}
