package model;

import java.util.*;

public class ThreadInfo {
    public String name;
    public String threadNo;
    public ThreadCategory category;
    public boolean daemon;
    public int priority;
    public String osPriority;
    public String tid;
    public String nid;
    public String state;
    public String stateDetails;
    public List<String> stackTrace = new ArrayList<>();
    public String waitingOnLock;
    public List<String> ownsLocks = new ArrayList<>();
    public String blockedOn;


}