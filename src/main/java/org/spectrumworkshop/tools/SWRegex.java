package org.spectrumworkshop.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SWRegex {
    
    public static String ListToString(List<String> list) {
        String returnString = "";
        for(String string : list){
            returnString += " " + string;
        }
        return returnString;
    }

    public static Matcher CreateMatcher(String line, String regex) {
        Pattern pattern = Pattern.compile(CleanRegex(regex));
        Matcher matcher = pattern.matcher(line);
        return matcher;
    }

    public static String CleanRegex(String pattern) {
        String returnString = "";
        String[] patternArray = pattern.split("~");
        for (String token : patternArray) {
            switch (token) {
                case "µ" -> {
                    returnString += "\\s*";
                }
                /*case "∆" -> {
                    returnString += "(.*)";
                }
                case "¢" -> {
                    if (!openPar) {
                        returnString += "\\(";
                    } else {
                        returnString += "\\)";
                    }
                    openPar = !openPar;
                }
                case "£" -> {
                    if (!openBrac) {
                        returnString += "\\{";
                    } else {
                        returnString += "\\}";
                    }
                    openBrac = !openBrac;
                }
                case "¥" -> {
                    returnString += "\\((.*?)\\)";
                }*/
                case "®" -> {
                    returnString += "[a-zA-Z_]+[a-zA-Z_0-9]*?";
                }
                case "©" -> {
                    returnString += "-?\\d+(?:\\.\\d+)?";
                }
                case "¶" -> {
                    returnString += "\\(.*?\\)";
                }
                case "§" -> {
                    returnString += "\\{.*?\\}";
                }
                case "∂" -> {
                    returnString += SWRegex.CleanRegex("~®~(?:\\.~®~(?:~¶~)?)+");
                }
                default -> {
                    returnString += token;
                }
            }
        }
        return returnString;
    }

    public static boolean Match(String pattern, String regex) {
        return pattern.matches(CleanRegex(regex));
    }

    public static ArrayList<String> Compress(String text) throws Exception {
        int container = 0; // The index of the next container
        int startIndex = 0; // The start of the next non-container string
        String returnString = ""; // The compressed string to return
        int foundPars = 0; // The amount of open parantheses
        int foundBrac = 0; // The amount of open braces
        int parIndex = -1; // The index of the most recent opened parantheses
        boolean parSearch = false;
        ArrayList<String> returnContainers = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            switch (text.substring(i, i + 1)) {
                case "(" -> {
                    foundPars++;
                    if (parIndex == -1) {
                        parSearch = true;
                        returnString += text.substring(startIndex, i);
                        parIndex = i + 1;
                    }
                }
                case ")" -> {
                    foundPars--;
                    if (foundPars < 0) {
                        throw new Exception("Unclosed Parantheses!!");
                    } else if (foundPars == 0 && parSearch) {
                        returnString += "(Container" + container++ + ")";
                        returnContainers.add(text.substring(parIndex, i));
                        startIndex = i + 1;
                        parIndex = -1;
                    }
                }
                case "{" -> {
                    foundBrac++;
                    if (parIndex == -1) {
                        parSearch = false;
                        returnString += text.substring(startIndex, i);
                        parIndex = i + 1;
                    }
                }
                case "}" -> {
                    foundBrac--;
                    if (foundBrac < 0) {
                        throw new Exception("Unclosed Braces!!");
                    } else if (foundBrac == 0 && !parSearch) {
                        returnString += "{Container" + container++ + "}";
                        returnContainers.add(text.substring(parIndex, i));
                        startIndex = i + 1;
                        parIndex = -1;
                    }
                }
                default -> {
                }
            }
        }
        returnString += text.substring(startIndex);
        returnContainers.add(0, returnString);
        return returnContainers;
    }

    //private ArrayList<String> regexSub;
    private final ArrayList<String> compContainers = new ArrayList<>();
    private ArrayList<SWRegexPrimary> groups;
    private String block;
    private final String regex;

    public SWRegex(String block, String regex) {
        this.block = block; this.regex = regex;
    }

    /* ---------------
       Parsing Methods
       --------------- */

    public String Compress() throws Exception {
        int container = 0; // The index of the next container
        int startIndex = 0; // The start of the next non-container string
        String returnString = ""; // The compressed string to return
        int foundPars = 0; // The amount of open parantheses
        int foundBrac = 0; // The amount of open braces
        int parIndex = -1; // The index of the most recent opened parantheses
        boolean parSearch = false;

        for (int i = 0; i < block.length(); i++) {
            switch (block.substring(i, i + 1)) {
                case "(" -> {
                    foundPars++;
                    if (parIndex == -1) {
                        parSearch = true;
                        returnString += block.substring(startIndex, i);
                        parIndex = i + 1;
                    }
                }
                case ")" -> {
                    foundPars--;
                    if (foundPars < 0) {
                        throw new Exception("Unclosed Parantheses!!");
                    } else if (foundPars == 0 && parSearch) {
                        returnString += "(Container" + container++ + ")";
                        compContainers.add(block.substring(parIndex, i));
                        startIndex = i + 1;
                        parIndex = -1;
                    }
                }
                case "{" -> {
                    foundBrac++;
                    if (parIndex == -1) {
                        parSearch = false;
                        returnString += block.substring(startIndex, i);
                        parIndex = i + 1;
                    }
                }
                case "}" -> {
                    foundBrac--;
                    if (foundBrac < 0) {
                        throw new Exception("Unclosed Braces!!");
                    } else if (foundBrac == 0 && !parSearch) {
                        returnString += "{Container" + container++ + "}";
                        compContainers.add(block.substring(parIndex, i));
                        startIndex = i + 1;
                        parIndex = -1;
                    }
                }
                default -> {
                }
            }
        }
        returnString += block.substring(startIndex);
        return returnString;
    }

    public boolean Find(ArrayList<String> subs) throws Exception {
        String[] newRegex = regex.split("º");
        ArrayList<SWRegexPrimary> tempGroups = new ArrayList<>();
        Matcher matcher;
        for (String newRegexItem : newRegex) {
            boolean mustContain = true;
            String value = CleanRegex(newRegexItem, subs);
            if(!value.startsWith("^")){value = "^" + value;}
            SWRegexPrimary newGroup = new SWRegexPrimary();
            if (value.endsWith("*") || value.endsWith("+")) {
                if (value.endsWith("*")) {mustContain = false; }
                value = value.substring(0, value.length() - 1);
                matcher = CreateMatcher(block, value);
                boolean found = false;
                while (matcher.find()) {
                    found = true;
                    SWRegexSecondary subGroup = new SWRegexSecondary();
                    for (int j = 1; j < matcher.groupCount() + 1; j++) {
                        subGroup.Add(matcher.group(j));
                    }
                    newGroup.Add(subGroup);
                    block = block.replaceFirst(value, "").strip();
                    matcher = SWRegex.CreateMatcher(block, value);
                }
                if (!found) {
                    if (mustContain) return false;
                    //newGroup.isEmpty = true;
                }
            } else {
                if (value.endsWith("?")) {
                    value = value.substring(0, value.length() - 1);
                    mustContain = false;
                }
                matcher = CreateMatcher(block, value);
                if (matcher.find()) {
                    SWRegexSecondary subGroup = new SWRegexSecondary();
                    for (int j = 1; j < matcher.groupCount() + 1; j++) {
                        subGroup.Add(matcher.group(j));
                    }
                    newGroup.container = subGroup;
                    block = block.replaceFirst(value, "").strip();
                } else {
                    if (mustContain) return false;
                    //newGroup.isEmpty = true;
                }
            }
            tempGroups.add(newGroup);
        }
        this.groups = tempGroups;
        return true;

        // Compress and save groupings
        // Top, Middle, Bottom Groupings
        // Top - Main Groups, ? / * / +
        // Middle - Repeating (*/+) Groups 
        // Bottom - Capture Groups 
        
    }
    
    public boolean Find(ArrayList<String> subs, boolean compress) throws Exception {
        if(compress){block = Compress();}
        return Find(subs);
    }

    public ArrayList<String> AsList() {
        ArrayList<String> list = new ArrayList<>();
        for (SWRegexPrimary group : groups) {
            if (group.GetContainer() != null) {
                for (String item : group.GetContainer().GetList()) {
                    list.add(item);
                }
            }
            else {
               for (SWRegexSecondary subGroup : group.GetList()) {
                for (String item : subGroup.GetList()) {
                    list.add(item);
                }
                } 
            }
            
        }
        return list;
    }

    public ArrayList<String> GetContainers() {
        return compContainers;
    }

    /* -------------
       Regex Methods
       ------------- */

    //public void SetSubs(ArrayList<String> sub) { regexSub = sub; }

    private static String CompareToSubs(String string, ArrayList<String> subs) {
        if (subs == null)
            return null;
        for (int i = 0; i < subs.size(); i += 2) {
            if (string.equals(subs.get(i))) {
                return CleanRegex(subs.get(i + 1));
            }
        }
        return null;
    }

    public static String CleanRegex(String pattern, ArrayList<String> subs) {
        String returnString = "";
        String[] patternArray = pattern.split("~");
        for (String token : patternArray) {
            String result = CompareToSubs(token, subs);
            if (result != null) {
                returnString += CleanRegex(result);
                continue;
            }
            switch (token) {
                case "µ" -> {
                    returnString += "\\s*";
                }
                case "®" -> {
                    returnString += "[a-zA-Z_]+[a-zA-Z_0-9]*?";
                }
                case "©" -> {
                    returnString += "-?\\d+(?:\\.\\d+)?";
                }
                case "¶" -> {
                    returnString += "\\(.*?\\)";
                }
                case "§" -> {
                    returnString += "\\{.*?\\}";
                }
                case "∂" -> {
                    returnString += SWRegex.CleanRegex("~®~(?:\\.~®~(?:~¶~)?)+");
                }
                default -> {
                    returnString += token;
                }
            }
        }
        return returnString.replaceAll("º", "");
    }

    /* ----------------
       Grouping Classes
       ---------------- */

    private class SWRegexPrimary {
        private SWRegexSecondary container;
        private final ArrayList<SWRegexSecondary> containers = new ArrayList<>();
        //private boolean isEmpty = false;

        public void Add(SWRegexSecondary group) {
            containers.add(group);
        }
        
        public SWRegexSecondary GetContainer() {
            return container;
        }

        public ArrayList<SWRegexSecondary> GetList() {
            return containers;
        }
    }

    private class SWRegexSecondary {
        private final ArrayList<String> containers = new ArrayList<>();;

        public void Add(String string) {
            containers.add(string);
        }

        public ArrayList<String> GetList() {
            return containers;
        }
    }
}