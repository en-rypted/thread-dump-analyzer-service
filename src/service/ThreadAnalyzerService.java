package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadAnalyzerService {

    public static ThreadAnalysis getThreadAnalysis(File file) throws IOException {
        List<ThreadDumpSnapshot> threads = ThreadDumpParser.parseMultiple(file);
        Map<String,ThreadGroupResult> threadGroupResultMap = new HashMap<>();
        Map<String,ThreadSummary> summaryMap = new HashMap<>();
        for (ThreadDumpSnapshot threadDumpSnapshot :threads){
            ThreadGroupResult threadGroupResult = ThreadDumpParser.analyzeSnapshot(threadDumpSnapshot);
          threadGroupResultMap.put(threadDumpSnapshot.timestamp,threadGroupResult);
          summaryMap.put(threadDumpSnapshot.timestamp,getSummery(threadGroupResult,threadDumpSnapshot.threads));

        }



        ThreadAnalysis threadAnalysis = new ThreadAnalysis();
        threadAnalysis.threadGroupResult=threadGroupResultMap;
        threadAnalysis.threadDumpSnapshotList = threads;
        threadAnalysis.summary = summaryMap;
        return threadAnalysis;
    }

    private static String getTopMethod(List<ThreadInfo> threads) {
        for (ThreadInfo thread : threads) {
            if (thread.stackTrace != null && !thread.stackTrace.isEmpty()) {
                return thread.stackTrace.getFirst(); // top frame (most recent call)
            }
        }
        return "N/A";
    }

    private static ThreadSummary getSummery(ThreadGroupResult groups,List<ThreadInfo> threads){
        ThreadSummary summary = new ThreadSummary();
        summary.setTotalThreads(threads.size());
        summary.setInferredDeadlocks(groups.inferredDeadlocks.size());
        summary.setJvmReportedDeadlocks(groups.jvmReportedDeadlocks.size());
        summary.setWaitingThreads(groups.waiting.size());
        summary.setRunnableThreads(groups.runnable.size());
        summary.setTimedWaitingThreads(groups.timedWaiting.size());
        summary.setBlockedThreads(groups.blocked.size());
        summary.setTopWaitingMethod(getTopMethod(groups.waiting));
        summary.setTopRunnableMethod(getTopMethod(groups.runnable));
        return summary;
    }
}
