package org.ngs.bigx.minecraft.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.collections.SetChangeListener;

import org.lwjgl.opengl.GL11;
import org.ngs.bigx.minecraft.BiGX;
import org.ngs.bigx.minecraft.context.BigxClientContext;
import org.ngs.bigx.minecraft.quests.Quest;
import org.ngs.bigx.minecraft.quests.QuestException;
import org.ngs.bigx.minecraft.quests.QuestTaskFightAndChasing;

import com.ibm.icu.impl.ICUService.Key;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import noppes.npcs.client.gui.player.GuiQuestLog;

public class GuiChapter extends GuiScreen {
	private Minecraft mc;
	private BigxClientContext context;
	
	private static int chapterNumber = 1;
	private static String chapterText = "";
	private static String title = "";
	private static String subtitleLine1 = "";
	private static String subtitleLine2 = "";
	private static String subtitleLineShort = "";
	
	public static final String STR_CHAPTER_1_TITLE = "WAKE UP!";
	public static final String STR_CHAPTER_1_SUBTITLE_LINE_1 = "Go talk to father";
	public static final String STR_CHAPTER_1_SUBTITLE_LINE_2 = "";
	public static final String STR_CHAPTER_1_SUBTITLE_LINE_SHORT = "Go talk to father";
	
	public static final String STR_CHAPTER_2_TITLE = "NOT READY YET!";
	public static final String STR_CHAPTER_2_SUBTITLE_LINE_1 = "Go talk to a trainer home";
	public static final String STR_CHAPTER_2_SUBTITLE_LINE_2 = "";
	public static final String STR_CHAPTER_2_SUBTITLE_LINE_SHORT = "Go talk to a trainer";
	
	public static final String STR_CHAPTER_3_TITLE = "JOB EARNED - POLICE OFFICER";
	public static final String STR_CHAPTER_3_SUBTITLE_LINE_1 = "Go talk to a police officer";
	public static final String STR_CHAPTER_3_SUBTITLE_LINE_2 = "at police station";
	public static final String STR_CHAPTER_3_SUBTITLE_LINE_SHORT = "Go talk to a police officer";
	
	public static boolean didOneSecondPassed = false;

	public static void proceedToNextChapter()
	{
		chapterNumber ++;
		GuiChapter.setChapter(chapterNumber);
	}
	
	public GuiChapter(Minecraft mc, int chapterNumber) {
		super();
		didOneSecondPassed = false;
		this.mc = mc;
		this.chapterNumber = chapterNumber;
		setTitleAndSubTitles(chapterNumber);
	}
	
	public GuiChapter(BigxClientContext c,Minecraft mc, int chapterNumber) {
		this(mc, chapterNumber);
		context = c;
	}
	
	public static String getCurrentChapterSubtitleShort()
	{
		if(!BigxClientContext.getIsGameSaveRead())
		{
			return null;
		}
		
		GuiChapter.setChapter(chapterNumber);
		
		return subtitleLineShort;
	}
	
	public static void setChapter(int chapterNumber)
	{
		GuiChapter.chapterNumber = chapterNumber;
		
		GuiChapter.setTitleAndSubTitles(chapterNumber);
	}
	
	private static void setTitleAndSubTitles(int chapterNumber)
	{
		chapterText = "~ Chapter " + chapterNumber + " ~";
		
		switch(chapterNumber)
		{
		case 1:
			title = STR_CHAPTER_1_TITLE;
			subtitleLine1 = STR_CHAPTER_1_SUBTITLE_LINE_1;
			subtitleLine2 = STR_CHAPTER_1_SUBTITLE_LINE_2;
			subtitleLineShort = STR_CHAPTER_1_SUBTITLE_LINE_SHORT;
			break;
		case 2:
			chapterText = "~~";
			title = STR_CHAPTER_2_TITLE;
			subtitleLine1 = STR_CHAPTER_2_SUBTITLE_LINE_1;
			subtitleLine2 = STR_CHAPTER_2_SUBTITLE_LINE_2;
			subtitleLineShort = STR_CHAPTER_2_SUBTITLE_LINE_SHORT;
			break;
		case 3:
			chapterText = "~~";
			title = STR_CHAPTER_3_TITLE;
			subtitleLine1 = STR_CHAPTER_3_SUBTITLE_LINE_1;
			subtitleLine2 = STR_CHAPTER_3_SUBTITLE_LINE_2;
			subtitleLineShort = STR_CHAPTER_3_SUBTITLE_LINE_SHORT;
			break;
		default:
			break;
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		buttonList.clear();
		
		// Start Timer for close screen
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(mc.currentScreen instanceof GuiChapter)
				{
					didOneSecondPassed = true;
				}
			}
		}, 1*1000);
		
		// Start Timer for close screen
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(mc.currentScreen instanceof GuiChapter)
				{
					didOneSecondPassed = false;
					Minecraft.getMinecraft().thePlayer.closeScreen();
				}
			}
		}, 4*1000);
	}
	
	@Override
	public void updateScreen() 
	{
		super.updateScreen();
	};
	
	@Override
	public void drawScreen(int mx, int my, float partialTicks) {
	    ScaledResolution sr = new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
		int mcWidth = sr.getScaledWidth();
    	int mcHeight = sr.getScaledHeight();

		drawRect(0, 0, mcWidth, mcHeight, 0xCC000000); // The Box on top
		
		GL11.glPushMatrix();
		    GL11.glTranslatef(mcWidth/2, 0, 0);
	
        	fontRendererObj = Minecraft.getMinecraft().fontRenderer;
    		fontRendererObj.drawString(chapterText, -1 * fontRendererObj.getStringWidth(chapterText)/2, mcHeight/4 - 20, 0xFFFFFF);

        	fontRendererObj = Minecraft.getMinecraft().fontRenderer;
    		fontRendererObj.drawString(title, -1 * fontRendererObj.getStringWidth(title)/2, mcHeight/4 - 10, 0xFFFFFF);
    		
    		if(didOneSecondPassed)
    		{
    			fontRendererObj = Minecraft.getMinecraft().fontRenderer;
        		fontRendererObj.drawString(subtitleLine1, -1 * fontRendererObj.getStringWidth(subtitleLine1)/2, mcHeight/4 + 5, 0xEEEEEE);

            	fontRendererObj = Minecraft.getMinecraft().fontRenderer;
        		fontRendererObj.drawString(subtitleLine2, -1 * fontRendererObj.getStringWidth(subtitleLine2)/2, mcHeight/4 + 14, 0xEEEEEE);
    		}
		GL11.glPopMatrix();
		
		super.drawScreen(mx, my, partialTicks);
	}
	
	public int getTopMargin() {
		return 30;
	}
}
