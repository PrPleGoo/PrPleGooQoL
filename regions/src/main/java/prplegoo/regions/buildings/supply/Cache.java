package prplegoo.regions.buildings.supply;

import game.faction.FACTIONS;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;

import java.io.IOException;
import java.util.Arrays;

final class Cache implements SAVABLE{

	private double[] lockedUntil = new double[RESOURCES.ALL().size()];

	private int debugAmount = 0;
	
	Cache(ROOM_SUPPLY b) {
		
	}
	
	public int needed(RESOURCE res) {
		int am = 0; 
		for (ADSupply a : AD.supplies().get(res))
			am += a.needed(FACTIONS.player());
		return am + debugAmount;
	}
	
	public int deliverableSecret(RESOURCE res) {
		
		int needed = 0;
		for (ADSupply s : AD.supplies().get(res)) {
			for (WArmy e : FACTIONS.player().armies().all()) {
				needed += s.needed(e);
			}
		}
		needed += debugAmount;
		return needed;
	}
	
	public int deliverable(RESOURCE res) {
		if (lockedUntil[res.index()] > TIME.currentSecond())
			return 0;
		int needed = deliverableSecret(res);
		if (needed <= 0) {
			lockedUntil[res.index()] = TIME.currentSecond()+TIME.secondsPerDay*0.25;
			return 0;
		}
		return needed;
	}
	
	public int deliver(RESOURCE res, int am) {
		
		if(debugAmount != 0)
			return am;
		
		double needed = deliverable(res);
		if (needed <= 0) {
			return 0;
		}
		int delivered = 0;
		for (ADSupply s : AD.supplies().get(res)) {
			for (WArmy e : FACTIONS.player().armies().all()) {
				double n = s.needed(e);
				n/= needed;
				int a = (int) Math.ceil(am*n);
				if (a > am)
					a = am;
				delivered += a;
				s.current().inc(e, a);
			}
		}
		return delivered;
	}

	@Override
	public void save(FilePutter file) {
		file.dsE(lockedUntil);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.dsE(lockedUntil);
		
	}

	@Override
	public void clear() {
		Arrays.fill(lockedUntil, 0);
	}

}
