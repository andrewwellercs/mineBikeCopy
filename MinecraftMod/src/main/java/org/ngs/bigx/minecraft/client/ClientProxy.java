package org.ngs.bigx.minecraft.client;

import java.awt.Event;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.ngs.bigx.minecraft.BiGXPacketHandler;
import org.ngs.bigx.minecraft.CommonProxy;
import org.ngs.bigx.minecraft.Context;
import org.ngs.bigx.minecraft.Main;
import org.ngs.bigx.minecraft.item.ModItems;
import org.ngs.bigx.net.gameplugin.client.BiGXNetClient;
import org.ngs.bigx.net.gameplugin.client.BiGXNetClientListener;
import org.ngs.bigx.net.gameplugin.common.BiGXNetPacket;
import org.ngs.bigx.net.gameplugin.exception.BiGXInternalGamePluginExcpetion;
import org.ngs.bigx.net.gameplugin.exception.BiGXNetException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy{
	
	ClientEventHandler clientEvents;
	
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		Context context = Main.instance().context;
		clientEvents = new ClientEventHandler(context);
		FMLCommonHandler.instance().bus().register(clientEvents);
    	MinecraftForge.EVENT_BUS.register(clientEvents);
    	MinecraftForge.EVENT_BUS.register(new GuiStats(context,Minecraft.getMinecraft()));
    	MinecraftForge.EVENT_BUS.register(new GuiQuest(context,Minecraft.getMinecraft()));
    	
    	context.initBigX();
    }
	
	public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
		try {
			BiGXPacketHandler.connect(Main.instance().context.bigxclient);
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (BiGXNetException e1) {
			e1.printStackTrace();
		} catch (BiGXInternalGamePluginExcpetion e1) {
			e1.printStackTrace();
		}
	}

}