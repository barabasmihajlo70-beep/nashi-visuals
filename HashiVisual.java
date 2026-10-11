package com.example.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import java.util.ArrayList;
import java.util.List;

public class NashiVisuals implements ClientModInitializer {
    public static int watermarkX = 10, watermarkY = 10;
    public static int eventX = 10, eventY = 30;
    public static List<String> friendsList = new ArrayList<>();
    public static PlayerEntity currentTarget = null;
    
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            
            int rgb = getRainbowColor(client.world.getTime(), 0);
            drawContext.drawText(client.textRenderer, "§bNashi Visuals §7[§f1.21.4§7]", watermarkX, watermarkY, rgb, true);
            
            drawContext.fill(eventX, eventY, eventX + 115, eventY + 20, 0x70000000);
            drawContext.drawText(client.textRenderer, "⚡ Івент: §eМістик (Активний)", eventX + 5, eventY + 6, 0xFFFFFF, true);
            
            ItemStack chest = client.player.getInventory().getArmorStack(2);
            if (chest.isOf(Items.ELYTRA)) {
                drawContext.drawText(client.textRenderer, "§a[!] Вдягнена Елітра", watermarkX, watermarkY + 110, 0x55FF55, true);
            }
        });

        WorldRenderCallback.LAST.register(context -> {
            renderChinaHat(context);
        });
    }

    public static int getRainbowColor(long ticks, int offset) {
        float hue = ((ticks + offset) % 100) / 100.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
    }

    private void renderChinaHat(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.getPerspective().isFirstPerson()) return;

        double tickDelta = context.tickDelta();
        double x = client.player.prevX + (client.player.getX() - client.player.prevX) * tickDelta - context.camera().getPos().x;
        double y = client.player.prevY + (client.player.getY() - client.player.prevY) * tickDelta - context.camera().getPos().y;
        double z = client.player.prevZ + (client.player.getZ() - client.player.prevZ) * tickDelta - context.camera().getPos().z;

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(x, y + (client.player.isSneaking() ? 1.8 : 2.1), z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-client.player.getViewYRot((float) tickDelta)));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        int rgbCenter = getRainbowColor(client.world.getTime(), 0);
        buffer.vertex(matrices.peek().getPositionMatrix(), 0, 0.25f, 0).color(rgbCenter);

        float radius = 0.6f;
        for (int i = 0; i <= 30; i++) {
            float angle = (float) (i * (Math.PI * 2) / 30);
            float cx = (float) Math.cos(angle) * radius;
            float cz = (float) Math.sin(angle) * radius;
            int rgbEdge = getRainbowColor(client.world.getTime(), i * 4);
            buffer.vertex(matrices.peek().getPositionMatrix(), cx, 0, cz).color(rgbEdge);
        }

        RenderSystem.enableBlend();
        BufferUploader.drawWithShader(buffer.end());
        matrices.pop();
        RenderSystem.disableBlend();
    }
}
