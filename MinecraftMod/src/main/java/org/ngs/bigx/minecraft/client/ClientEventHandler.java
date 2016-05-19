package org.ngs.bigx.minecraft.client;

import java.io.Console;
import java.nio.ByteBuffer;

import org.ngs.bigx.minecraft.BiGXPacketHandler;
import org.ngs.bigx.minecraft.CommonEventHandler;
import org.ngs.bigx.minecraft.Context;
import org.ngs.bigx.minecraft.Main;
import org.ngs.bigx.net.gameplugin.common.BiGXNetPacket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class ClientEventHandler {
	
		private Context context;
		
		public ClientEventHandler(Context con) {
			context = con;
		}
		
		int client_tick = 0;
		int hazard_tick = 0; 
		
		//Called whenever the client ticks
		@SubscribeEvent
		public void onClientTick(TickEvent.ClientTickEvent event) {
			if ((Minecraft.getMinecraft().thePlayer!=null) 
					&& (event.phase==TickEvent.Phase.END)) {
				client_tick++;
				
				// Interval: 50 ms
				if (client_tick==20) {
					client_tick = 0;
				}
				if (client_tick==0||client_tick==10) {
					context.bump = !context.bump;
				}

				// Degrade the current player's speed
				EntityPlayer p = Minecraft.getMinecraft().thePlayer;
				float degradation = 0.05f;
                if(context.getSpeed() >= 0){
					context.setSpeed((float) Math.max(0,context.getSpeed()-degradation));
				}
				else{
					context.setSpeed((float) Math.min(0,context.getSpeed()+degradation));
					System.out.println("Negative Velocity: " + context.getSpeed());
				}
				float moveSpeed = context.getSpeed();
				//getRotationYawHead() returns player's angle in degrees - 90
				double xt = Math.cos(Math.toRadians(p.getRotationYawHead()+90)) * moveSpeed * 4;
				double zt = Math.sin(Math.toRadians(p.getRotationYawHead()+90)) * moveSpeed * 4;
				p.setVelocity(xt, p.motionY, zt);
				
				if( (p.rotationPitch < -45) && (context.getRotationY() < 0) ) {	}
				else if( (p.rotationPitch > 45) && (context.getRotationY() > 0) ) {	}
				else {
					p.rotationPitch += context.getRotationY();
				}
				context.setRotationY(0);
				
				//* EYE TRACKING *//
				//System.out.println("pitch[" + p.rotationPitch + "] yaw[" + p.rotationYaw + "] head[" + p.rotationYawHead + "] X[" + context.getRotationX() + "]");
				if( (context.getRotationX() < .5) && (context.getRotationX() > -.5)) {
					p.rotationYaw += context.getRotationX() / 8;
				}
				else if( (context.getRotationX() < 1.0) && (context.getRotationX() > -1.0)) {
					p.rotationYaw += context.getRotationX() / 4;
				}
				else if( (context.getRotationX() < 1.5) && (context.getRotationX() > -1.5)) {
					p.rotationYaw += context.getRotationX() / 2;
				}
				else {
					p.rotationYaw += context.getRotationX();
				}
				
				context.setRotationX(0);
				
				// Obtain the block under the main character and set the resistance
				Block b = p.getEntityWorld().getBlock((int) p.posX,(int) p.posY-2,(int) p.posZ);
				if (b==Blocks.air) {
					b = p.getEntityWorld().getBlock((int) p.posX, (int) p.posY-3,(int) p.posZ);
				}
				context.block= b;
				float new_resistance = context.resistance;
				if (b!=null) {
					if (context.resistances.containsKey(b)) {
						new_resistance = context.resistances.get(b).getResistance();
					}
					else{
						new_resistance = 1;
					}
				}
				if (new_resistance!=context.resistance) {
					System.out.println("New resistance old[" + new_resistance + "] new[" + context.resistance + "]");
					context.resistance = new_resistance;
					ByteBuffer buf = ByteBuffer.allocate(5);
					buf.put((byte) 0x00);
					buf.put((byte) ((byte) ((int)context.resistance) & 0xFF));
					buf.put((byte) ((byte) (((int)context.resistance) & 0xFF00)>>8));
//					buf.putFloat(context.resistance);
					BiGXNetPacket packet = new BiGXNetPacket(org.ngs.bigx.dictionary.protocol.specification.command.REQ_SEND_DATA, 0x0100, 
							org.ngs.bigx.dictionary.protocol.specification.dataType.RESISTANCE, buf.array());
					BiGXPacketHandler.sendPacket(context.bigxclient, packet);
				}
				if (context.questManager.hasQuestPopupShown()==false&&context.questManager.getSuggestedQuest()!=null) {
					GuiScreenQuest gui = new GuiScreenQuest(Minecraft.getMinecraft().thePlayer,context.questManager.getSuggestedQuest(),context);
					Minecraft.getMinecraft().displayGuiScreen(gui);
					context.questManager.showQuestPopup();
				}

				/// TODO: Challenge 1: Pushing the player to the lava
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				if((player.posX >= 1511)
					&& (player.posX <= 1519)
					&& (player.posY >= 60)
					&& (player.posY <= 70)
					&& (player.posZ >= 1533)
					&& (player.posZ <= 1543))
				{
					System.out.println("Player Location [" + player.posX + "][" + player.posY + "][" + player.posZ + "]");
					player.setPosition(player.posX - 0.025, player.posY, player.posZ);
				}
				
				/// Hazard Attack!
				if(client_tick == 0)
				{
					int clear_tick = 0;
					hazard_tick ++;
					clear_tick = hazard_tick - 8;
					
					player.getEntityWorld().setBlock(1511 + hazard_tick, 68, 1536, Blocks.lava);
					
					if(clear_tick < 0)
					{
						clear_tick += 16;
					}
					
					player.getEntityWorld().setBlock(1511 + clear_tick, 68, 1536, Blocks.air);
				}
				
				if(hazard_tick == 16)
				{
					hazard_tick = 0;
				}
			}
		}
		
		@SubscribeEvent
		public void onGuiOpen(GuiOpenEvent event) {
			if (event.gui instanceof GuiMainMenu) {
				GuiMenu gui = new GuiMenu();
				gui.setContext(context);
				event.gui = gui;
			}
		}
}
