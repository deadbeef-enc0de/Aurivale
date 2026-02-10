package net.mctitan.rpg.data;

import net.mctitan.data.BaseData;
import net.mctitan.data.UUID;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Party extends BaseData {
    private UUID owner;
    private HashSet<UUID> members = new HashSet<>();

    public Party() {}

    public Party(Player owner) {
        this.owner = owner.uuid;
    }

    public boolean member(Player member) { return members.contains(member.uuid); }
    public void addmember(Player member) { members.add(member.uuid); }
    public void removemember(Player member) { members.remove(member.uuid); }

    public Player owner() { return DataManager.instance().player(owner); }
    public Set<Player> members() {
        return members.stream()
                .map(uuid -> DataManager.instance().player(uuid))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
