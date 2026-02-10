package net.mctitan.rpg.data.handbook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.mctitan.rpg.Aurivale;
import net.mctitan.rpg.data.ItemStack;
import net.mctitan.rpg.util.Item;
import net.mctitan.rpg.util.Text;
import net.mctitan.rpg.util.Version;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class Handbook {
    private static final Handbook instance = new Handbook();
    private ItemStack handbook = Item.cleanstack(Material.WRITTEN_BOOK);
    private Version version = Aurivale.instance().version();
    private ArrayList<Section> sections = new ArrayList<>();

    private Handbook() {
        ConfigurationSection config = Aurivale.instance().getConfig("handbook");
        BookMeta meta = (BookMeta)handbook.bukkitstack().getItemMeta();

        // set book info
        meta.setTitle(config.getString("title"));
        meta.setAuthor(config.getString("author"));
        meta.setGeneration(BookMeta.Generation.valueOf(config.getString("generation")));

        // add title page
        meta.addPages(titlepage(config.getString("title")));

        // load book sections from config
        int totalsections = 0;
        int sectionindex = 1;
        ConfigurationSection sectionsconfig = config.getConfigurationSection("sections");
        for(String sectionid : sectionsconfig.getKeys(false)) {
            ConfigurationSection sectionconfig = sectionsconfig.getConfigurationSection(sectionid);
            Section newsection = new Section(sectionconfig, sectionindex++);
            totalsections += newsection.sections();
            sections.add(newsection);
        }

        // get lines for table of contents
        int startpage = meta.getPageCount() + (totalsections + 12)/13 + 1;
        ArrayList<Component> toclines = new ArrayList<>();
        for(Section section : sections) {
            section.addtoc(toclines, startpage);
            startpage += section.pages();
        }

        // add lines to pages for table of contents
        for(int lineindex = 0; lineindex < toclines.size(); lineindex += 13) {
            int endindex = Math.min(lineindex + 13, toclines.size());
            TextComponent.Builder builder = Component.text();
            builder.append(Component.text("Table of Contents")
                    .decorate(TextDecoration.BOLD)
            );
            for(Component line : toclines.subList(lineindex, endindex)) {
                builder.append(Component.text("\n")).append(line);
            }
            meta.addPages(builder.build());
        }

        // add book sections to book
        for(Section section : sections) {
            section.addpages(meta);
        }

        // set the meta on the
        handbook.bukkitstack().setItemMeta(meta);
    }

    public Component titlepage(String titlepage) {
        // get version
        String versionstr = version.toString();

        // get centered components for titla and version
        Component titlecomponent = Text.bookcenter(titlepage, TextDecoration.BOLD);
        Component versioncomponent = Text.bookcenter(String.format("v%s",versionstr), TextDecoration.ITALIC);

        // build the title page
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("\n\n\n\n\n\n"));
        builder.append(titlecomponent);
        builder.append(Component.text("\n"));
        builder.append(versioncomponent);

        return builder.build();
    }

    public static Handbook instance() { return instance; }

    public ItemStack item() { return handbook.clone(); }
    public Version version() { return version; }
}
