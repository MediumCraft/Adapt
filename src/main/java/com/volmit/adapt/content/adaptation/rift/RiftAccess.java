package com.volmit.adapt.content.adaptation.rift;

import com.volmit.adapt.Adapt;
import com.volmit.adapt.api.adaptation.SimpleAdaptation;
import com.volmit.adapt.api.recipe.AdaptRecipe;
import com.volmit.adapt.content.item.BoundEnderPearl;
import com.volmit.adapt.nms.NMS;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.Element;
import com.volmit.adapt.util.J;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class RiftAccess extends SimpleAdaptation<RiftAccess.Config> {
    private final List<InventoryView> activeViews = new ArrayList<>();

    public RiftAccess() {
        super("rift-access");
        registerConfiguration(Config.class);
        setDescription("Pull from the void");
        setDisplayName("Rift Access");
        setMaxLevel(1);
        setIcon(Material.NETHER_STAR);
        setBaseCost(getConfig().baseCost);
        setCostFactor(getConfig().costFactor);
        setInitialCost(getConfig().initialCost);
        setInterval(5544);
        registerRecipe(AdaptRecipe.shapeless()
                .key("rift-access")
                .ingredient(Material.ENDER_PEARL)
                .ingredient(Material.COMPASS)
                .result(BoundEnderPearl.io.withData(new BoundEnderPearl.Data(null)))
                .build());
    }

    @NoArgsConstructor
    protected static class Config {
        boolean enabled = true;
        int baseCost = 3;
        double costFactor = 0.2;
        int initialCost = 15;
    }

    @Override
    public void addStats(int level, Element v) {
        v.addLore(C.ITALIC + "Enderpearl + Compass = Reliquary Portkey");
        v.addLore(C.ITALIC + "This item allows you to access containers remotely");
        v.addLore(C.ITALIC + "Once crafted look at item to see usage");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!hasAdaptation(p)) {
            return;
        }


        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemMeta handMeta = hand.getItemMeta();
        Block block = e.getClickedBlock();
        if (p.hasCooldown(hand.getType())) {
            e.setCancelled(true);
            return;
        } else {
            p.setCooldown(Material.ENDER_PEARL, 100);
        }

        if (handMeta == null || handMeta.getLore() == null || !hand.hasItemMeta() || !handMeta.getLore().get(0).equals(C.UNDERLINE + "Portkey")) {
            return;
        }
        xp(p, 1);
        switch (e.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                if (block != null && isStorage(block.getBlockData())) { // Ensure its a container
                    if (p.isSneaking()) { // Binding (Sneak Container)
                        e.setCancelled(true);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 0.50f, 0.22f);
                        linkPearl(p, block);
                    }
                } else if (block != null && !isStorage(block.getBlockData())) {
                    if (p.isSneaking()) { //(Sneak NOT Container)
                        p.sendMessage(C.LIGHT_PURPLE + "That's not a container");
                    } else if (!p.isSneaking() && isBound(hand)) {
                        openPearl(p);
                    }
                }
                e.setCancelled(true);

            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK, LEFT_CLICK_AIR -> {
                if (isBound(hand)) {
                    openPearl(p);
                }
                e.setCancelled(true);

            }
        }
    }

    private void linkPearl(Player p, Block block) {
        NMS.get().sendCooldown(p, Material.ENDER_PEARL, 100);
        vfxSingleCubeOutline(block, Particle.REVERSE_PORTAL);
        ItemStack hand = p.getInventory().getItemInMainHand();

        if (hand.getAmount() == 1) {
            BoundEnderPearl.setData(hand, block);
        } else {
            hand.setAmount(hand.getAmount() - 1);
            ItemStack pearl = BoundEnderPearl.withData(block);
            p.getInventory().addItem(pearl).values().forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
        }
    }

    private void openPearl(Player p) {
        Block b = BoundEnderPearl.getBlock(p.getInventory().getItemInMainHand());
        NMS.get().sendCooldown(p, Material.ENDER_PEARL, 100);

        if (b != null && b.getState() instanceof InventoryHolder holder) {
            activeViews.add(p.openInventory(holder.getInventory()));
            p.playSound(p.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 100f, 0.10f);
            p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 100f, 0.10f);
        }
    }

    private boolean isBound(ItemStack stack) {
        return BoundEnderPearl.getBlock(stack) != null;
    }

    @Override
    public void onTick() {
        J.s(() -> {
            for (int ii = activeViews.size() - 1; ii >= 0; ii--) {
                InventoryView i = activeViews.get(ii);

                if (i.getPlayer().getOpenInventory().equals(i)) {
                    if (i.getTopInventory().getLocation() == null || !isStorage(i.getTopInventory().getLocation().getBlock().getBlockData())) {
                        i.getPlayer().closeInventory();
                        i.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
                        activeViews.remove(ii);
                    }
                } else {
                    i.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
                    activeViews.remove(ii);
                }
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return getConfig().enabled;
    }
}