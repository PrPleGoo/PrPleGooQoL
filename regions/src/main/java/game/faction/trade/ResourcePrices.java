package game.faction.trade;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGEat;
import prplegoo.regions.api.npc.KingLevel;
import prplegoo.regions.api.npc.KingLevels;
import world.region.RD;

import java.util.Arrays;

public final class ResourcePrices {

	private int[] lastCheck = new int[RESOURCES.ALL().size()];
	{
		Arrays.fill(lastCheck, -16);
	}
	
	private double[] price = new double[RESOURCES.ALL().size()];
	
	private int echeck = -16;
	private int edible = NPCStockpile.AVERAGE_PRICE;
	private int edibleLow = NPCStockpile.AVERAGE_PRICE/8;
	
	public void clearCache() {
		Arrays.fill(lastCheck, -16);
		echeck = -16;
	}
	
	public int get(RESOURCE res) {
		if (FACTIONS.player() == null || FACTIONS.player().capitolRegion() == null) {
			return NPCStockpile.AVERAGE_PRICE;
		}
		int ri = res.index();
		if (Math.abs(lastCheck[res.index()] - GAME.updateI()) > 16) {
			lastCheck[res.index()] = GAME.updateI();
			double a = 0;
			price[ri] = 0;
			for (int fi = 0; fi < FACTIONS.NPCs().size(); fi++) {
				FactionNPC f = FACTIONS.NPCs().get(fi);
				double pop = RD.RACES().population.get(f.capitolRegion());
				if (f.stockpile.amount(ri) > 0) {
					price[ri] += f.stockpile.price(ri, 0)* (KingLevels.isActive() ? 1 : pop);
					a+=pop;
				} else {
					price[ri] += NPCStockpile.AVERAGE_PRICE * NPCStockpile.getPRICE_MAX();
				}
			}
			if (!KingLevels.isActive()) {
				if (a == 0)
					price[ri] = NPCStockpile.AVERAGE_PRICE;
				else {
					price[ri] /= a;
				}
			} else {
				price[ri] /= FACTIONS.NPCs().size();
			}
		}
		return (int) Math.ceil(price[res.index()]);
	}
	
	private void ee() {
		if (Math.abs(echeck - GAME.updateI()) > 16) {
			echeck = GAME.updateI();
			edible = 0;
			edibleLow = Integer.MAX_VALUE;
			for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
				ResGEat e = RESOURCES.EDI().all().get(ei);
				int p = get(e.resource);
				edibleLow = Math.min(p, edibleLow);
				edible += p;
			}
			edible /= RESOURCES.EDI().all().size();
		}
	}
	
	public int edible() {
		ee();
		return edible;
	}
	
	public int edibleLow() {
		ee();
		return edibleLow;
	}
	
}
