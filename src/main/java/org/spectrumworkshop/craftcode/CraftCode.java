package org.spectrumworkshop.craftcode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.craftcode.Events.Event.ScriptEvent;
import org.spectrumworkshop.craftcode.Events.EventSetters.BlockEvents;
import org.spectrumworkshop.craftcode.Events.EventSetters.PlayerEvents;
import org.spectrumworkshop.craftcode.Interpreters.Interpreter;
import org.spectrumworkshop.tools.SWRegex;

public final class CraftCode extends JavaPlugin implements Listener {

    public static ArrayList<String> plugins = new ArrayList<>();
    public static ArrayList<Actions> startAction = new ArrayList<>();

   @Override
   public void onEnable() {
        plugins.add("TestPlugin");
        String scriptDirectory = System.getProperty("user.dir") + "/config/CraftCodes";
        File directory = new File(scriptDirectory);
        ArrayList<File> files = GetAllScripts(directory);
        for (File file : files) {
            ReadScript(file);
        }
        try {
            Interpreter.Interpret(startAction, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

    @Override
    public void onDisable() {

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

    private void ReadScript(File script) {
        try {
            PluginManager manager = getServer().getPluginManager();
            List<String> lined = Files.readAllLines(Paths.get(script.getPath()));
            String line = SWRegex.ListToString(lined);
            line = line.strip();
            Matcher matcher = SWRegex.CreateMatcher(line, "^plugin~µ~(~®~)");
            if (!matcher.find())
                throw new Exception("Must include Plugin Declaration!!");
            String pluginName = matcher.group(1);
            if (!plugins.contains(pluginName))
                return;
            line = line.replaceFirst(SWRegex.CleanRegex("^plugin~µ~(~®~)"), "").strip();
            ArrayList<String> compressedScript = SWRegex.Compress(line);
            String block = compressedScript.get(0);
            compressedScript.remove(0);
            ScriptEvent event;
            if (ScriptEvent.isEvent(block)) {
                event = ScriptEvent.CreateEvent(block, compressedScript);
                manager.registerEvents(SetUpEvents(event), this);
                block = block.replaceFirst(SWRegex.CleanRegex("^when~µ~(~®~)~µ~with~µ~(~¶~)~µ~do~µ~(~§~)"), "").strip();
            } else if (ScriptEvent.isStart(block)) {
                event = ScriptEvent.CreateStart(block, compressedScript);
                SetUpEvents(event);
                block = block.replaceFirst(SWRegex.CleanRegex("^start~µ~(~§~)"), "").strip();
            } else {
                throw new Exception("No Start or Event Declaration Detected!!");
            }
            matcher = SWRegex.CreateMatcher(block, "^(~®~)");
            while (matcher.find()) {
                if (!matcher.group(1).equals("when"))
                    throw new Exception("Unknown Token: " + matcher.group(1));
                if (!ScriptEvent.isEvent(block))
                    throw new Exception("Incorrect Event Declaration Format");
                event = ScriptEvent.CreateEvent(block, compressedScript);
                manager.registerEvents(SetUpEvents(event), this);
                block = block.replaceFirst(SWRegex.CleanRegex("^when~µ~(~®~)(?:~µ~with~µ~(~¶~))?~µ~do~µ~(~§~)"), "")
                        .strip();
                matcher = SWRegex.CreateMatcher(block, "^(~®~)");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static Listener SetUpEvents(ScriptEvent event) throws Exception {
        String name = event.GetName();
        System.out.println(name);
        if (name.equals("Start")) {
            startAction = event.GetActions();
        }
        else if (name.startsWith("Player")) {
            System.out.println("Here");
            return PlayerEvents.SetEvent(event);
        }
        else if (name.startsWith("Block")) {
            return BlockEvents.SetEvent(event);
        }
        return null;
    }

}