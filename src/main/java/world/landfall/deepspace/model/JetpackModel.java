package world.landfall.deepspace.model;// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class JetpackModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "jetpack"), "main");
	private final ModelPart jetpack;
	private final ModelPart LeftSide;
	private final ModelPart continer;
	private final ModelPart RightSide;
	private final ModelPart continer2;

	public JetpackModel(ModelPart root) {
		this.jetpack = root.getChild("jetpack");
		this.LeftSide = this.jetpack.getChild("LeftSide");
		this.continer = this.LeftSide.getChild("continer");
		this.RightSide = this.jetpack.getChild("RightSide");
		this.continer2 = this.RightSide.getChild("continer2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition jetpack = partdefinition.addOrReplaceChild("jetpack", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -22.0F, 1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(14, 0).addBox(-1.0F, -23.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(6, 16).addBox(-2.0F, -15.0F, 2.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 4).addBox(2.0F, -22.0F, 2.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(-4.0F, -22.0F, 2.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition LeftSide = jetpack.addOrReplaceChild("LeftSide", CubeListBuilder.create().texOffs(20, 6).addBox(-2.5F, -2.1F, -0.5F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(6.8F, -19.2F, 3.0F));

		PartDefinition cube_r1 = LeftSide.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 20).addBox(-0.5F, -0.5F, 0.2F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6669F, -1.0331F, -0.7F, 0.0F, 0.0F, -0.4189F));

		PartDefinition continer = LeftSide.addOrReplaceChild("continer", CubeListBuilder.create().texOffs(6, 18).addBox(-1.6F, 1.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-1.6F, -2.5F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.4F, 0.0F, 0.0F, 0.1047F));

		PartDefinition RightSide = jetpack.addOrReplaceChild("RightSide", CubeListBuilder.create().texOffs(10, 20).addBox(0.5F, -2.1F, -0.5F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.8F, -19.2F, 3.0F));

		PartDefinition cube_r2 = RightSide.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 8).addBox(-1.5F, -0.5F, 0.2F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6669F, -1.0331F, -0.7F, 0.0F, 0.0F, 0.4189F));

		PartDefinition continer2 = RightSide.addOrReplaceChild("continer2", CubeListBuilder.create().texOffs(20, 4).addBox(-1.4F, 1.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 10).addBox(-1.4F, -2.5F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.4F, 0.0F, 0.0F, -0.1047F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        jetpack.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}