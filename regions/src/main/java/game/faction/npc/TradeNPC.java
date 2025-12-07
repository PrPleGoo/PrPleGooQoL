package game.faction.npc;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import game.faction.trade.ITYPE;
import game.faction.trade.TradeManager;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import world.region.RD;

public class TradeNPC implements FACTION_IMPORTER, FACTION_EXPORTER{

	private final FactionNPC s;
	private double bestBuyValue = 1;

	public TradeNPC(FactionNPC s) {
		this.s = s;
		if (false) {
			//the trade caps need examining. PRobably importing should be the inverse of exporting.
		}
	}

	public int amount(RESOURCE res) {
		return s.stockpile.amount(res);
	}

	final SAVABLE saver = new SAVABLE() {

		@Override
		public void save(FilePutter file) {
			file.d(bestBuyValue);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			bestBuyValue = file.d();
		}

		@Override
		public void clear() {
			bestBuyValue = 1;
		}

	};

	@Override
	public int priceSell(RESOURCE res, int amount) {
		//double d = s.stockpile.prodRate(res)/s.stockpile.rate(res);
		double p = s.stockpile.priceSell(res.bIndex(), amount);
		p += 1;
		return (int) CLAMP.d(p, 0, Integer.MAX_VALUE);
	}

	@Override
	public int buyPrice(RESOURCE res, int amount) {
		return (int) Math.floor(s.stockpile.priceBuy(res.bIndex(), amount))-1;
	}

	@Override
	public void sell(RESOURCE res, int amount, int price, Faction buyer) {
		s.credits().inc(price, CTYPE.TRADE, res, amount);
		remove(res, amount, ITYPE.trade);

		if (buyer == FACTIONS.player()) {
			s.stockpile.incPlayer(res, amount);
			ROPINIONS.trade(s, price);
		}

		if (KingLevels.isActive()) {
			KingLevels.getInstance().soldGoodsTracker.processTraded(s, res.index(), amount);
		}
	}

	@Override
	public void remove(RESOURCE res, int amount, ITYPE type) {
		s.stockpile.inc(res, -amount);
		s.res().inc(res, type.rtype, -amount);
	}

	@Override
	public int forSale(RESOURCE res) {
		return (int)(s.stockpile.amount(res.bIndex()));
	}

	@Override
	public void buy(RESOURCE res, int amount, int price, Faction seller) {
		s.credits().inc(-price, CTYPE.TRADE, res, amount);
		reserveSpace(res, amount, ITYPE.trade);
		if (seller == FACTIONS.player()) {
			s.stockpile.incPlayer(res, amount);
			ROPINIONS.trade(s, price);
		}

	}

//	@Override
//	public double buyValue(RESOURCE res, int amount, double price) {
//		if (Integer.MAX_VALUE - s.stockpile.amount(res.index()) < amount)
//			return 0;
//		return s.stockpile.priceBuy(res.bIndex(), amount)/price;
//	}

	@Override
	public double buyPriority(RESOURCE res, int amount, double price) {
		if (Integer.MAX_VALUE - s.stockpile.amount(res.index()) < amount)
			return 0;
		if (KingLevels.isActive() && s.credits().getD() < 200000) {
			return 0;
		}

		return s.stockpile.priceBuy(res.bIndex(), amount)/price - 1.0;
	}



//	@Override
//	public void setBestBuyValue(double value) {
//		this.bestBuyValue = 1.0/value;
//	}


	@Override
	public int spaceForTribute(RESOURCE res) {
		return Integer.MAX_VALUE;
	}

	public int credits() {
		return (int) s.stockpile.credit();
	}

	@Override
	public void reserveSpace(RESOURCE res, int am, ITYPE type) {
		if (am < 0)
			return;

		if (type != ITYPE.tax) {
			s.stockpile.inc(res, am);
			s.res().inc(res, type.rtype, am);
		}
	}

	@Override
	public void deliver(RESOURCE res, int am, ITYPE type) {

	}

	public int priceSellP(RESOURCE res) {
		int am = Math.min(forSale(res)+1, 32);
		int p = priceSell(res, am)/am;
		if (p < 0)
			return 0;
		p += TradeManager.totalFee(s, FACTIONS.player(), RD.DIST().distance(s), res, 1);
		return (int) Math.ceil((double)p);
	}

	public int priceBuyP(RESOURCE res) {
		int p = buyPrice(res, 32)/32;

		p -= TradeManager.totalFee(FACTIONS.player(), s, RD.DIST().distance(s), res, 1);
		return p;
	}



//	public double credits2() {
//		return stockpile.credits2();
//	}




}
