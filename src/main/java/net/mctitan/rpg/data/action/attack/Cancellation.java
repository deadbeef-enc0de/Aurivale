package net.mctitan.rpg.data.action.attack;

import net.mctitan.rpg.data.Modable;
import net.mctitan.rpg.enums.CancelType;
import net.mctitan.rpg.modifier.Modifier;
import net.mctitan.rpg.modifier.types.DamageCancelModifier;

import java.util.HashMap;

public class Cancellation extends Modable {
    private HashMap<CancelType, Double> cancellations = new HashMap<>();

    public Cancellation() {}

    private Cancellation(Cancellation other) {
        for(CancelType canceltype : other.cancellations.keySet()) {
            this.cancellations.put(canceltype, other.value(canceltype));
        }
    }

    public Cancellation clone() { return new Cancellation(this); }

    public double value(CancelType cancelType) {
        if(cancellations.containsKey(cancelType)) {
            return Math.min(1d, cancellations.get(cancelType));
        }
        return 0;
    }

    @Override
    protected void add(Modifier modifier) {
        if(modifier instanceof DamageCancelModifier cancelmod) {
            if(!cancellations.containsKey(cancelmod.canceltype())) {
                cancellations.put(cancelmod.canceltype(), 0d);
            }
            cancellations.put(cancelmod.canceltype(), cancellations.get(cancelmod.canceltype()) + cancelmod.value() / 100d);
        }
    }

    @Override
    protected void remove(Modifier modifier) {
        if(modifier instanceof DamageCancelModifier cancelmod) {
            cancellations.put(cancelmod.canceltype(), cancellations.get(cancelmod.canceltype()) - cancelmod.value() / 100d);
            if(cancellations.get(cancelmod.canceltype()) <= 0) {
                cancellations.remove(cancelmod.canceltype());
            }
        }
    }
}
