package org.ngs.bigx.minecraft.entity.lotom;

import org.ngs.bigx.minecraft.entity.lotom.BackpackProperty.backpackReturnCode;

public class CharacterProperty {
	public static enum characterReturnCode {
		success, failure, bagIsFull, bagIsNotActive
	};
	
	private double oxygenLevel = 100;
	private double oxygenCosumptionRate = 0.1;	// Oxygen consumption rate per 50 milliseconds
	private double oxygenLevelMax = 100;		// Default amount of oxgygen a player can hold without any equipment
	private int weight = 60; 					// The unit is Kg
	private int totalWeight;
	private double speedRate = 10;
	private int strength = 10;					// Abstracted strength: Pedaling Resistance down
	private int skill = 10;						// Abstracted skill: Skill to craft items
	private int luck = 10;						// Abstracted luck: Luck to mine minerals
	private BackpackProperty[] backpacks = new BackpackProperty[2];	// Two backpacks per character at most
	
	public void initProperty()
	{
		this.oxygenLevel = 100;
		this.oxygenCosumptionRate = .1;
		this.oxygenLevelMax = 100;
		this.weight = 60;
		this.speedRate = 1.0;
		this.strength = 10;
		this.skill = 10;
		this.luck = 10;
		this.backpacks[0].activateBackpack();
		this.backpacks[1].deactivateBackpack();
		this.totalWeight = this.weight;
	}
	
	public void recoverOxygenLevel()
	{
		this.oxygenLevel = this.oxygenLevelMax;
	}
	
	public void decreaseOxygenByTick()
	{
		this.oxygenLevel -= this.oxygenCosumptionRate;
	}
	
	public int getMaxWeightCharCanHandle()
	{
		return this.strength * 6;
	}
	
	public int getRemaintWeightLeftToMax()
	{
		return this.getMaxWeightCharCanHandle() - this.totalWeight;
	}
	
	public characterReturnCode insertAnItemToBackpack(int backpackIndex, ItemLotom item)
	{
		if(!this.backpacks[backpackIndex].isActivated())
			return characterReturnCode.bagIsNotActive;
		
		backpackReturnCode backpackrc = this.backpacks[backpackIndex].insertAnItem(item);
		
		switch(backpackrc)
		{
		case success:
			return characterReturnCode.success;
		case bagIsFull:
			return characterReturnCode.bagIsFull;
		};

		return characterReturnCode.failure; 
	}
	
	public double getSpeedRate()
	{
		return this.speedRate;
	}
	
	public double getResistanceSettings()
	{
		return this.totalWeight - this.strength*6;
	}
}