
package org.ngs.bigx.minecraft.client.area;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.ngs.bigx.dictionary.objects.game.BiGXGameTag;
import org.ngs.bigx.dictionary.protocol.Specification;
import org.ngs.bigx.minecraft.BiGXTextBoxDialogue;
import org.ngs.bigx.minecraft.client.area.Area.AreaTypeEnum;
import org.ngs.bigx.minecraft.context.BigxClientContext;
import org.ngs.bigx.minecraft.context.BigxContext;
import org.ngs.bigx.minecraft.context.BigxContext.LOGTYPE;
import org.ngs.bigx.minecraft.quests.QuestTask.QuestActivityTagEnum;
import org.ngs.bigx.net.gameplugin.exception.BiGXInternalGamePluginExcpetion;
import org.ngs.bigx.net.gameplugin.exception.BiGXNetException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class ClientAreaEvent {
	private static boolean areaChangedFlag = true;
	
	public static Area previousArea = null;

	private static ArrayList<Area> areaListEvent = new ArrayList<Area>();
	private static ArrayList<Area> areaListRoom = new ArrayList<Area>();
	private static ArrayList<Area> areaListBuilding = new ArrayList<Area>();
	private static ArrayList<Area> areaListPlace = new ArrayList<Area>();	// e.g CITY(VILLAGE) / CASE / CASTLE... 
	private static ArrayList<Area> areaListWorld = new ArrayList<Area>();
	
	public static boolean isAreaChange()
	{
		return areaChangedFlag;
	}
	
	public static void unsetAreaChangeFlag()
	{
		areaChangedFlag = false;
	}
	
	public static Area detectAreaChange(EntityPlayer player)
	{
		if(previousArea == null)
		{
			previousArea = new Area(Vec3.createVectorHelper(0, 0, 1.0), Vec3.createVectorHelper(0, 0, 0), 
					"Nowhere", AreaTypeEnum.NOTASSIGNED, 0, 0);
		}
		
		Area currentArea = detectCurrentArea(player);
		
		if(currentArea == null)
		{
			currentArea = new Area(Vec3.createVectorHelper(-100000, 0, -100000), Vec3.createVectorHelper(100000, 0, 100000), 
					"Nowhere", AreaTypeEnum.NOTASSIGNED, player.dimension, 0);
		}
		
		if(!compareAreas(currentArea, previousArea))
		{
			areaChangedFlag = true;
			previousArea = currentArea;
		}
		
		return currentArea;
	}
	
	public static void sendLocationGameTag(int locationId)
	{
		// SEND GAME TAG - Quest 0x(GAME TAG[0xFF])(questActivityTagEnum [0xF])
		try {
			int locationTypeEnum = (0xfff & locationId);
			BiGXGameTag biGXGameTag = new BiGXGameTag();
			biGXGameTag.setTagName("" + (Specification.GameTagType.GAMETAG_ID_LOCATION_BEGINNING | locationTypeEnum));
			
			BigxClientContext.sendGameTag(biGXGameTag);

			BigxContext.logWriter(LOGTYPE.TAG, "" + Specification.GameTagType.GAMETAG_ID_LOCATION_BEGINNING + "\t" + locationTypeEnum + "\t");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiGXNetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiGXInternalGamePluginExcpetion e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int getAreaId(String strLocationId) {
		return 0;
	}

	public static void initArea()
	{
		addArea(new Area(Vec3.createVectorHelper(93, 54, -55), Vec3.createVectorHelper(99, 74, -49), 
				BiGXTextBoxDialogue.placeQuestRoom, AreaTypeEnum.ROOM, 0, 1), AreaTypeEnum.ROOM);
		
		addArea(new Area(Vec3.createVectorHelper(88, 74, 241), Vec3.createVectorHelper(91, 80, 244), 
				BiGXTextBoxDialogue.wakeUpMsg, AreaTypeEnum.EVENT, 0, 2), AreaTypeEnum.EVENT);
		
		addArea(new Area(Vec3.createVectorHelper(93, 65, 230), Vec3.createVectorHelper(97, 75, 235), 
				BiGXTextBoxDialogue.policeDirection, AreaTypeEnum.EVENT, 0, 3), AreaTypeEnum.EVENT);
		
		addArea(new Area(Vec3.createVectorHelper(93, 65, -9), Vec3.createVectorHelper(100, 75, 0), 
				BiGXTextBoxDialogue.readSignReminder, AreaTypeEnum.EVENT, 0, 4), AreaTypeEnum.EVENT);
		
		addArea(new Area(Vec3.createVectorHelper(86, 45, 235), Vec3.createVectorHelper(103, 100, 250), 
				BiGXTextBoxDialogue.placeHome, AreaTypeEnum.BUILDING, 0, 5), AreaTypeEnum.BUILDING);
		
		addArea(new Area(Vec3.createVectorHelper(100, 45, 199), Vec3.createVectorHelper(118, 100, 215), 
				BiGXTextBoxDialogue.placeMarket, AreaTypeEnum.BUILDING, 0, 6), AreaTypeEnum.BUILDING);
		
		addArea(new Area(Vec3.createVectorHelper(99, 45, 179), Vec3.createVectorHelper(120, 100, 192), 
				BiGXTextBoxDialogue.placePoliceDepartment, AreaTypeEnum.BUILDING, 0, 7), AreaTypeEnum.BUILDING);
		
		addArea(new Area(Vec3.createVectorHelper(99, 45, 176), Vec3.createVectorHelper(132, 69, 192), 
				BiGXTextBoxDialogue.placeJail, AreaTypeEnum.BUILDING, 0, 8), AreaTypeEnum.BUILDING);
		
		addArea(new Area(Vec3.createVectorHelper(70, 45, 140), Vec3.createVectorHelper(132, 100, 256), 
				BiGXTextBoxDialogue.placeVillage, AreaTypeEnum.PLACE, 0, 9), AreaTypeEnum.PLACE);
		
		addArea(new Area(Vec3.createVectorHelper(93, 54, -55), Vec3.createVectorHelper(99, 74, -8), 
				BiGXTextBoxDialogue.placeCave, AreaTypeEnum.PLACE, 0, 10), AreaTypeEnum.PLACE);
		
		addArea(new Area(Vec3.createVectorHelper(-1000, 40, -1000), Vec3.createVectorHelper(1000, 100, 1000), 
				BiGXTextBoxDialogue.placeContinentPangea, AreaTypeEnum.WORLD, 0, 11), AreaTypeEnum.WORLD);
		
		//Fire Cave Areas
		addArea(new Area(Vec3.createVectorHelper(-5, 60, -5), Vec3.createVectorHelper(5, 70, 11), 
				BiGXTextBoxDialogue.readSignReminder, AreaTypeEnum.ROOM, 105, 12), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(-1000, 40, -1000), Vec3.createVectorHelper(1000, 100, 1000), 
				BiGXTextBoxDialogue.placeCave, AreaTypeEnum.PLACE, 105, 13), AreaTypeEnum.PLACE);
		
		// Tutorial Areas 
		// TODO: CHANGE VECTOR LOCATIONS
		addArea(new Area(Vec3.createVectorHelper(510, 54, -4), Vec3.createVectorHelper(518, 100, 4), 
				BiGXTextBoxDialogue.instructionsPedalForward, AreaTypeEnum.ROOM, 102, 14), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(518, 54, -4), Vec3.createVectorHelper(530, 100, 4), 
				BiGXTextBoxDialogue.instructionsPedalBackward, AreaTypeEnum.ROOM, 102, 15), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(530, 54, -4), Vec3.createVectorHelper(541, 100, 4), 
				BiGXTextBoxDialogue.instructionsJump, AreaTypeEnum.ROOM, 102, 16), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(541, 54, -4), Vec3.createVectorHelper(552, 100, 4), 
				BiGXTextBoxDialogue.instructionsMine, AreaTypeEnum.ROOM, 102, 17), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(552, 54, -4), Vec3.createVectorHelper(563, 100, 4), 
				BiGXTextBoxDialogue.instructionsBuild, AreaTypeEnum.ROOM, 102, 18), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(563, 54, -4), Vec3.createVectorHelper(574, 100, 4), 
				BiGXTextBoxDialogue.instructionsChestOpen, AreaTypeEnum.ROOM, 102, 19), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(574, 54, -4), Vec3.createVectorHelper(585, 100, 4), 
				BiGXTextBoxDialogue.instructionsAttackNPC, AreaTypeEnum.ROOM, 102, 20), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(585, 54, -4), Vec3.createVectorHelper(596, 100, 4), 
				BiGXTextBoxDialogue.instructionsDashJump, AreaTypeEnum.ROOM, 102, 21), AreaTypeEnum.ROOM);
		addArea(new Area(Vec3.createVectorHelper(596, 54, -4), Vec3.createVectorHelper(610, 100, 4), 
				BiGXTextBoxDialogue.instructionsExitTutorial, AreaTypeEnum.ROOM, 102, 22), AreaTypeEnum.ROOM);
	}
	
	public static void addArea(Area areaToAdd, AreaTypeEnum areaType)
	{
		switch(areaType)
		{
		case EVENT:
			areaListEvent.add(areaToAdd);
			break;
		case ROOM:
			areaListRoom.add(areaToAdd);
			break;
		case BUILDING:
			areaListBuilding.add(areaToAdd);
			break;
		case PLACE:
			areaListPlace.add(areaToAdd);
			break;
		case WORLD:
			areaListWorld.add(areaToAdd);
			break;
		default:
			break;
		}
	}
	
	private static Area detectCurrentArea(EntityPlayer player)
	{
		Area returnArea = null;

		if((returnArea = isPlayerInAreaList(player, areaListEvent)) != null)
			return returnArea;
		else if((returnArea = isPlayerInAreaList(player, areaListRoom)) != null)
			return returnArea;
		else if((returnArea = isPlayerInAreaList(player, areaListBuilding)) != null)
			return returnArea;
		else if((returnArea = isPlayerInAreaList(player, areaListPlace)) != null)
			return returnArea;
		else if((returnArea = isPlayerInAreaList(player, areaListWorld)) != null)
			return returnArea;
		else
			return null;
	}
	
	private static Area isPlayerInAreaList(EntityPlayer player, ArrayList<Area> areaList)
	{
		Area returnArea = null;
		
		for(Area area:areaList)
		{
			returnArea = isPlayerInArea(player, area);
			
			if(returnArea != null)
				break;
		}
		
		return returnArea;
	}
	
	private static Area isPlayerInArea(EntityPlayer player, Area area)
	{
		Area returnArea = null;
		
		if(player.dimension != area.dimension)
			return returnArea;
		
		if( (player.posX >= area.pointA.xCoord) && (player.posX <= area.pointB.xCoord) )
		{
			if( (player.posY >= area.pointA.yCoord) && (player.posY <= area.pointB.yCoord) )
			{
				if( (player.posZ >= area.pointA.zCoord) && (player.posZ <= area.pointB.zCoord) )
				{
					returnArea = area;
				}
			}
		}
		
		return returnArea;
	}
	
	private static boolean compareAreas(Area areaA, Area areaB)
	{
		if(areaA == null)
			return false;
		
		if(areaB == null)
			return false;
		
		return areaA.equals(areaB);
	}
}
