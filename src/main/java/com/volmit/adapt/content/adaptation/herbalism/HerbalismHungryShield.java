package com.volmit.adapt.content.adaptation.herbalism;

import com.volmit.adapt.api.adaptation.SimpleAdaptation;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.Element;
import com.volmit.adapt.util.Form;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class HerbalismHungryShield extends SimpleAdaptation<HerbalismHungryShield.Config> {
    private final List<Integer> holds = new ArrayList<>();

    public HerbalismHungryShield() {
        super("herbalism-hungry-shield");
        registerConfiguration(Config.class);
        setDescription("Take damage to your hunger before your health");
        setDisplayName("Hungry Shield");
        setIcon(Material.APPLE);
        setBaseCost(getConfig().baseCost);
        setMaxLevel(getConfig().maxLevel);
        setInterval(875);
        setInitialCost(getConfig().initialCost);
        setCostFactor(getConfig().costFactor);
    }

    @Override
    public void onTick() {

    }

    private double getEffectiveness(double factor) {
        return Math.min(getConfig().maxEffectiveness, factor * factor + getConfig().effectivenessBase);
    }

    @Override
    public void addStats(int level, Element v) {
        v.addLore(C.GREEN + "+ " + Form.pc(getEffectiveness(getLevelPercent(level)), 0) + C.GRAY + " Resisted by Hunger");
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player && getLevel((Player) e.getEntity()) > 0) {
            double f = getEffectiveness(getLevelPercent((Player) e.getEntity()));
            double h = e.getDamage() * f;
            double d = e.getDamage() - h;
            Player p = (Player) e.getEntity();

            if(getPlayer(p).consumeFood(h, 6))
            {
                d += h;
                e.setDamage(d);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return getConfig().enabled;
    }

    @NoArgsConstructor
    protected static class Config {
        boolean enabled = true;
        int baseCost = 7;
        int maxLevel = 5;
        int initialCost = 14;
        double costFactor = 0.925;
        double effectivenessBase = 0.15;
        double maxEffectiveness = 0.65;
    }
}
