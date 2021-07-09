package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;

import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.OrientedData;

import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Calendar;

public class ChestInstance extends TileEntityInstance<ChestTileEntity> implements IDynamicInstance {

	private final OrientedData body;
	private final OrientedData lid;

	private final Float2FloatFunction lidProgress;
	private final RenderMaterial renderMaterial;

	public ChestInstance(MaterialManager<?> materialManager, ChestTileEntity tile) {
		super(materialManager, tile);

		Block block = blockState.getBlock();

		ChestType chestType = blockState.contains(ChestBlock.TYPE) ? blockState.get(ChestBlock.TYPE) : ChestType.SINGLE;
		renderMaterial = Atlases.getChestTexture(tile, chestType, isChristmas());

		body = baseInstance()
				.setPosition(getInstancePosition());
		lid = lidInstance()
				.setPosition(getInstancePosition())
				.nudge(0, 9f/16f, 0);

		if (block instanceof ChestBlock) {

			ChestBlock chestBlock = (ChestBlock) block;

			TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> wrapper = chestBlock.getBlockEntitySource(blockState, world, getWorldPosition(), true);

			this.lidProgress = wrapper.apply(ChestBlock.getAnimationProgressRetriever(tile));


		} else {
			lidProgress = $ -> 0f;
		}
	}

	@Override
	public void beginFrame() {
		float progress = lidProgress.get(AnimationTickHolder.getPartialTicks());

		progress = 1.0F - progress;
		progress = 1.0F - progress * progress * progress;

		float angleX = -(progress * ((float) Math.PI / 2F));

		Quaternion quaternion = new Quaternion(Vector3f.POSITIVE_X, angleX, false);

		lid.setRotation(quaternion)
				.setPivot(0, 0, 1f / 16f);

	}

	@Override
	public void updateLight() {
		relight(getWorldPosition(), body, lid);
	}

	@Override
	public void remove() {
		body.delete();
		lid.delete();
	}

	private OrientedData baseInstance() {

		return materialManager.getMaterial(Materials.ORIENTED, renderMaterial.getAtlasId())
				.get("base_" + renderMaterial.getTextureId(), this::getBaseModel)
				.createInstance();
	}

	private OrientedData lidInstance() {

		return materialManager.getMaterial(Materials.ORIENTED, renderMaterial.getAtlasId())
				.get("lid_" + renderMaterial.getTextureId(), this::getLidModel)
				.createInstance();
	}

	private BufferedModel getBaseModel() {

		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(1, 0, 1)
				.end(15, 10, 15)
				.endCuboid()
				.build();
	}

	private BufferedModel getLidModel() {

		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 0)
				.start(1, 0, 1)
				.end(15, 5, 15)
				.endCuboid()
				.cuboid()
				.start(7, -2, 15)
				.size(2, 4, 1)
				.endCuboid()
				.build();
	}

	public static boolean isChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}
}