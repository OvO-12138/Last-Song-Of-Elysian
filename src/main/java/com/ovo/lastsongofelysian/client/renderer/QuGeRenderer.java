package com.ovo.lastsongofelysian.client.renderer;

import com.ovo.lastsongofelysian.client.model.QuGeModel;
import com.ovo.lastsongofelysian.entity.QuGeEntity;
import com.ovo.lastsongofelysian.lastsongofelysian;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class QuGeRenderer extends MobRenderer<QuGeEntity, QuGeModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(lastsongofelysian.MODID, "textures/entity/qu_ge.png");

    public QuGeRenderer(EntityRendererProvider.Context context) {
        super(context, new QuGeModel(context.bakeLayer(QuGeModel.LAYER_LOCATION)), 0.55F);
    }

    @Override
    protected void scale(QuGeEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.05F, 1.05F, 1.05F);
    }

    @Override
    public ResourceLocation getTextureLocation(QuGeEntity entity) {
        return TEXTURE;
    }
}
