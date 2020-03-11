/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.client.render;

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.common.block.otherworld.IOtherworldBlock;
import com.github.klikli_dev.occultism.registry.OccultismEffects;
import com.github.klikli_dev.occultism.util.Math3DUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThirdEyeEffectRenderer {

    //region Fields
    public static final int MAX_THIRD_EYE_DISTANCE = 10;

    public static final ResourceLocation THIRD_EYE_SHADER = new ResourceLocation(Occultism.MODID,
            "shaders/post/third_eye.json");
    public static final ResourceLocation THIRD_EYE_OVERLAY = new ResourceLocation(Occultism.MODID,
            "textures/overlay/third_eye.png");

    public boolean thirdEyeActiveLastTick = false;

    public Set<BlockPos> uncoveredBlocks = new HashSet<>();
    //endregion Fields

    //region Static Methods
    private static void renderOverlay(RenderGameOverlayEvent.Post event, ResourceLocation texture) {

        MainWindow window = Minecraft.getInstance().getMainWindow();
        RenderSystem.pushMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        Minecraft.getInstance().getTextureManager().bindTexture(texture);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(0.0D, window.getScaledHeight(), -90.0D).tex(0.0f, 1.0f).endVertex();
        buffer.pos(window.getScaledWidth(), window.getScaledHeight(), -90.0D)
                .tex(1.0f, 1.0f).endVertex();
        buffer.pos(window.getScaledWidth(), 0.0D, -90.0D).tex(1.0f, 0.0f).endVertex();
        buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0f, 0.0f).endVertex();
        tessellator.draw();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        RenderSystem.popMatrix();
    }
    //endregion Static Methods

    //region Methods
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.player.world.isRemote)
            this.onThirdEyeTick(event);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HELMET) {
            if (this.thirdEyeActiveLastTick) {

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);

                RenderSystem.color4f(1, 1, 1, 1);

                renderOverlay(event, THIRD_EYE_OVERLAY);

                RenderSystem.color4f(1, 1, 1, 1);
            }
        }
    }

    /**
     * Resets the currently uncovered blocks
     * @param world the world.
     * @param clear true to delete the list of uncovered blocks.
     */
    public void resetUncoveredBlocks(World world, boolean clear){
        for(BlockPos pos : this.uncoveredBlocks) {
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() instanceof IOtherworldBlock) //handle replaced or removed blocks gracefully
                world.setBlockState(pos, state.with(IOtherworldBlock.UNCOVERED, false), 1);
        }
        if(clear)
            this.uncoveredBlocks.clear();
    }

    /**
     * Uncovers the otherworld blocks within MAX_THIRD_EYE_DISTANCE of the player.
     * @param player the player.
     * @param world the world.
     */
    public void uncoverBlocks(PlayerEntity player, World world){
        BlockPos origin = player.getPosition();
        BlockPos.getAllInBoxMutable(origin.add(-MAX_THIRD_EYE_DISTANCE, -MAX_THIRD_EYE_DISTANCE, -MAX_THIRD_EYE_DISTANCE),
                origin.add(MAX_THIRD_EYE_DISTANCE,MAX_THIRD_EYE_DISTANCE,MAX_THIRD_EYE_DISTANCE)).forEach(pos -> {
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() instanceof IOtherworldBlock){
                if(!state.get(IOtherworldBlock.UNCOVERED)){
                    world.setBlockState(pos, state.with(IOtherworldBlock.UNCOVERED, true), 1);
                }
                this.uncoveredBlocks.add(pos.toImmutable());
            }
        });
    }

    public void onThirdEyeTick(TickEvent.PlayerTickEvent event) {
        EffectInstance effect = event.player.getActivePotionEffect(OccultismEffects.THIRD_EYE.get());
        int duration = effect == null ? 0 : effect.getDuration();
        if (duration > 1) {
            if (!this.thirdEyeActiveLastTick) {
                this.thirdEyeActiveLastTick = true;

                //uncover nearby blocks
                this.uncoverBlocks(event.player, event.player.world);

                //load shader
                Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().gameRenderer.loadShader(THIRD_EYE_SHADER));
            }
            else {
                this.uncoverBlocks(event.player, event.player.world);
            }
        }
        else {
            //cover uncovered blocks
            //Try twice, but on the last effect tick, clear the list.
            this.resetUncoveredBlocks(event.player.world, duration == 0);

            if (this.thirdEyeActiveLastTick) {
                this.thirdEyeActiveLastTick = false;
                //unload shader
                Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().gameRenderer.stopUseShader());
            }
        }
    }
}
