package net.mctitan.rpg.util;

import net.mctitan.data.BasicData;

import java.util.ArrayList;

public class Version extends BasicData {
    private ArrayList<Integer> parts = new ArrayList<>();
    private transient String string;

    public Version() {} // for YamlSaver

    public Version(String versionstr) {
        String[] strparts = versionstr.split("\\.");
        for(int index = 0; index < strparts.length; index++) {
            try {
                Integer part = Integer.parseInt(strparts[index]);
                parts.add(part);
            } catch(Exception e) {
                String[] buildinfo = strparts[index].split("b");
                parts.add(Integer.parseInt(buildinfo[0]));
                parts.add(Integer.parseInt(buildinfo[1]));
            }
        }
    }

    public int length() { return parts.size(); }

    public int get(int index) {
        if(index >= parts.size()) {
            return 0;
        }
        return parts.get(index);
    }

    @Override
    public String toString() {
        if(string == null) {
            string = String.join(".", parts.stream().map(Object::toString).toList());
        }
        return string;
    }
}
