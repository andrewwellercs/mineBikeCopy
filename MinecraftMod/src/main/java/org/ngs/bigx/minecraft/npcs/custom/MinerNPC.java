package org.ngs.bigx.minecraft.npcs.custom;

import org.ngs.bigx.minecraft.client.GuiMessageWindow;

import org.ngs.bigx.minecraft.quests.custom.CustomQuest;
import org.ngs.bigx.minecraft.quests.custom.JillQuest;
import org.ngs.bigx.minecraft.quests.custom.MinerQuest;
import org.ngs.bigx.minecraft.quests.custom.SoccerQuest;
import org.ngs.bigx.minecraft.quests.custom.helpers.CustomQuestAbstract;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public class MinerNPC extends CustomNPCAbstract
{
	
	public static final String NAME = "Tobuscus";
	public static final Vec3 LOCATION = Vec3.createVectorHelper(82, 70, 230);
	public static final String TEXTURE = "customnpcs:textures/entity/humanmale/OfficialTobuscusMinecraftSkin.png";
	private CustomQuestAbstract quest;

	public MinerNPC()
	{
		name = NAME;
		location = LOCATION;
		texture = TEXTURE;
		quest = new MinerQuest();
		//quest = new JillQuest();
	}
	public void onInteraction(EntityPlayer player, EntityInteractEvent event) {
		// TODO Auto-generated method stub
		GuiMessageWindow.showMessage("My name is Tobuscus!\nI need ores to build a house!\nWanna help me?");
		quest.start();
	}
}
	