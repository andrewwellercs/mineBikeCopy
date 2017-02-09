package org.ngs.bigx.minecraft;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.ngs.bigx.dictionary.objects.clinical.BiGXPatientPrescription;
import org.ngs.bigx.dictionary.objects.game.properties.Stage;
import org.ngs.bigx.dictionary.objects.game.properties.StageSettings;
import org.ngs.bigx.dictionary.protocol.Specification.GameTagType;
import org.ngs.bigx.minecraft.Context;
import org.ngs.bigx.minecraft.Context.Resistance;
import org.ngs.bigx.minecraft.networking.HandleQuestMessageOnClient;
import org.ngs.bigx.minecraft.quests.Quest;
import org.ngs.bigx.minecraft.quests.QuestChasing;
import org.ngs.bigx.minecraft.quests.QuestEvent;
import org.ngs.bigx.minecraft.quests.QuestEvent.eventType;
import org.ngs.bigx.minecraft.quests.QuestManager;
import org.ngs.bigx.minecraft.quests.QuestPlayer;
import org.ngs.bigx.minecraft.quests.QuestStateManager.Trigger;
import org.ngs.bigx.minecraft.quests.worlds.QuestTeleporter;
import org.ngs.bigx.minecraft.quests.worlds.WorldProviderFlats;
import org.ngs.bigx.net.gameplugin.exception.BiGXInternalGamePluginExcpetion;
import org.ngs.bigx.net.gameplugin.exception.BiGXNetException;
import org.ngs.bigx.utility.NpcCommand;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.entity.EntityCustomNpc;
import scala.collection.concurrent.Debug;


public class CommonEventHandler {

	static float playerQuestPitch, playerQuestYaw;
	
	int server_tick = 0;
	boolean serverQuestTest = true;
	int serverQuestTestTickCount = 10;
	private static int countdown = 10;
	private static int time = 30;
	private static int timeFallBehind = 0;
	EntityCustomNpc activenpc;
	NpcCommand activecommand;
	public static float initialDist, dist = 0;
	boolean doMakeBlocks;
	float ratio;
	Vec3 returnLocation;
	
	private static Context context;
	EntityCustomNpc npc;
	NpcCommand command;
	
	public static final float chaseRunBaseSpeed = 2.1f; // 157 blocks per 15 seconds!!
	public static float speedchange = 0f;
	private final float chaseRunSpeedInBlocks = 157f/15f;
	public static boolean chasingQuestOnGoing = false;
	public static boolean chasingQuestOnCountDown = false;
	public static int virtualCurrency = 0;
	public static long warningMsgBlinkingTime = System.currentTimeMillis();
	
	private static ArrayList<Integer> questSettings = null;

	private static int chasingQuestInitialPosX = 0;
	private static int chasingQuestInitialPosY = 0;
	private static int chasingQuestInitialPosZ = 0;
	
	private static Timer t = null;
	private static Timer t2 = null;
	private static Timer t3 = null;
	private static QuestTeleporter teleporter = null;
	
	public static int getTime()
	{
		return time;
	}
	
	public static int getCountdown()
	{
		return countdown;
	}
	
	public static int getTimeFallBehind()
	{
		return timeFallBehind;
	}
	
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		BikeWorldData data = BikeWorldData.get(event.world);
		System.out.println(event.world.provider.dimensionId);
		if (event.world.provider.dimensionId == 0){
			System.out.println("DIMENSION ID == 0");
			WorldServer ws = MinecraftServer.getServer().worldServerForDimension(0);
			//WorldServer ws = MinecraftServer.getServer().worldServerForDimension(event.world.provider.dimensionId);
			EntityCustomNpc teleporternpc = NpcCommand.spawnNpc(-60f, 73f, 70f, ws, "Quest Giver");
			NpcCommand teleportercommand = new NpcCommand(teleporternpc);
			teleportercommand.enableMoving(false);
			teleportercommand.makeTransporter(true);
			
			activenpc = teleporternpc;
			activecommand = teleportercommand;
			//allNPCS.SetQuestNPCS();
		}
//		if (event.world.provider.dimensionId == 100){
//			System.out.println("DIMENSION ID == 100");
//			WorldServer ws = MinecraftServer.getServer().worldServerForDimension(100);
//			//WorldServer ws = MinecraftServer.getServer().worldServerForDimension(event.world.provider.dimensionId);
//			EntityCustomNpc thiefnpc = NpcCommand.spawnNpc(0f, 10f, 10f, event.world, "Thief");
//			NpcCommand thiefcommand = new NpcCommand(thiefnpc);
//			thiefcommand.enableMoving(true);
//			thiefcommand.setSpeed(10);
//			thiefcommand.runInDirection(ForgeDirection.EAST);
//			//allNPCS.SetChaseNPC();
//			Timer questTimer = new Timer();
//		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		Context context = BiGX.instance().context;
		
		//TODO: Implement proper cleanup when the game is exited
		//The event which is called by the server shutting down needs to be located and used
		//instead of this event which is called whenever the game is paused.
		
		//context.unloadWorld();
	}
	
	public boolean checkPlayerInArea(final PlayerUseItemEvent.Start event, int x1, int y1, int z1, int x2, int y2, int z2){
		if (event.entityPlayer.posX >= x1 && event.entityPlayer.posX <= x2)
			if (event.entityPlayer.posY >= y1 && event.entityPlayer.posY <= y2)
				if (event.entityPlayer.posZ >= z1 && event.entityPlayer.posZ <= z2)
					return true;
		return false;
	}
	
	public Block getBlockByDifficulty(int difficultyLevel)
	{
		int category = difficultyLevel/205;
		
		switch(category)
		{
		case 0:
			return Blocks.brick_block;
		case 1:
			return Blocks.stone;
		case 2:
			return Blocks.grass;
		case 3:
			return Blocks.gravel;
		case 4:
			return Blocks.sand;
		default:
			return null;
		}
	}
	
	public void goBackToTheOriginalWorld(World world, MinecraftServer worldServer, QuestTeleporter teleporter, Entity entity)
	{		
		chasingQuestOnGoing = false;
		chasingQuestOnCountDown = false;
		timeFallBehind = 0;
		time = 30;
		BiGX.instance().context.setSpeed(0);
		
		if(npc != null)
			command.removeNpc(npc.display.name, WorldProviderFlats.dimID);

		if(t != null)
		{
			t.cancel();
			t = null;
		}
		if(t2 != null)
		{
			t2.cancel();
			t2 = null;
		}
		if(t3 != null)
		{
			t3.cancel();
			t3 = null;
		}
		if(returnLocation == null)
		{
			returnLocation = Vec3.createVectorHelper(-174, 71, 338);
		}
		
		cleanArea(world, chasingQuestInitialPosX, chasingQuestInitialPosY, chasingQuestInitialPosZ, (int)entity.posZ);
		teleporter.teleport(entity, worldServer.worldServerForDimension(0), (int)returnLocation.xCoord, (int)returnLocation.yCoord, (int)returnLocation.zCoord);
	}
	
	public void cleanArea(World world, int initX, int initY, int initZ, int endZ)
	{
		for(int dz=initZ; dz<endZ+64; dz++)
		{
			for(int dx=chasingQuestInitialPosX-16; dx<chasingQuestInitialPosX+16; dx++)
			{
				world.setBlock(dx, initY-1, dz, Blocks.grass);
			}
			world.setBlock(chasingQuestInitialPosX-16, initY, dz, Blocks.air);
			world.setBlock(chasingQuestInitialPosX+16, initY, dz, Blocks.air);
		}
	}
	
	// TODO BUG: Player transports to Quest World when items are used (leave this in for testing purposes)
	@SubscribeEvent
	public void onItemUse(final PlayerUseItemEvent.Start event) {
		final WorldServer ws = MinecraftServer.getServer().worldServerForDimension(WorldProviderFlats.dimID);
		context = BiGX.instance().context;
		if (event.item.getDisplayName().contains("Potion") && checkPlayerInArea(event, -177, 70, 333, -171, 74, 339)
				&& event.entity.dimension != WorldProviderFlats.dimID){
			if (ws != null && event.entity instanceof EntityPlayerMP) {				
				// INIT questSettings ArrayList if there is any
				if(context.suggestedGamePropertiesReady)
				{
					time = 300; 
					questSettings = new ArrayList<Integer>();
					StageSettings stagesettings = context.suggestedGameProperties.getQuestProperties().getStageSettingsArray().get(0);
					List<Stage> stageList = stagesettings.stages;
					
					for(int i=0; i<stageList.size();i++)
					{
						for(int j=0; j<stageList.get(i).duration; j++)
						{
							questSettings.add(stageList.get(i).exerciseSettings);
						}
					}
				}
				else{
					time = 30;
				}

				t = new Timer();
				t2 = new Timer();
				t3 = new Timer();
				
				teleporter = new QuestTeleporter(ws);
				
				returnLocation = Vec3.createVectorHelper(event.entity.posX-1, event.entity.posY-1, event.entity.posZ);
				teleporter.teleport(event.entity, ws, 1, 11, 0);

				chasingQuestInitialPosX = (int)event.entity.posX;
				chasingQuestInitialPosY = 10;
				chasingQuestInitialPosZ = (int)event.entity.posZ;
				
				// Clean up placed blocks when the quest ends
				final List<Vec3> blocks = new ArrayList<Vec3>();
				final TimerTask t3Task = new TimerTask() {
					@Override
					public void run() {
						// Timer for the case where the main char is close enough to catch the bad guy
						if (!Minecraft.getMinecraft().isGamePaused()) {
							for (int x = chasingQuestInitialPosX-16; x < chasingQuestInitialPosX+16; ++x) {
								for (int z = (int)event.entity.posZ+48; z < (int)event.entity.posZ+64; ++z) {
									ws.setBlock(x, chasingQuestInitialPosY-1, z, Blocks.gravel);
									blocks.add(Vec3.createVectorHelper(x, chasingQuestInitialPosY-1, z));
									//ws.setBlock(x, (int)event.entity.posY-1, z-64, Blocks.grass);
								}
							}
						}
					}
				};
				final TimerTask t2Task = new TimerTask() {
					@Override
					public void run() {
						dist = event.entity.getDistanceToEntity(npc);
						ratio = (initialDist-dist)/initialDist;
						if (!Minecraft.getMinecraft().isGamePaused()) {
							time--;
							
							for (int z = (int)event.entity.posZ+32; z < (int)event.entity.posZ+64; ++z) {
								ws.setBlock(chasingQuestInitialPosX-16, chasingQuestInitialPosY, z, Blocks.fence);
								blocks.add(Vec3.createVectorHelper((int)event.entity.posX-16, chasingQuestInitialPosY, z));
								ws.setBlock(chasingQuestInitialPosX+16, chasingQuestInitialPosY, z, Blocks.fence);
								blocks.add(Vec3.createVectorHelper((int)event.entity.posX+16, chasingQuestInitialPosY, z));
							}
							
							if(context.suggestedGamePropertiesReady)
							{
								int currentRelativePosition = (int)event.entity.posZ - chasingQuestInitialPosZ;
								int currentRelativeTime = (int) (currentRelativePosition/chaseRunSpeedInBlocks);
								
								if(currentRelativeTime > questSettings.size())
								{
									currentRelativeTime = questSettings.size() -1;
								}
								
								int currentQuestDifficulty = questSettings.get(currentRelativeTime);
								Block blockByDifficulty = getBlockByDifficulty(currentQuestDifficulty);
								
								for (int x = chasingQuestInitialPosX-16; x < chasingQuestInitialPosX+16; ++x) {
									for (int z = (int)event.entity.posZ+48; z < (int)event.entity.posZ+64; ++z) {
										ws.setBlock(x, chasingQuestInitialPosY-1, z, blockByDifficulty);
										blocks.add(Vec3.createVectorHelper(x, chasingQuestInitialPosY-1, z));
									}
								}
							}
							else{
								if (ratio > 0.4) {
									for (int x = chasingQuestInitialPosX-16; x < chasingQuestInitialPosX+16; ++x) {
										for (int z = (int)event.entity.posZ+48; z < (int)event.entity.posZ+64; ++z) {
											ws.setBlock(x, chasingQuestInitialPosY-1, z, Blocks.gravel);
											blocks.add(Vec3.createVectorHelper(x, chasingQuestInitialPosY-1, z));
											ws.setBlock(x, chasingQuestInitialPosY-1, z-64, Blocks.grass);
										}
									}
								}
							}
						}
						
						// SPEED CHANGE LOGIC BASED ON THE HEART RATE AND THE RPM OF THE PEDALLING
//						if (BiGX.instance().context.getSpeed() < chaseRunBaseSpeed) {
						speedchange = 0f;
						float speedchangerate = 0.05f;
						
						// Handling Player heart rate and rpm as mechanics for Chase Quest
						BiGXPatientPrescription playerperscription = context.suggestedGameProperties.getPlayerProperties().getPatientPrescriptions().get(0);
						if (playerperscription.getTargetMin() > context.heartrate || context.rotation < 40)
							speedchange += speedchangerate;
						else if (playerperscription.getTargetMax() >= context.heartrate || context.rotation > 60 && context.rotation <= 90)
							speedchange += speedchangerate;
						else if (playerperscription.getTargetMax() < context.heartrate)
							speedchange -= speedchangerate/2;
						
						if (context.rpm <= 60 && context.rpm > 40)
							speedchange += speedchangerate;
						else if (context.rpm <= 90 && context.rpm > 60)
							speedchange += speedchangerate;
						else if (context.rpm > 90)
							speedchange += speedchangerate/2;
						else if (context.rpm <= 40)
							speedchange -= speedchangerate;

//							BiGX.instance().context.setSpeed(chaseRunBaseSpeed + speedchange);
//						}
						
						// Quest Success!
						if (ratio > 0.8f) {
							try {
								context.bigxclient.sendGameEvent(GameTagType.GAMETAG_NUMBER_QUESTSTOPSUCCESS, System.currentTimeMillis());
							} catch (SocketException e) {
								e.printStackTrace();
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (BiGXNetException e) {
								e.printStackTrace();
							} catch (BiGXInternalGamePluginExcpetion e) {
								e.printStackTrace();
							}
							goBackToTheOriginalWorld(ws, MinecraftServer.getServer(), teleporter, event.entity);
						}

						// Quest Failure: Fall Behind!!!
						if (ratio < 0) {
							warningMsgBlinkingTime = System.currentTimeMillis();
							timeFallBehind++;
							System.out.println("PUSH! You are too far away!");
						}
						else{
							timeFallBehind = 0;
						}
						
						if(timeFallBehind >= 10)
						{
							try {
								context.bigxclient.sendGameEvent(GameTagType.GAMETAG_NUMBER_QUESTSTOPFAILURE, System.currentTimeMillis());
							} catch (SocketException e) {
								e.printStackTrace();
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (BiGXNetException e) {
								e.printStackTrace();
							} catch (BiGXInternalGamePluginExcpetion e) {
								e.printStackTrace();
							}
							goBackToTheOriginalWorld(ws, MinecraftServer.getServer(), teleporter, event.entity);
						}
					}
				};

				final TimerTask tTask = new TimerTask() {
					@Override
					public void run() {
						if (countdown > 0) {
							chasingQuestOnGoing = true;
							chasingQuestOnCountDown = true;
							System.out.println(countdown-- + "...");
							
							if (countdown == 0)
								dist = 0;
							
							if (countdown == 5) {
								npc = NpcCommand.spawnNpc(0.5f, 11, 20, ws, "Thief");
								command = new NpcCommand(npc);
								command.setSpeed(10);
								command.enableMoving(false);
								command.runInDirection(ForgeDirection.SOUTH);
							}
							else if (countdown == 1)
							{
								try {
									context.bigxclient.sendGameEvent(GameTagType.GAMETAG_NUMBER_QUESTSTART, System.currentTimeMillis());
								} catch (SocketException e) {
									e.printStackTrace();
								} catch (UnknownHostException e) {
									e.printStackTrace();
								} catch (BiGXNetException e) {
									e.printStackTrace();
								} catch (BiGXInternalGamePluginExcpetion e) {
									e.printStackTrace();
								}
							}
							else if(countdown == 9)
							{
								event.entity.rotationPitch = 0f;
								event.entity.rotationYaw = 0f;
							}
							
							
						} else {
							chasingQuestOnCountDown = false;
							System.out.println("GO!");
							command.enableMoving(true);
							countdown = 10;
							t.cancel();
							initialDist = event.entity.getDistanceToEntity(npc);
							t2.scheduleAtFixedRate(t2Task, 0, 1000);
						}
					}
				};
				
				for (int z = (int)event.entity.posZ; z < (int)event.entity.posZ+64; ++z) {
					ws.setBlock(chasingQuestInitialPosX-16, chasingQuestInitialPosY-2, z, Blocks.fence);
					blocks.add(Vec3.createVectorHelper((int)event.entity.posX-16, chasingQuestInitialPosY-2, z));
					ws.setBlock(chasingQuestInitialPosX+16, chasingQuestInitialPosY-2, z, Blocks.fence);
					blocks.add(Vec3.createVectorHelper((int)event.entity.posX+16, chasingQuestInitialPosY-2, z));
				}
				
				t.scheduleAtFixedRate(tTask, 0, 1000);
			}
		}
		else if (event.item.getDisplayName().contains("Potion")
				&& event.entity.dimension == WorldProviderFlats.dimID){
			if (ws != null && event.entity instanceof EntityPlayerMP) {
				teleporter = new QuestTeleporter(ws);
				goBackToTheOriginalWorld(ws, MinecraftServer.getServer(), teleporter, event.entity);
			}
		}
	}
	
	@SubscribeEvent
	public void onDecoratorCreate(DecorateBiomeEvent.Decorate event) {
		if (event.world.provider.getDimensionName() == WorldProviderFlats.dimName) {
			if (event.type == DecorateBiomeEvent.Decorate.EventType.PUMPKIN) {
				// Stops the specified EventType from decorating during chunk generation
				event.setResult(Result.DENY);
			}
		}
	}
	
	public static void makeQuestOnServer()
	{
		//Quest q = BiGX.instance().context.questManager.makeQuest("runFromMummy");
		Quest q = BiGX.instance().context.questManager.makeQuest("timedTrack");
		makeQuestOnServer();
		
		for (WorldServer world:MinecraftServer.getServer().worldServers) {
			List<EntityPlayerMP> playerList = world.playerEntities;
			
			for (EntityPlayerMP player:playerList) {
				q.addPlayer(player.getDisplayName(),BiGX.instance().context);
				World worldd = player.getEntityWorld();
				q.setOriginalWorld(worldd);
			}
			
			q.addQuestInitiator(1524, 65, 411);
		}
		
		System.out.println("[BIGX] CREATE QUEST QUEUEING");
		BiGX.instance().context.questEventQueue.add(new QuestEvent(q, eventType.CreateQuest));
	}
	
	 //Called when the server ticks. Usually 20 ticks a second. 
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) throws Exception {
		if (MinecraftServer.getServer()!=null&&event.phase==TickEvent.Phase.END) {
			boolean isServer = MinecraftServer.getServer().isDedicatedServer();
			server_tick++;
			//20 ticks = 1 second
			if (server_tick==20) {
				server_tick = 0;
			}
			
			
			// Test Purpose Code
			if(this.serverQuestTest)
			{
				if(this.serverQuestTestTickCount > 0)
				{
					if(server_tick == 0)
					{
						this.serverQuestTestTickCount--;
					}
				}
				else{
					this.serverQuestTest = false;
					
					//makeQuestOnServer();
				}
			}
			
			if(BiGX.instance().context.questEventQueue.size() == 0)
			{
				return;
			}
			
			QuestEvent questevent = BiGX.instance().context.questEventQueue.remove();
			Quest quest = questevent.quest;
			eventType type = questevent.type; 
			Collection<QuestPlayer> players = quest.players.values();
			
			switch(type)
			{
			case CreateQuest:
				for (QuestPlayer player : players)
				{
					HandleQuestMessageOnClient packet = new HandleQuestMessageOnClient(quest, Trigger.MakeQuest);
					BiGX.network.sendTo(packet, (EntityPlayerMP) player.getEntity());
				}
				break;
			case NotifyQuestPlayers:
				for (QuestPlayer player : players)
				{
					HandleQuestMessageOnClient packet = new HandleQuestMessageOnClient(quest, Trigger.NotifyQuest);
//					quest.removeQuestInitiator(1524, 65, 411);
					BiGX.network.sendTo(packet, (EntityPlayerMP) player.getEntity());
				}
				break;
			default:
				WorldServer[] worldServers = MinecraftServer.getServer().worldServers;
				
				for (WorldServer world:worldServers) {
					List<EntityPlayerMP> playerList = world.playerEntities;
					for (EntityPlayerMP player:playerList) {
					}
				}
				break;
			}
					

		}
	}
		
	public static float getPlayerPitch() {
		return playerQuestPitch;
	}
	public static float getPlayerYaw() {
		return playerQuestYaw;
	}
	
}