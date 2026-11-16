package com.unstableparty;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.InputStream;
import java.util.Optional;

/**
 * Reads vanilla GUI textures when resources load and darkens the grey parts to an ash tone,
 * so no copied Mojang textures need to ship inside the mod.
 */
public class AshRecolor implements ResourceManagerReloadListener {

    // ---- tweak these to change the look ----
    /** How bright the grey stays. 1.0 = vanilla, lower = darker. */
    private static final float BRIGHTNESS = 0.45F;
    /** Slight tint added to the grey (ash = faintly cool/green-grey). */
    private static final float TINT_R = 0.97F, TINT_G = 1.00F, TINT_B = 1.02F;
    /** Pixels with colour spread above this are treated as icons and left alone. */
    private static final int GREY_TOLERANCE = 24;

    private static final String[] TEXTURES = {
            "textures/gui/container/inventory.png",
            "textures/gui/container/generic_54.png",
            "textures/gui/container/crafting_table.png",
            "textures/gui/container/furnace.png",
            "textures/gui/container/blast_furnace.png",
            "textures/gui/container/smoker.png",
            "textures/gui/container/dispenser.png",
            "textures/gui/container/hopper.png",
            "textures/gui/container/shulker_box.png",
            "textures/gui/container/brewing_stand.png",
            "textures/gui/container/anvil.png",
            "textures/gui/container/beacon.png",
            "textures/gui/container/enchanting_table.png",
            "textures/gui/container/grindstone.png",
            "textures/gui/container/loom.png",
            "textures/gui/container/stonecutter.png",
            "textures/gui/container/cartography_table.png",
            "textures/gui/container/smithing.png",
            "textures/gui/container/villager2.png",
            "textures/gui/container/horse.png",
            "textures/gui/recipe_book.png",
            "textures/gui/widgets.png" // only the button rows, see MIN_Y below
    };

    @Override
    public void onResourceManagerReload(ResourceManager rm) {
        Minecraft mc = Minecraft.getInstance();
        for (String path : TEXTURES) {
            ResourceLocation loc = new ResourceLocation("minecraft", path);
            Optional<Resource> res = rm.getResource(loc);
            if (res.isEmpty()) continue;
            // widgets.png also holds the hotbar at the top; buttons start at y = 46.
            int minY = path.endsWith("widgets.png") ? 46 : 0;
            try (InputStream in = res.get().open()) {
                NativeImage img = NativeImage.read(in);
                recolor(img, minY);
                mc.getTextureManager().register(loc, new DynamicTexture(img));
            } catch (Exception e) {
                // If anything goes wrong, vanilla's texture stays in use.
            }
        }
    }

    private static void recolor(NativeImage img, int minY) {
        for (int y = minY; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getPixelRGBA(x, y); // ABGR
                int a = (p >>> 24) & 0xFF;
                if (a == 0) continue;
                int b = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int r = p & 0xFF;
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                if (max - min > GREY_TOLERANCE) continue; // coloured icon, keep as is
                int lum = (r + g + b) / 3;
                int nr = clamp(lum * BRIGHTNESS * TINT_R);
                int ng = clamp(lum * BRIGHTNESS * TINT_G);
                int nb = clamp(lum * BRIGHTNESS * TINT_B);
                img.setPixelRGBA(x, y, (a << 24) | (nb << 16) | (ng << 8) | nr);
            }
        }
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
