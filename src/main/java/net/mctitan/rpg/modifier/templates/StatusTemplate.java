package net.mctitan.rpg.modifier.templates;

import net.mctitan.rpg.data.status.StatusEffectInstance;
import net.mctitan.rpg.enums.EffectType;
import net.mctitan.rpg.enums.StatusType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.types.StatusModifier;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.Random;

public class StatusTemplate extends ModifierTemplate {
    private EffectType effecttype;
    private StatusType statustype;
    private double levelprecision;
    private double levelflat;
    private int levelsteps;
    private double ticksprecision;
    private double ticksflat;
    private int tickssteps;
    private double radiusprecision;
    private double radiusflat;
    private int radiussteps;

    private StatusTemplate(int rank, boolean base, boolean monster, String id, EffectType effecttype, StatusType statustype,
                           double levelprecision, double levelflat, int levelsteps,
                           double ticksprecision, double ticksflat, int tickssteps,
                           double radiusprecision, double radiusflat, int radiussteps) {
        super(rank, base, monster, id);

        this.effecttype = effecttype;
        this.statustype = statustype;

        this.levelprecision = levelprecision;
        this.levelflat = levelflat;
        this.levelsteps = levelsteps;

        this.ticksprecision = ticksprecision;
        this.ticksflat = ticksflat;
        this.tickssteps = tickssteps;

        this.radiusprecision = radiusprecision;
        this.radiusflat = radiusflat;
        this.radiussteps = radiussteps;
    }

    public static StatusTemplate permanent(int rank, boolean base, boolean monster, String id, EffectType effecttype,
                                           double levelprecision, double levelflat, int levelsteps) {
        return new StatusTemplate(rank, base, monster, id, effecttype, StatusType.PERMANENT,
                levelprecision, levelflat, levelsteps,
                0, 0, 0,
                0, 0, 0);
    }

    public static StatusTemplate temporary(int rank, boolean base, boolean monster, String id, EffectType effecttype,
                                           double levelprecision, double levelflat, int levelsteps,
                                           double ticksprecision, double ticksflat, int tickssteps) {
        return new StatusTemplate(rank, base, monster, id, effecttype, StatusType.TEMPORARY,
                levelprecision, levelflat, levelsteps,
                ticksprecision, ticksflat, tickssteps,
                0,0,0);
    }

    public static StatusTemplate immunity(int rank, boolean base, boolean monster, String id, EffectType effecttype) {
        return new StatusTemplate(rank, base, monster, id, effecttype, StatusType.IMMUNITY,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
    }

    public static StatusTemplate aura(int rank, boolean base, boolean monster, String id, EffectType effecttype,
                                 double levelprecision, double levelflat, int levelsteps,
                                 double radiusprecision, double radiusflat, int radiussteps) {
        return new StatusTemplate(rank, base, monster, id, effecttype, StatusType.AURA,
                levelprecision, levelflat, levelsteps,
                0, 0, 0,
                radiusprecision, radiusflat, radiussteps);
    }

    public static StatusTemplate instance(StatusEffectInstance instance) {
        if(instance.remaining() == PotionEffect.INFINITE_DURATION) {
            return permanent(1, false, false, "status_instance", instance.effect().effecttype(),
                             0, instance.effect().level(), 0);
        } else {
            return temporary(1, false, false, "status_instance", instance.effect().effecttype(),
                             0, instance.effect().level(), 0,
                             0, instance.remaining(), 0);
        }
    }

    public StatusTemplate(PersistentDataContainer pdc) {
        super(pdc);

        if(pdc.has(Key.EFFECT_TYPE_KEY, PersistentDataType.STRING)) {
            String effecttypestr = pdc.get(Key.EFFECT_TYPE_KEY, PersistentDataType.STRING);
            effecttype = EffectType.valueOf(effecttypestr);
        }

        if(pdc.has(Key.STATUS_TYPE_KEY, PersistentDataType.STRING)) {
            String statustypestr = pdc.get(Key.STATUS_TYPE_KEY, PersistentDataType.STRING);
            statustype = StatusType.valueOf(statustypestr);
        }

        if(statustype != StatusType.IMMUNITY && pdc.has(Key.LEVEL_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer levelpdc = pdc.get(Key.LEVEL_KEY, PersistentDataType.TAG_CONTAINER);

            if(levelpdc.has(Key.PRECISION_KEY, PersistentDataType.DOUBLE)) {
                levelprecision = levelpdc.get(Key.PRECISION_KEY, PersistentDataType.DOUBLE);
            }

            if(levelpdc.has(Key.FLAT_KEY, PersistentDataType.DOUBLE)) {
                levelflat = levelpdc.get(Key.FLAT_KEY, PersistentDataType.DOUBLE);
            }

            if(levelpdc.has(Key.STEPS_KEY, PersistentDataType.INTEGER)) {
                levelsteps = levelpdc.get(Key.STEPS_KEY, PersistentDataType.INTEGER);
            }
        }

        if(statustype == StatusType.TEMPORARY && pdc.has(Key.TICKS_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer tickspdc = pdc.get(Key.TICKS_KEY, PersistentDataType.TAG_CONTAINER);

            if(tickspdc.has(Key.PRECISION_KEY, PersistentDataType.DOUBLE)) {
                ticksprecision = tickspdc.get(Key.PRECISION_KEY, PersistentDataType.DOUBLE);
            }

            if(tickspdc.has(Key.FLAT_KEY, PersistentDataType.DOUBLE)) {
                ticksflat = tickspdc.get(Key.FLAT_KEY, PersistentDataType.DOUBLE);
            }

            if(tickspdc.has(Key.STEPS_KEY, PersistentDataType.INTEGER)) {
                tickssteps = tickspdc.get(Key.STEPS_KEY, PersistentDataType.INTEGER);
            }
        }

        if(statustype == StatusType.AURA && pdc.has(Key.RADIUS_KEY, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer radiuspdc = pdc.get(Key.RADIUS_KEY, PersistentDataType.TAG_CONTAINER);

            if(radiuspdc.has(Key.PRECISION_KEY, PersistentDataType.DOUBLE)) {
                radiusprecision = radiuspdc.get(Key.PRECISION_KEY, PersistentDataType.DOUBLE);
            }

            if(radiuspdc.has(Key.FLAT_KEY, PersistentDataType.DOUBLE)) {
                radiusflat = radiuspdc.get(Key.FLAT_KEY, PersistentDataType.DOUBLE);
            }

            if(radiuspdc.has(Key.STEPS_KEY, PersistentDataType.INTEGER)) {
                radiussteps = radiuspdc.get(Key.STEPS_KEY, PersistentDataType.INTEGER);
            }
        }
    }

    public StatusTemplate(ConfigurationSection section) {
        super(section);

        effecttype = EffectType.valueOf(section.getString("effecttype"));
        statustype = StatusType.valueOf(section.getString("statustype"));

        if(statustype != StatusType.IMMUNITY) {
            levelprecision = section.getDouble("level.precision");
            levelflat = section.getDouble("level.flat");
            levelsteps = section.getInt("level.steps");
        }

        if(statustype == StatusType.TEMPORARY) {
            ticksprecision = section.getDouble("ticks.precision");
            ticksflat = section.getDouble("ticks.flat");
            tickssteps = section.getInt("ticks.steps");
        }

        if(statustype == StatusType.AURA) {
            radiusprecision = section.getDouble("radius.precision");
            radiusflat = section.getDouble("radius.flat");
            radiussteps = section.getInt("radius.steps");
        }
    }

    public EffectType effecttype() { return effecttype; }
    public StatusType statustype() { return statustype; }

    public double levelprecision() { return levelprecision; }
    public double levelflat() { return levelflat; }
    public int levelsteps() { return levelsteps; }

    public double ticksprecision() { return ticksprecision; }
    public double ticksflat() { return ticksflat; }
    public int tickssteps() { return tickssteps; }

    public double radiusprecision() { return radiusprecision; }
    public double radiusflat() { return radiusflat; }
    public int radiussteps() { return radiussteps; }

    protected double level(int step) { return Math.round(levelflat() + levelprecision() * step, levelprecision()); }
    protected double ticks(int step) { return Math.round(ticksflat() + ticksprecision() * step, ticksprecision()); }
    protected double radius(int step) { return Math.round(radiusflat() + radiusprecision() * step, radiusprecision()); }

    @Override
    public Modifier modifier(Random random) {
        double level = levelflat();
        if(levelsteps() > 1) { level = level(random.nextInt(levelsteps())); }

        if(statustype() == StatusType.PERMANENT) {
            return StatusModifier.permanent(rank(), this, base(), monster(), effecttype(), level);
        } else if(statustype() == StatusType.TEMPORARY) {
            double ticks = ticksflat();
            if (tickssteps() > 1) {
                ticks = ticks(random.nextInt(tickssteps()));
            }
            return StatusModifier.temporary(rank(), this, base(), monster(), effecttype(), level, ticks);
        } else if(statustype() == StatusType.IMMUNITY) {
            return StatusModifier.immunity(rank(), this, base(), monster(), effecttype());
        } else if(statustype() == StatusType.AURA) {
            double radius = radiusflat();
            if(radiussteps() > 1) { radius = radius(random.nextInt(radiussteps())); }
            return StatusModifier.aura(rank(), this, base(), monster(), effecttype(), level, radius);
        }

        return null;
    }

    @Override
    public String string() {
        double minlevel = level(0);
        double maxlevel = level(levelsteps() - 1);
        double minticks = ticks(0);
        double maxticks = ticks(tickssteps() - 1);
        double minradius = radius(0);
        double maxradius = radius(radiussteps() - 1);

        String format = "Undefined Status {effecttype} {statustype} {level} {tick} {radius}";
        if(statustype() == StatusType.PERMANENT) { format = ""; }
        if(statustype() == StatusType.TEMPORARY) { format = ""; }
        if(statustype() == StatusType.AURA) { format = ""; }

        if(statustype() == StatusType.PERMANENT) { format = "{effecttype} {level}"; }
        else if(statustype() == StatusType.TEMPORARY) { format = "{verb} {effecttype} {level} for {time}"; }
        else if(statustype() == StatusType.IMMUNITY) { format = "{effecttype} Immunity"; }
        else if(statustype() == StatusType.AURA) { format = "{effecttype} {level} {radius} Aura"; }

        String effectstr = Text.enumtoprint(effecttype.name());
        String levelstr = Text.romanint((int)minlevel, effecttype().maxlevel());
        if(levelsteps() > 1 && effecttype().maxlevel() > 1) { levelstr = String.format("(%s - %s)",levelstr, Text.romanint((int)maxlevel, effecttype().maxlevel())); }
        String timestr = String.format("%.02fs", minticks/20);
        if(tickssteps() > 1) { timestr = String.format("(%s - %.02fs)", timestr, maxticks/20); }
        String radiusstr = String.format("%.01fm", minradius);
        if(radiussteps() > 1) { radiusstr = String.format("{%s - %.01fm)", radiusstr, maxradius); }

        return format
                .replace("{effecttype}", effectstr)
                .replace("{statustype}", statustype.name())
                .replace("{level}", levelstr)
                .replace("{time}", timestr)
                .replace("{radius}", radiusstr)
                .replace("{verb}", effecttype().verb())
                .replace("  ", " ")
                .trim();
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.EFFECT_TYPE_KEY, PersistentDataType.STRING, effecttype().name());
        pdc.set(Key.STATUS_TYPE_KEY, PersistentDataType.STRING, statustype().name());

        if(statustype != StatusType.IMMUNITY) {
            PersistentDataContainer levelpdc = pdc.getAdapterContext().newPersistentDataContainer();
            levelpdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, levelprecision());
            levelpdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, levelflat());
            levelpdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, levelsteps());
            pdc.set(Key.LEVEL_KEY, PersistentDataType.TAG_CONTAINER, levelpdc);
        }

        if(statustype() == StatusType.TEMPORARY) {
            PersistentDataContainer tickspdc = pdc.getAdapterContext().newPersistentDataContainer();
            tickspdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, ticksprecision());
            tickspdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, ticksflat());
            tickspdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, tickssteps());
            pdc.set(Key.TICKS_KEY, PersistentDataType.TAG_CONTAINER, tickspdc);
        }

        if(statustype() == StatusType.AURA) {
            PersistentDataContainer radiuspdc = pdc.getAdapterContext().newPersistentDataContainer();
            radiuspdc.set(Key.PRECISION_KEY, PersistentDataType.DOUBLE, radiusprecision());
            radiuspdc.set(Key.FLAT_KEY, PersistentDataType.DOUBLE, radiusflat());
            radiuspdc.set(Key.STEPS_KEY, PersistentDataType.INTEGER, radiussteps());
            pdc.set(Key.RADIUS_KEY, PersistentDataType.TAG_CONTAINER, radiuspdc);
        }
    }
}
