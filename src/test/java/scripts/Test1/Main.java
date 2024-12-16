/*package scripts.Test1;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.craftcode.Events.Event.ScriptEvent;
import org.spectrumworkshop.craftcode.Events.EventSetters.BlockEvents;
import org.spectrumworkshop.craftcode.Events.EventSetters.PlayerEvents;
import org.spectrumworkshop.tools.SWRegex;

public class Main {

    public static ArrayList<String> plugins = new ArrayList<>();
    public static ArrayList<Actions> startAction = new ArrayList<>();

    public static void main(String args[]) throws Exception {

        //List<String> plugins = plugin.getConfig().getStringList("activated-plugins");
        plugins.add("TestPlugin");
        String scriptDirectory = System.getProperty("user.dir") + "/src/test/java/scripts";
        File directory = new File(scriptDirectory);
        ArrayList<File> files = GetAllScripts(directory);
        for (File file : files) { ReadScript(file); }
    }
    
    private static ArrayList<File> GetAllScripts(File scriptDirectory) {
        ArrayList<File> scriptList = new ArrayList<>();
        File[] files = scriptDirectory.listFiles();
        for (File file : files) {
            String filePath = file.getPath();
            if (filePath.endsWith(".craft")) {
                scriptList.add(file);
            } else if (Files.isDirectory(Paths.get(filePath))) {
                for (File subFile : GetAllScripts(file)) {
                    String subFilePath = subFile.getPath();
                    if (subFilePath.endsWith(".craft")) {
                        scriptList.add(subFile);
                    }
                }
            }
        }
        return scriptList;
    }

    private static void ReadScript(File script) throws Exception {
        PluginManager manager;
        List<String> lined = Files.readAllLines(Paths.get(script.getPath()));
        String line = SWRegex.ListToString(lined);
        line = line.strip();
        Matcher matcher = SWRegex.CreateMatcher(line, "^(?:plugin~µ~(~®~))");
        if (!matcher.find())
            throw new Exception("Must include Plugin Declaration!!");
        String pluginName = matcher.group(1);
        if (!plugins.contains(pluginName))
            return;
        line = line.replaceFirst("^(?:plugin!µ!(!®!))", "").strip();
        ArrayList<String> compressedScript = SWRegex.Compress(line);
        String block = compressedScript.get(0);
        compressedScript.remove(0);
        ScriptEvent event;
        if (ScriptEvent.isEvent(block)) {
            event = ScriptEvent.CreateEvent(block, compressedScript);
            manager.registerEvents(SetUpEvents(event), this);
            block = block.replaceFirst("^when~µ~(~®~)~µ~with~µ~(~¶~)~µ~do~µ~(~§~)", "");
            for (int i = 0; i <= 2; i++) {
                compressedScript.remove(0);
            }
        } else if (ScriptEvent.isStart(block)) {
            event = ScriptEvent.CreateStart(compressedScript);
            manager.registerEvents(SetUpEvents(event), this);
            block = block.replaceFirst("^start~µ~(~§~)", "");
            compressedScript.remove(0);
        } else {
            throw new Exception("No Start or Event Declaration Detected!!");
        }
        matcher = SWRegex.CreateMatcher(line, "^(//w+)");
        while (matcher.find()) {
            if (!matcher.group(1).equals("when"))
                throw new Exception("Unknown Token: " + matcher.group(1));
            if (!ScriptEvent.isEvent(line))
                throw new Exception("Incorrect Event Declaration Format");
            event = ScriptEvent.CreateEvent(block, compressedScript);
            manager.registerEvents(SetUpEvents(event), this);
            block = block.replaceFirst("^when~µ~(~®~)~µ~with~µ~(~¶~)~µ~do~µ~(~§~)", "");
            for (int i = 0; i <= 2; i++) {
                compressedScript.remove(0);
            }
        }
    }
    
    private static Listener SetUpEvents(ScriptEvent event) throws Exception {
        String name = event.toString();
        if (name.startsWith("Player")) {
            return PlayerEvents.SetEvent(event);
        }
        if (name.startsWith("Block")) {
            return BlockEvents.SetEvent(event);
        }
        return null;
    }

}*/