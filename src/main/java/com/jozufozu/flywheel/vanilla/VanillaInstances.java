package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.tileentity.TileEntityType;

public class VanillaInstances {

	public static void init() {
		InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();

		r.tile(TileEntityType.CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new)
				.build();
	}
}