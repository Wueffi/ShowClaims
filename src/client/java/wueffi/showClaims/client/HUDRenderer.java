package wueffi.showClaims.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Colors;

public class HUDRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ShowClaimsClient.visible) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.options.hudHidden) return;

        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();

        ClaimConfig config = ShowClaimsClient.config;
        String text = config.defaultText();

        for (Region region : config.regions()) {
            if (region.contains(px, py, pz)) {
                text = region.label();
                break;
            }
        }

        renderBanner(context, client, text);
    }

    private void renderBanner(DrawContext context, MinecraftClient client, String text) {
        int fontH  = client.textRenderer.fontHeight;
        int textW  = client.textRenderer.getWidth(text);
        int screenW = client.getWindow().getScaledWidth();

        int boxW = 40 + textW;
        int boxH = 10 + fontH;
        int boxX = (screenW - boxW) / 2;
        int boxY = 6;

        context.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xD0193A7A);
        context.fill(boxX, boxY, boxX + boxW, boxY + 1, 0xFF4D6FBF);
        context.fill(boxX, boxY, boxX + 1, boxY + boxH, 0xFF4D6FBF);
        context.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, 0xFF0D1F45);
        context.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, 0xFF0D1F45);
        context.fill(boxX + 10, boxY + 5, boxX + 20, boxY + boxH - 5, 0xFF6B9FFF);

        int textX = boxX + 26;
        int textY = boxY + 5;
        context.drawTextWithShadow(client.textRenderer, text, textX, textY, Colors.WHITE);
    }
}
