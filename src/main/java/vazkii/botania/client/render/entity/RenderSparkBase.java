/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Aug 21, 2014, 5:53:22 PM (GMT)]
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.entity.EntitySparkBase;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class RenderSparkBase<T extends EntitySparkBase> extends EntityRenderer<T> {

	public RenderSparkBase(EntityRendererManager manager) {
		super(manager);
	}

	@Override
	public void doRender(@Nonnull T tEntity, double par2, double par4, double par6, float par8, float par9) {
		TextureAtlasSprite iicon = getBaseIcon(tEntity);

		GlStateManager.pushMatrix();
		GlStateManager.translated(par2, par4, par6);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.05F);

		double time = ClientTickHandler.ticksInGame + par9;
		time += new Random(tEntity.getEntityId()).nextInt();

		float a = 0.1F + (tEntity.isInvisible() ? 0 : 1) * 0.8F;

		GlStateManager.color4f(1F, 1F, 1F, (0.7F + 0.3F * (float) (Math.sin(time / 5.0) + 0.5) * 2) * a);

		float scale = 0.75F + 0.1F * (float) Math.sin(time / 10);
		GlStateManager.scalef(scale, scale, scale);
		bindEntityTexture(tEntity);
		Tessellator tessellator = Tessellator.getInstance();

		GlStateManager.pushMatrix();
		float r = 180.0F - renderManager.playerViewY;
		GlStateManager.rotatef(r, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(-renderManager.playerViewX, 1F, 0F, 0F);
		renderIcon(tessellator, iicon);

		TextureAtlasSprite spinningIcon = getSpinningIcon(tEntity);
		GlStateManager.translatef(-0.02F + (float) Math.sin(time / 20) * 0.2F, 0.24F + (float) Math.cos(time / 20) * 0.2F, 0.005F);
		GlStateManager.scalef(0.2F, 0.2F, 0.2F);
		colorSpinningIcon(tEntity, a);
		renderIcon(tessellator, spinningIcon);

		GlStateManager.popMatrix();
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		renderCallback(tEntity, par9);

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	protected TextureAtlasSprite getBaseIcon(T entity) {
		return MiscellaneousIcons.INSTANCE.sparkWorldIcon;
	}

	private void colorSpinningIcon(T entity, float a) {
		int hex = entity.getNetwork().colorValue;
		int r = (hex & 0xFF0000) >> 16;
		int g = (hex & 0xFF00) >> 8;
		int b = hex & 0xFF;
		GlStateManager.color4f(r / 255F, g / 255F, b / 255F, a);
	}

	protected TextureAtlasSprite getSpinningIcon(T entity) {
		return MiscellaneousIcons.INSTANCE.corporeaIconStar;
	}

	protected void renderCallback(T entity, float pticks) {}

	@Nonnull
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntitySparkBase entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}

	private void renderIcon(Tessellator tess, TextureAtlasSprite icon) {
		float f = icon.getMinU();
		float f1 = icon.getMaxU();
		float f2 = icon.getMinV();
		float f3 = icon.getMaxV();
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;

		tess.getBuffer().begin(GL11.GL_QUADS, ClientProxy.POSITION_TEX_LMAP_NORMAL);
		tess.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(f, f3).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
		tess.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(f1, f3).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
		tess.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(f1, f2).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
		tess.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(f, f2).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
		tess.draw();
	}

}
