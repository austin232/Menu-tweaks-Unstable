package com.unstableparty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Draws the custom image once, stretched to fit, instead of vanilla's tiled dirt. */
@Mod.EventBusSubscriber(modid = UnstableParty.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    private static final ResourceLocation BG =
            new ResourceLocation(UnstableParty.MODID, "textures/gui/loading_bg.png");
    private static final int TEX_W = 2560;
    private static final int TEX_H = 1440;

    @SubscribeEvent
    public static void onBackground(ScreenEvent.BackgroundRendered event) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        // In a world, vanilla uses a see-through gradient. The title screen has the panorama.
        if (mc.level != null || screen instanceof TitleScreen) return;

        GuiGraphics g = event.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();

        // "Cover" the screen: crop the image to the screen shape rather than distorting it.
        float texAspect = (float) TEX_W / TEX_H;
        float scrAspect = (float) w / h;
        int u = 0, v = 0, rw = TEX_W, rh = TEX_H;
        if (scrAspect > texAspect) {
            rh = Math.round(TEX_W / scrAspect);
            v = (TEX_H - rh) / 2;
        } else {
            rw = Math.round(TEX_H * scrAspect);
            u = (TEX_W - rw) / 2;
        }
        g.blit(BG, 0, 0, w, h, (float) u, (float) v, rw, rh, TEX_W, TEX_H);
        g.fill(0, 0, w, h, 0x70000000); // slight darken so text stays readable
    }
}
