package com.volmit.adapt.content.adaptation.seaborrne;

import com.volmit.adapt.api.adaptation.SimpleAdaptation;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.Element;
import com.volmit.adapt.util.Form;
import com.volmit.adapt.util.KList;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SeaborneOxygen extends SimpleAdaptation<SeaborneOxygen.Config> {
    private final KList<Integer> holds = new KList<>();

    public SeaborneOxygen() {
        super("oxygen");
        registerConfiguration(Config.class);
        setDescription("Hold more oxygen!");
        setIcon(Material.GLASS_PANE);
        setBaseCost(getConfig().baseCost);
        setMaxLevel(getConfig().maxLevel);
        setInterval(3750);
        setInitialCost(getConfig().initialCost);
        setCostFactor(getConfig().costFactor);
    }

    @Override
    public void addStats(int level, Element v) {
        v.addLore(C.GREEN + "+ " + Form.pc(getAirBoost(level), 0) + C.GRAY + " Oxygen");
    }

    public int getRealMaxAir(int level) {
        return (int) ((getAirBoost(level) * 300) + 300);
    }

    public double getAirBoost(int level) {
        return getLevelPercent(level) * getConfig().airFactor;
    }

    @Override
    public void onTick() {
        for(Player i : Bukkit.getOnlinePlayers()) {
            if(getLevel(i) > 0) {
                i.setMaximumAir(getRealMaxAir(getLevel(i)));
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
        int baseCost = 3;
        int maxLevel = 5;
        int initialCost = 5;
        double costFactor = 0.525;
        double airFactor = 4.55;
    }
}