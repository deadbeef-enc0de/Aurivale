package net.mctitan.rpg.modifier.types;

import net.kyori.adventure.text.Component;
import net.mctitan.rpg.data.Entity;
import net.mctitan.rpg.data.Player;
import net.mctitan.rpg.enums.Operator;
import net.mctitan.rpg.enums.SkillType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.ModifierTemplate;
import net.mctitan.rpg.modifier.templates.SkillTemplate;
import net.mctitan.rpg.util.Key;
import net.mctitan.rpg.util.Math;
import net.mctitan.rpg.util.Text;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SkillModifier extends Modifier {
    private SkillType skill;
    private Operator operator;
    private double value;

    public SkillModifier() {} // for YamlSaver

    public SkillModifier(int rank, ModifierTemplate template, boolean base, boolean monster,
                         SkillType skill, Operator operator, double value) {
        super(rank, template, base, monster);

        this.skill = skill;
        this.operator = operator;
        this.value = value;
    }

    public SkillModifier(PersistentDataContainer pdc) {
        super(pdc, new SkillTemplate(pdc.get(Key.TEMPLATE_KEY, PersistentDataType.TAG_CONTAINER)));

        if(pdc.has(Key.SKILL_KEY, PersistentDataType.STRING)) {
            String skillstr = pdc.get(Key.SKILL_KEY, PersistentDataType.STRING);
            skill = SkillType.valueOf(skillstr);
        }

        if(pdc.has(Key.OPERATOR_KEY, PersistentDataType.STRING)) {
            String operatorstr = pdc.get(Key.OPERATOR_KEY, PersistentDataType.STRING);
            operator = Operator.valueOf(operatorstr);
        }

        if(pdc.has(Key.VALUE_KEY, PersistentDataType.DOUBLE)) {
            value = pdc.get(Key.VALUE_KEY, PersistentDataType.DOUBLE);
        }
    }

    public SkillType skill() { return skill; }
    public Operator operator() { return operator; }
    public double value() { return value; }

    @Override
    public Component text() {
        String skillstr = Text.enumtoprint(skill().toString());

        if(operator() == Operator.FLAT) {
            String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
            return Component.text(String.format("%s to %s", valuestr, skillstr));
        } else if(operator() == Operator.SCALER || operator() == Operator.MULTIPLIER) {
            String valuestr = String.format("%.00f", Math.abs(value()));
            return Component.text(String.format("%s%% %s %s", valuestr, operator().string(value(), base()), skillstr));
        } else if(operator() == Operator.SET) {
            String valuestr = String.format("%s%.00f", operator().string(value(), base()), Math.abs(value()));
            return Component.text(String.format("%s is %s", skillstr, valuestr));
        }

        return Component.text(String.format("Undefined %s %s %.03f", skillstr, operator().name(), value));
    }

    @Override
    public void apply(Entity entity) {
        if(!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        synchronized (entity) {
            for(SkillType type : skill().children()) {
                player.skill(type).apply(this);
            }
        }
    }

    @Override
    public void unapply(Entity entity) {
        if(!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        synchronized (entity) {
            for(SkillType type : skill().children()) {
                player.skill(type).unapply(this);
            }
        }
    }

    @Override
    public void save(PersistentDataContainer pdc) {
        super.save(pdc);

        pdc.set(Key.SKILL_KEY, PersistentDataType.STRING, skill().name());
        pdc.set(Key.OPERATOR_KEY, PersistentDataType.STRING, operator().name());
        pdc.set(Key.VALUE_KEY, PersistentDataType.DOUBLE, value());
    }
}
