package service;


import model.ThreadCategory;
import model.ThreadDumpSnapshot;
import model.ThreadGroupResult;
import model.ThreadInfo;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class ThreadDumpParser {

    public static List<ThreadDumpSnapshot> parseMultiple(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<ThreadDumpSnapshot> snapshots = new ArrayList<>();
        List<ThreadInfo> currentThreads = new ArrayList<>();
        ThreadInfo current = null;
        String currentTimestamp = null;
        String previousTimestamp = null;

        Pattern timestampPattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");
        Pattern dumpStart = Pattern.compile("^Full thread dump.*");
      //  Pattern header = Pattern.compile("^\"([^\"]+)\"\\s*(daemon)?\\s*#\\d+\\s+prio=(\\d+)\\s+os_prio=(\\S+)\\s+tid=([^ ]+)\\s+nid=([^ ]+).*");
        Pattern header = Pattern.compile("^\"([^\"]+)\"\\s+#(\\d+)\\s*(daemon)?\\s+prio=(\\d+)\\s+os_prio=(\\S+)\\s+tid=(\\S+)\\s+nid=(\\S+).*");
        Pattern otherHeaderPattern = Pattern.compile(
                "^\"([^\"]+)\"\\s*(daemon)?\\s*os_prio=(\\d+)\\s+tid=([^\\s]+)\\s+nid=([^\\s]+)\\s*(\\S+)?"
        );
        Pattern state = Pattern.compile("^\\s+java\\.lang\\.Thread\\.State:\\s+(\\S+)(?: \\(([^)]+)\\))?");
        Pattern stack = Pattern.compile("^\\s+at (.+)");
        Pattern waiting = Pattern.compile("waiting to lock <([^>]+)>");
        Pattern owns = Pattern.compile("locked <([^>]+)>");
        Pattern blocked = Pattern.compile("blocked on <([^>]+)>");
        Pattern jvmDeadlockStart = Pattern.compile("^Found one Java-level deadlock:");

        String line;
        boolean insideJvmDeadlock = false;
        List<ThreadInfo> jvmDeadlockGroup = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            Matcher tsMatcher = timestampPattern.matcher(line);
            if (tsMatcher.find()) {
                previousTimestamp = currentTimestamp;
                currentTimestamp = tsMatcher.group(1);
            }

            if (dumpStart.matcher(line).find() && !currentThreads.isEmpty()) {
                snapshots.add(buildSnapshot(currentThreads, previousTimestamp));
                currentThreads = new ArrayList<>();
            }

            if (jvmDeadlockStart.matcher(line).find()) {
                insideJvmDeadlock = true;
                jvmDeadlockGroup = new ArrayList<>();
                continue;
            }

            if (insideJvmDeadlock) {
                if ((line.startsWith("\""))) {
                    Matcher m = header.matcher(line);
                    if (m.find()) {
                        ThreadInfo ti = new ThreadInfo();
                        ti.name = m.group(1);
                        jvmDeadlockGroup.add(ti);
                    }
                } else if (line.trim().isEmpty()) {
                    insideJvmDeadlock = false;
                    if (!jvmDeadlockGroup.isEmpty()) {
                        // Save deadlock group into current snapshot
                        if (!snapshots.isEmpty()) {
                            snapshots.get(snapshots.size() - 1).jvmReportedDeadlocks.add(jvmDeadlockGroup);
                        }
                    }
                }
                continue;
            }

            Matcher m;
            if ((m = header.matcher(line)).find()) {
                if (current != null) currentThreads.add(current);
                current = new ThreadInfo();
                current.name = m.group(1);
                current.threadNo = m.group(2);
                current.daemon = m.group(3) != null;
                current.priority = Integer.parseInt(m.group(4));
                current.osPriority = m.group(5);
                current.tid = m.group(6);
                current.nid = m.group(7);
                current.category = ThreadCategory.APPLICATION;
                continue;
            }else if((m = otherHeaderPattern.matcher(line)).find()){
                current = new ThreadInfo();
                current.name = m.group(1);

                current.daemon = m.group(2) != null;

                current.osPriority = m.group(3);
                current.tid = m.group(4);
                current.nid = m.group(5);
                current.state = m.group(6).toUpperCase();
                current.category = classifyThread(current.name);
                continue;
            }

            if (current == null) continue;

            if ((m = state.matcher(line)).find()) {
                current.state = m.group(1);
                current.stateDetails = m.group(2);
            } else if ((m = stack.matcher(line)).find()) {
                current.stackTrace.add(m.group(1));
            } else if ((m = waiting.matcher(line)).find()) {
                current.waitingOnLock = m.group(1);
            } else if ((m = owns.matcher(line)).find()) {
                current.ownsLocks.add(m.group(1));
            } else if ((m = blocked.matcher(line)).find()) {
                current.blockedOn = m.group(1);
            }
        }

        if (current != null) currentThreads.add(current);
        if (!currentThreads.isEmpty()) {
            snapshots.add(buildSnapshot(currentThreads, currentTimestamp));
        }

        reader.close();
        return snapshots;
    }

    private static ThreadCategory classifyThread(String name) {
        if (name.matches(".*(GC|Garbage|Mark|Sweep|Scavenge|Gang).*")) return ThreadCategory.GC;
        if (name.matches(".*(Compiler|C1|C2).*")) return ThreadCategory.COMPILER;
        if (name.contains("Finalizer")) return ThreadCategory.FINALIZER;
        if (name.contains("Reference Handler")) return ThreadCategory.REFERENCE;
        if (name.contains("Signal Dispatcher")) return ThreadCategory.SIGNAL;
        if (name.contains("VM Thread")) return ThreadCategory.VM;
        if (name.contains("JFR") || name.contains("Flight Recorder")) return ThreadCategory.JFR;
        return ThreadCategory.APPLICATION;
    }

    private static ThreadDumpSnapshot buildSnapshot(List<ThreadInfo> threads, String timestamp) {
        ThreadDumpSnapshot snapshot = new ThreadDumpSnapshot();
        snapshot.threads = new ArrayList<>(threads);
        snapshot.timestamp = timestamp;
        return snapshot;
    }

    public static ThreadGroupResult analyzeSnapshot(ThreadDumpSnapshot snapshot) {
        ThreadGroupResult analysis = new ThreadGroupResult();
        Map<String, ThreadInfo> mayBeDeadlocks = new HashMap<>();

        for (ThreadInfo t : snapshot.threads) {
            switch (t.state) {
                case "RUNNABLE":
                    analysis.runnable.add(t);
                    break;
                case "WAITING":
                    analysis.waiting.add(t);
                    break;
                case "TIMED_WAITING":
                    analysis.timedWaiting.add(t);
                    break;
                case "BLOCKED":
                    analysis.blocked.add(t);
                    break;
            }

          if(t.waitingOnLock != null && !t.ownsLocks.isEmpty()){
              mayBeDeadlocks.put(t.waitingOnLock,t);
          }
        }

        List<String> waitingOnList = mayBeDeadlocks.keySet().stream().toList();
        List<ThreadInfo> mayBeDeadThreadInfoList = mayBeDeadlocks.values().stream().toList();

        for(String waitingOn : waitingOnList){
            String currentWaitingOn = waitingOn;
            Map<String,ThreadInfo> cycle = new HashMap<>();
            for (int i = 0; i < mayBeDeadThreadInfoList.size() ; i++) {
                ThreadInfo currentThread = mayBeDeadThreadInfoList.get(i);
                if(currentThread.ownsLocks.contains(currentWaitingOn)){
                    if(cycle.isEmpty()){
                        cycle.put(currentThread.tid,currentThread);
                        currentWaitingOn = currentThread.waitingOnLock;
                        i = -1;
                    }else{
                        if(cycle.containsKey(currentThread.tid)){
                            String id = cycle.keySet().stream().sorted().collect(Collectors.joining("--}"));
                            if(!analysis.inferredDeadlocks.containsKey(id)){
                                analysis.inferredDeadlocks.put(id,cycle);
                            }
                            i = mayBeDeadThreadInfoList.size();
                        }else{
                            cycle.put(currentThread.tid,currentThread);
                            currentWaitingOn = currentThread.waitingOnLock;
                            i = -1;
                        }
                    }
                }
            }
        }
        analysis.jvmReportedDeadlocks.addAll(snapshot.jvmReportedDeadlocks);
        return analysis;
    }
}
