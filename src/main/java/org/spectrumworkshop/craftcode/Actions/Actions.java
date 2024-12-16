package org.spectrumworkshop.craftcode.Actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.spectrumworkshop.craftcode.Functions.IfCondition;
import org.spectrumworkshop.craftcode.Functions.Loop;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

public class Actions {
    
    public static ArrayList<Actions> CreateActionList(String line) throws Exception {
        line = line.strip();
        ArrayList<String> lines = SWRegex.Compress(line);
        String block = lines.get(0).strip();
        lines.remove(0);
        ArrayList<Actions> actions = new ArrayList<>();

        Matcher matcher = SWRegex.CreateMatcher(block, "^(\\w+)");
        while (matcher.find()) {
            String word = matcher.group(1);
            if (IfCondition.isConditional(block)) {
                IfCondition newIf = IfCondition.CreateConditional(block, lines);
                actions.add(newIf);
                block = block.replaceFirst(SWRegex.CleanRegex("^if~µ~(~¶~)~µ~(~§~)º(?:else~µ~if~µ~(~¶~)~µ~(~§~))*º(?:else~µ~(~§~))?", null), "")
                        .strip();
            } else if (Loop.isLoop(block)) {
                actions.add(Loop.CreateLoop(block, lines));
                block = block.replaceFirst(SWRegex.CleanRegex("^repeater~µ~(~¶~)~µ~(~§~)"), "").strip();
            } else if (StoreAction.isStoreVariable(block)) {
                ArrayList<String> subs = new ArrayList<>(Arrays.asList(
            "∆", "(?:~∂~)|~¶~|~®~|~©~", "∑", "\".*?\"" ));
                actions.add(StoreAction.CreateStoreVariable(block, lines));
                block = block.replaceFirst(SWRegex.CleanRegex("^store~µ~(~∆~|~∑~)~µ~in~µ~(~®~|~∑~)", subs), "")
                        .strip();
            } else if (DotAction.isDotAction(block)) {
                DotAction dot = DotAction.CreateDotAction(block, lines);
                actions.add(dot);
                block = block.replaceFirst(SWRegex.CleanRegex("^(~®~)(?:\\.(?:(~®~)(~¶~)?)+)", null), "").strip();
            } else {
                throw new Exception("Unknown action, starting with: " + word);
            }
            matcher = SWRegex.CreateMatcher(block, "^(\\w+)");
        }
        return actions;
    }

    public ArrayList<Variable> Interpret(ArrayList<Variable> variables) throws Exception {
        return null;
    }

    public String GetType() { return null; }
}
