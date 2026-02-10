package net.mctitan.rpg.enums;

import org.bukkit.attribute.AttributeModifier.Operation;

public enum Operator {
    FLAT(Operation.ADD_NUMBER, "-", "+", ""),
    SCALER(Operation.ADD_SCALAR, "Decreased", "Increased"),
    MULTIPLIER(Operation.MULTIPLY_SCALAR_1, "Less", "More"),
    SET(null, "-", ""),
    ;

    private Operation operation;
    String negative;
    String positive;
    String base;

    Operator(Operation operation, String negative, String positive) {
        this(operation, negative, positive, positive);
    }

    Operator(Operation operation, String negative, String positive, String base) {
        this.operation = operation;
        this.negative = negative;
        this.positive = positive;
        this.base = base;
    }

    public Operation operation() { return operation; }
    public String string(double value, boolean base) {
        return (value < 0 ?
                this.negative :
                ( base ? this.base : this.positive )
        );
    }
}
