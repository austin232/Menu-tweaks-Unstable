package com.unstableparty;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Registers our textures as a built-in resource pack that is always on, sits on top of the
 * pack list, and cannot be moved by the player, so no other pack (including Minecraft's own
 * "Default" pack) can override the Unstable Party look. If anything is missing it logs a
 * warning and does nothing, so it can never crash the game.
 */
@Mod.EventBusSubscriber(modid = UnstableParty.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PackEvents {
    private static Pack pack; // built once, reused

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        try {
            if (pack == null) {
                Path root = ModList.get().getModFileById(UnstableParty.MODID)
                        .getFile().findResource("resourcepacks/unstableparty");
                if (root == null || !Files.exists(root.resolve("pack.mcmeta"))) {
                    System.err.println("[UnstableParty] resourcepacks/unstableparty/pack.mcmeta not found in jar - skipping built-in pack.");
                    return;
                }
                final Pack.ResourcesSupplier supplier = id -> new PathPackResources(id, root, true);
                Pack.Info info = Pack.readPackInfo("builtin/unstableparty", supplier);
                if (info == null) {
                    System.err.println("[UnstableParty] Could not read pack.mcmeta - skipping built-in pack.");
                    return;
                }
                pack = Pack.create(
                        "builtin/unstableparty",
                        Component.literal("Unstable Party"),
                        true,                  // required: always on
                        supplier,
                        info,
                        PackType.CLIENT_RESOURCES,
                        Pack.Position.TOP,     // highest priority
                        true,                  // fixed position: players cannot move it down
                        PackSource.BUILT_IN);
            }
            final Pack p = pack;
            event.addRepositorySource(consumer -> consumer.accept(p));
            System.out.println("[UnstableParty] Registered built-in resource pack.");
        } catch (Exception e) {
            System.err.println("[UnstableParty] Failed to register built-in pack: " + e);
        }
    }
}
