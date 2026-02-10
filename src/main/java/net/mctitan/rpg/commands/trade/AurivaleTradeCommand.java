package net.mctitan.rpg.commands.trade;

import net.mctitan.rpg.commands.AurivaleBaseCommand;

public class AurivaleTradeCommand extends AurivaleBaseCommand {
    public AurivaleTradeCommand() {
        super("trade");

        this.add(new AurivaleTradeValueCommand());
    }
}
