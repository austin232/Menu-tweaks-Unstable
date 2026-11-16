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
 * Registers our textures as a built-in resource pack that is always enabled and sits
 * on top of the pack list. If anything is missing it logs a warning and does nothing,
 * so it can never crash the game.
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
                pack = Pack.readMetaAndCreate(
                        "builtin/unstableparty",
                        Component.literal("Unstable Party"),
                        true,
                        id -> new PathPackResources(id, root, true),
                        PackType.CLIENT_RESOURCES,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN);
            }
            if (pack == null) {
                System.err.println("[UnstableParty] Could not read the built-in pack - skipping.");
                return;
            }
            final Pack p = pack;
            event.addRepositorySource(consumer -> consumer.accept(p));
        } catch (Exception e) {
            System.err.println("[UnstableParty] Failed to register built-in pack: " + e);
        }
    }
}
