package net.mctitan.rpg.data.handbook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Section {
    int index;
    private String name;
    private ArrayList<Component> pages = new ArrayList<>();
    private ArrayList<Section> sections = new ArrayList<>();

    public Section(ConfigurationSection config, int index) {
        this.index = index;
        name = config.getString("name");

        if(config.contains("pages")) {
            for(Object pageobj : config.getList("pages")) {
                if(!(pageobj instanceof Map pagemap)) { continue; }
                MemoryConfiguration pageconfig = new MemoryConfiguration();
                pageconfig.addDefaults(pagemap);

                if (pageconfig.contains("lines")) {
                    TextComponent.Builder builder = Component.text();
                    builder.append(Component.text(name)
                            .clickEvent(ClickEvent.changePage(2))
                            .hoverEvent(HoverEvent.showText(Component.text("Go to Table of Contents")))
                            .decorate(TextDecoration.BOLD)
                    );
                    builder.append(Component.text("\n"));
                    for(String line : pageconfig.getStringList("lines")) {
                        builder.append(Component.text("\n")).append(MiniMessage.miniMessage().deserialize(line));
                    }
                    pages.add(builder.build());
                }
            }
        }

        if(config.contains("sections")) {
            ConfigurationSection sectionsconfig = config.getConfigurationSection("sections");
            int subindex = 1;
            for(String sectionid : sectionsconfig.getKeys(false)) {
                ConfigurationSection sectionconfig = sectionsconfig.getConfigurationSection(sectionid);
                sections.add(new Section(sectionconfig, subindex++));
            }
        }
    }

    public void addtoc(List<Component> lines, int startpage) { addtoc(lines, "", startpage); }
    public void addtoc(List<Component> lines, String prefix, int startpage) {
        lines.add(Component.text(String.format("%s%d %s", prefix, index, name))
                .clickEvent(ClickEvent.changePage(startpage))
                .hoverEvent(HoverEvent.showText(Component.text(String.format("Go to %s", name))))
        );

        int substartpage = startpage + pages.size();
        String subprefix = prefix.isBlank() ? String.format("  %d.", index) : String.format("%s.%d.", prefix, index);
        for(Section subsection : sections) {
            subsection.addtoc(lines, subprefix, substartpage);
            substartpage += subsection.pages();
        }
    }

    public void addpages(BookMeta meta) {
        for(Component page : pages) {
            meta.addPages(page);
        }

        // add pages for subsections
        for(Section subsection : sections) {
            subsection.addpages(meta);
        }
    }

    public String name() { return name; }

    public int sections() {
        int ret = 1;
        for(Section section : sections) {
            ret += section.sections();
        }
        return ret;
    }

    public int pages() {
        int ret = pages.size();
        for(Section section : sections) {
            ret += section.pages();
        }
        return ret;
    }
}
