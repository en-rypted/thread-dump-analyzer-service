import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.ThreadAnalysis;
import model.ThreadGroupResult;
import model.ThreadInfo;
import model.ThreadSummary;
import service.ThreadAnalyzerService;
import service.ThreadDumpParser;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
//        args = new String[] {
//                "D:\\Projetcs\\vs_code_extension\\ThreadDumpAnalyzer\\ThreadDumpAnalyzerService\\src\\sample_thread_dump.txt"
//        };
        if (args.length < 1) {
            System.err.println("Usage: java -jar thread-dump-cli.jar <threadDumpFile.txt>");
            System.exit(1);
        }

        File file = new File(args[0]);
       ThreadAnalysis threadsAnalysis = ThreadAnalyzerService.getThreadAnalysis(file);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(threadsAnalysis));


    }


}