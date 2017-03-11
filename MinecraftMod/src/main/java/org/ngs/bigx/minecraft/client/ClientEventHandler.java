package org.ngs.bigx.minecraft.client;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.ngs.bigx.minecraft.BiGX;
import org.ngs.bigx.minecraft.BiGXEventTriggers;
import org.ngs.bigx.minecraft.BiGXPacketHandler;
import org.ngs.bigx.minecraft.BiGXTextBoxDialogue;
import org.ngs.bigx.minecraft.CommonEventHandler;
import org.ngs.bigx.minecraft.Context;
import org.ngs.bigx.minecraft.client.area.Area;
import org.ngs.bigx.minecraft.client.area.ClientAreaEvent;
import org.ngs.bigx.minecraft.networking.HandleQuestMessageOnServer;
import org.ngs.bigx.minecraft.quests.QuestLoot;
import org.ngs.bigx.minecraft.quests.QuestLootDatabase;
import org.ngs.bigx.minecraft.quests.QuestStateManager.State;
import org.ngs.bigx.minecraft.quests.QuestStateManager.Trigger;
import org.ngs.bigx.net.gameplugin.common.BiGXNetPacket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.entity.EntityCustomNpc;

public class ClientEventHandler {
	
		private Context context;
		public static KeyBinding keyBindingTogglePedalingMode;
		public static KeyBinding keyBindingMoveForward;
		public static KeyBinding keyBindingMoveBackward;
		public static KeyBinding keyBindingToggleMouse;
		public static KeyBinding keyBindingToggleBike;
		
		private static final double PLAYER_DEFAULTSPEED = 0.10000000149011612D;
		private static final MouseHelper defaultMouseHelper = new MouseHelper();
		
		public ClientEventHandler(Context con) {
			context = con;
		}

		int client_tick = 0;
		int client_tick_count = 0;
		boolean client_tick_bool = true;
		QuestLootDatabase lootDatabase = new QuestLootDatabase();
		boolean enableLock = false, enableBike = true;
		
		private boolean showLeaderboard;
		private int leaderboardSeconds;
		
		@SubscribeEvent
		public void onLivingJump(LivingJumpEvent event) {
			// FOR NOW, disable jumping
			if (enableLock)
				event.entity.motionY = 0;
		}
		
		@SubscribeEvent
		public void onKeyInput(KeyInputEvent event) {
			if (Minecraft.getMinecraft().gameSettings.keyBindForward.isPressed()) {
				Minecraft.getMinecraft().thePlayer.setSprinting(false);
			}
			if (keyBindingToggleMouse.isPressed()) {
				enableLock = !enableLock;
				System.out.println("Movement/Look lock: " + enableLock);
			}
			if (keyBindingToggleBike.isPressed()) {
				enableBike = !enableBike;
				System.out.println("Toggle Bike Movement: " + enableBike);
			}
//			if (keyBindingTogglePedalingMode.isPressed()) {
//				System.out.println("BiGX Shoe Toggle Key Pressed");
//				context.isSubtleModeOn ^= true;
//			}
//			else if (keyBindingMoveForward.isPressed()) {
//				System.out.println("BiGX Forward");
//			}
//			else if (keyBindingMoveBackward.isPressed()) {
//				System.out.println("BiGX Backward");
//			}
		}
		
		@SubscribeEvent
		public void onWorldUnload(WorldEvent.Unload event) {
			
		}
		
		@SubscribeEvent
		public void onAttackEntityEvent(AttackEntityEvent event) {
			EntityCustomNpc target;

			event.target.getEntityId();
			if(event.target.getClass().getName().equals("noppes.npcs.entity.EntityCustomNpc"))
			{
				target = (EntityCustomNpc)event.target;
				
				if(target.display.name.equals("Thief"))
				{
					Random r = new Random();
					int hit = r.nextInt(4)+1;
//					System.out.println("[BiGX] Interact with the Thief HP["+CommonEventHandler.getTheifHealthCurrent()+"/"+CommonEventHandler.getTheifHealthMax()+"] Lv["+CommonEventHandler.getTheifLevel()+"]");
					if (event.entityPlayer.inventory.mainInventory[event.entityPlayer.inventory.currentItem] == null)
						CommonEventHandler.deductTheifHealth(null);
					else
						CommonEventHandler.deductTheifHealth(event.entityPlayer.inventory.mainInventory[event.entityPlayer.inventory.currentItem].getItem());
//					event.entityPlayer.worldObj.playSoundEffect(event.entityPlayer.posX + 0.5D, event.entityPlayer.posY + 0.5D, event.entityPlayer.posZ + 0.5D, "minebike:sounds/hit1", 1.0f, 1.0f);
					event.entityPlayer.worldObj.playSoundAtEntity(event.entityPlayer, "minebike:hit" + hit, 1.0f, 1.0f);
				}
			}
		}
		
		@SubscribeEvent
		public void entityAttacked(LivingHurtEvent event)
		{
			if(event.entityLiving.getClass().getName().equals(EntityLiving.class.getName()))
			{
				EntityLiving attackedEnt = (EntityLiving) event.entityLiving;
				DamageSource attackSource = event.source;
				if (attackSource.getSourceOfDamage() != null)
				{
					EntityPlayer player = (EntityPlayer) attackSource.getSourceOfDamage();
					if(player.getHeldItem() != null)
					{
						ItemStack itemstack = player.getHeldItem();
						if (itemstack.getDisplayName().equals("Baton"))
						{
							NBTTagCompound tag = itemstack.getTagCompound();
							int damageAmmount = tag.getInteger("Damage");
							event.ammount = damageAmmount;
						}
					}
				}
			}
		}
		
		//Called whenever the client ticks
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
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
				
				if(client_tick == 0)
				{
					client_tick_count++;
				}
				
				if(client_tick_count >= 6)
				{
					client_tick_count = 0;
//					GuiMessageWindow.showMessage("Line ONE\nline TWO\nlineThree");
//					GuiMessageWindow.showGoldBar("Line ONE\nline TWO\nlineThree");
//					GuiLeaderBoard.showLeaderBoard(client_tick_bool);
					
					if(client_tick_bool)
						client_tick_bool = false;
					else
						client_tick_bool = true;
				}

				// Handling Player Skills
				EntityPlayer p = Minecraft.getMinecraft().thePlayer;
				// Degrade the current player's speed
				BiGX.characterProperty.decreaseSpeedByTime();
				p.capabilities.setPlayerWalkSpeed(BiGX.characterProperty.getSpeedRate());
				
				//Dealing with showing Sound Message
				BiGXEventTriggers.MusicPlaying(p);
				
				
				//Dealing with locking keys
				if (enableLock) {
					p.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0D);
					//Minecraft.getMinecraft().mouseHelper = BiGX.disableMouseHelper;
					p.jumpMovementFactor = 0f;
					p.capabilities.setPlayerWalkSpeed(0f);
					p.capabilities.setFlySpeed(0f);
					p.setJumping(false);
					p.motionX = 0; p.motionZ = 0;
					p.setSprinting(false);
					p.rotationPitch = 0f;
					p.rotationYaw = 0f;
				} else {
					p.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(PLAYER_DEFAULTSPEED);
					Minecraft.getMinecraft().mouseHelper = defaultMouseHelper;
					p.capabilities.setFlySpeed(0.05F);
					p.capabilities.setPlayerWalkSpeed(0.1F);
				}
				
				/***
				 * FOR NOW THIS LOGIC MOVED TO THE COMMON EVENT HANDLER FOR THE DEMO
				 */
//				// Handling Player heart rate and rpm as mechanics for Chase Quest
//				if (context.suggestedGamePropertiesReady){
//					if (CommonEventHandler.chasingQuestOnGoing)
//					{
//						BiGXPatientPrescription playerperscription = context.suggestedGameProperties.getPlayerProperties().getPatientPrescriptions().get(0);
//						if (playerperscription.getTargetMin() > context.heartrate || context.rotation < 40)
//							BiGX.characterProperty.changeSpeedRateby(-10);
//						else if (playerperscription.getTargetMax() >= context.heartrate || context.rotation > 60 && context.rotation <= 90)
//							BiGX.characterProperty.changeSpeedRateby(10);
//						else if (playerperscription.getTargetMax() < context.heartrate)
//							BiGX.characterProperty.changeSpeedRateby(-5);
//					}
//				}
				
				
				/*
				 * TODO: TEST SHOE ENERGY IDEA (OPTION 3)
				 */
				if(context.isSubtleModeOn)
				{
					if(p.moveForward > 0.9f)
					{
						if(context.shoeEnergy < 0.9f)
						{
							context.shoeEnergy = 0;
							p.setVelocity(0, p.motionY, 0);
						}
						else
						{
							context.shoeEnergy -= 0.8f;
							
							if(context.shoeEnergy < 0)
								context.shoeEnergy = 0;
							
							System.out.println("shoeEnergy[" + context.shoeEnergy + "]");
						}
					}
					else{
						context.shoeEnergy -= 0.6f;
					}
				}
				else {
					float moveSpeed = context.getSpeed()/4;
					double xt = Math.cos(Math.toRadians(p.getRotationYawHead()+90)) * moveSpeed;
					double zt = Math.sin(Math.toRadians(p.getRotationYawHead()+90)) * moveSpeed;
					if (enableBike)
						p.setVelocity(xt, p.motionY, zt);
					
//					float degradation = 0.05f;
//	                if(context.getSpeed() >= 0){
//						context.setSpeed((float) Math.max(0,context.getSpeed()-degradation));
//					}
//					else{
//						context.setSpeed((float) Math.min(0,context.getSpeed()+degradation));
//						System.out.println("Negative Velocity: " + context.getSpeed());
//					}
//					float moveSpeed = context.getSpeed()/4;
//					//getRotationYawHead() returns player's angle in degrees - 90
//					double xt = Math.cos(Math.toRadians(p.getRotationYawHead()+90)) * moveSpeed;
//					double zt = Math.sin(Math.toRadians(p.getRotationYawHead()+90)) * moveSpeed;
//					p.setVelocity(xt, p.motionY, zt);
				}  ////// END OF "TEST SHOE ENERGY IDEA"
				
				
				// Detect if there is area changes where the player is in
				ClientAreaEvent.detectAreaChange(p);
				
				if(ClientAreaEvent.isAreaChange())
				{
					ClientAreaEvent.unsetAreaChangeFlag();
					
					if (ClientAreaEvent.previousArea.type == Area.AreaTypeEnum.EVENT){
						if (ClientAreaEvent.previousArea.name == BiGXTextBoxDialogue.fatherMsg){	
							///Give player message from the friend
							ItemStack b = new ItemStack(Items.written_book);
							NBTTagList pages = new NBTTagList();
							pages.appendTag(new NBTTagString("Your father is in danger. You need to find the one after him and stop him. Go to the cave just outside of town and follow the music. This key will unveil answers."));
							b.stackTagCompound = new NBTTagCompound();
							b.stackTagCompound.setTag("author", new NBTTagString("A friend"));
							b.stackTagCompound.setTag("title", new NBTTagString("A Message"));
							b.stackTagCompound.setTag("pages", pages);
							if (!p.inventory.hasItemStack(b))
								p.inventory.addItemStackToInventory(b);
							///Give player the mysterious key
							ItemStack key = new ItemStack(Item.getItemById(131));
							key.setStackDisplayName("MysteriousKey");
							if (!p.inventory.hasItemStack(key))
								p.inventory.addItemStackToInventory(key);
							
						}
					}
					if (ClientAreaEvent.previousArea.type == Area.AreaTypeEnum.ROOM) {
						if (showLeaderboard) {
							showLeaderboard = false;
							final Timer leaderboardTimer = new Timer();
							
							final TimerTask leaderboardTimerTask = new TimerTask() {
								@Override
								public void run() {
									GuiLeaderBoard.showLeaderBoard(true);
									if (leaderboardSeconds++ >= 5) {
										GuiLeaderBoard.showLeaderBoard(false);
										leaderboardSeconds = 0;
										leaderboardTimer.cancel();
									}
								}
							};
							leaderboardTimer.scheduleAtFixedRate(leaderboardTimerTask, 0, 1000);
						}
					} else {
						showLeaderboard = true;
					}
					if(ClientAreaEvent.previousArea != null)
						GuiMessageWindow.showMessage(ClientAreaEvent.previousArea.name);
					else
						GuiMessageWindow.showMessage("Out of Island Caprona...");
				}
				
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
					BiGXNetPacket packet = new BiGXNetPacket(org.ngs.bigx.dictionary.protocol.Specification.Command.REQ_SEND_DATA, 0x0100, 
							org.ngs.bigx.dictionary.protocol.Specification.DataType.RESISTANCE, buf.array());
					BiGXPacketHandler.sendPacket(context.bigxclient, packet);
				}
				
				//Quest Code

				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				if (context.questManager.hasQuestPopupShown()==false&&context.questManager.getSuggestedQuest()!=null) {
					GuiScreenQuest gui = new GuiScreenQuest(Minecraft.getMinecraft().thePlayer,context.questManager.getSuggestedQuest(),context);
					Minecraft.getMinecraft().displayGuiScreen(gui);
					context.questManager.showQuestPopup();
				}
				
				/***
				 * For now Let's disable this feature for the demo I can see the file\
				 * malformed_QuestLoot.json0.json
				 * malformed_QuestProgress.json0.json
				 */
//				if(this.context.questManager.getQuest() != null)
//				{
//					if(this.context.questManager.getQuest().getStateMachine() == State.QuestInProgress)
//					{
//						boolean isQuestComplete = this.context.questManager.getQuest().checkComplete(player.getDisplayName());
//						if(isQuestComplete)
//						{
//							System.out.println("Quest is Complete.");
//							//Handle the reward
//							QuestLoot sampleLoot = lootDatabase.GetReward("SampleQuest1");
//							BiGX.characterProperty.addCoins(sampleLoot.GetCoins());
//							BiGX.characterProperty.increaseEXPby(sampleLoot.GetExperience());
//							ItemStack[] loot = sampleLoot.GetLoot();
//							for (int i = 0; loot[i]!=null; i++)
//								player.inventory.addItemStackToInventory(loot[i]);
//							
//							HandleQuestMessageOnServer packet = new HandleQuestMessageOnServer(this.context.questManager.getQuest(),Trigger.SuccessQuest);
//							BiGX.network.sendToServer(packet);
//						}
//					}
//				}
				
/*
				/// TODO: Challenge 1: Pushing the player to the lava
				if((player.getEntityWorld().getBlock(1523, 65, 411).getClass()!=BiGX.BlockQuestFRMCheck.getClass()) && ((client_tick%10) == 0) && (context.questManager.getSuggestedQuest()!=null))
				{
					// TODO: Need to revise the code to make quest
					context.questManager.setQuest(context.questManager.getSuggestedQuest());
					context.questManager.setSuggestedQuest(null);
					Quest quest = context.questManager.getQuest();
					quest.triggerStateChange(Trigger.AcceptQuestAndTeleport);
					HandleQuestMessageOnServer packet = new HandleQuestMessageOnServer(quest,Trigger.AcceptQuestAndTeleport);
					BiGX.network.sendToServer(packet);
				}
				
				if((player.getEntityWorld().isRemote) && ((client_tick%10) == 0))
				{
					if(this.context.questManager.getQuest() != null)
					{
						if(this.context.questManager.getQuest().getStateMachine() == State.QuestInProgress)
						{
							int i,j=0;
							int count=0;
							
							/// CHECK THE TREASURE
							for(i=0; i<30; i++)
							{
								for(j=0; j<30; j++)
								{
									if(Minecraft.getMinecraft().theWorld.getBlock(1524 + i, 65, 411 + j).getClass() == BiGX.BlockQuestFRMCheck.getClass())
									{
										count++;
									}
								}
							}
							QuestRunFromMummy.itemsCollected = QuestRunFromMummy.countDeadend - count;
							boolean isQuestDone = this.context.questManager.getQuest().checkComplete(player.getDisplayName());
							if(isQuestDone)
							{
								System.out.println("Quest is Done.");
								HandleQuestMessageOnServer packet = new HandleQuestMessageOnServer(this.context.questManager.getQuest(),Trigger.SuccessQuest);
								BiGX.network.sendToServer(packet);
							}
						}
					}
				}

				GuiStats.tick++;
*/			
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
