// Date: 5/25/2016 12:12:10 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX




package org.ngs.bigx.minecraft.entity.item;

import org.ngs.bigx.minecraft.BiGX;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;


public class ModelRacingCar extends ModelBase
{
  //fields
    ModelRenderer body;
    ModelRenderer backRightWheel;
    ModelRenderer frontRightWheel;
    ModelRenderer backLeftWheel;
    ModelRenderer frontLeftWheel;
    ModelRenderer Shape1;
  
  public ModelRacingCar()
  {
    textureWidth = 140;
    textureHeight = 75;
    
      body = new ModelRenderer(this, 24, 37);
      body.addBox(0F, 0F, 0F, 16, 6, 28);
      body.setRotationPoint(-8F, 14F, -13F);
      body.setTextureSize(140, 75);
      body.mirror = true;
      setRotation(body, 0F, 0F, 0F);
      backRightWheel = new ModelRenderer(this, 0, 0);
      backRightWheel.addBox(0F, 0F, 0F, 3, 7, 7);
      backRightWheel.setRotationPoint(-9F, 17F, 6F);
      backRightWheel.setTextureSize(140, 75);
      backRightWheel.mirror = true;
      setRotation(backRightWheel, 0F, 0F, 0F);
      frontRightWheel = new ModelRenderer(this, 0, 0);
      frontRightWheel.addBox(0F, 0F, 0F, 3, 7, 7);
      frontRightWheel.setRotationPoint(-9F, 17F, -11F);
      frontRightWheel.setTextureSize(140, 75);
      frontRightWheel.mirror = true;
      setRotation(frontRightWheel, 0F, 0F, 0F);
      backLeftWheel = new ModelRenderer(this, 0, 0);
      backLeftWheel.addBox(0F, 0F, 0F, 3, 7, 7);
      backLeftWheel.setRotationPoint(6F, 17F, 6F);
      backLeftWheel.setTextureSize(140, 75);
      backLeftWheel.mirror = true;
      setRotation(backLeftWheel, -0.0174533F, -0.0174533F, 0F);
      frontLeftWheel = new ModelRenderer(this, 0, 0);
      frontLeftWheel.addBox(0F, 0F, 0F, 3, 7, 7);
      frontLeftWheel.setRotationPoint(6F, 17F, -11F);
      frontLeftWheel.setTextureSize(140, 75);
      frontLeftWheel.mirror = true;
      setRotation(frontLeftWheel, 0F, 0F, 0F);
      Shape1 = new ModelRenderer(this, 48, 14);
      Shape1.addBox(0F, 0F, 0F, 10, 8, 13);
      Shape1.setRotationPoint(-5F, 6F, -4F);
      Shape1.setTextureSize(140, 75);
      Shape1.mirror = true;
      setRotation(Shape1, 0F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    body.render(f5);
    backRightWheel.render(f5);
    frontRightWheel.render(f5);
    backLeftWheel.render(f5);
    frontLeftWheel.render(f5);
    Shape1.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }

}
