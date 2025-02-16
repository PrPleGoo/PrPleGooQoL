package game.faction.trade;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.TradeShipper.Partner;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.KingLevels;
import settlement.main.SETT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Tree;

final class TradeSorter {

	private final ResTree[] resTrees = new ResTree[RESOURCES.ALL().size()];
	private final Tree<ResTree> tree = new Tree<TradeSorter.ResTree>(RESOURCES.ALL().size()) {

		@Override
		protected boolean isGreaterThan(ResTree current, ResTree cmp) { return current.value > cmp.value; }

	};
	private Holder[] holders = new Holder[FACTIONS.MAX*RESOURCES.ALL().size()];

	public TradeSorter() {
		for (int i = 0; i < resTrees.length; i++)
			resTrees[i] = new ResTree(RESOURCES.ALL().get(i));
		for (int i = 0; i < holders.length; i++)
			holders[i] = new Holder();
	}

	public void sellPlayer(TradeShipper shipper) {

		Faction player = FACTIONS.player();
		tree.clear();
		int hI = 0;
		for (RESOURCE r : RESOURCES.ALL()) {

			ResTree t = resTrees[r.index()];
			t.traders.clear();


			if (player.seller().forSale(r) <= 0)
				continue;



			for (int i = 0; i < shipper.partners(); i++) {

				Partner buyer = shipper.partner(i);

				int am = CLAMP.i(player.seller().forSale(r), 0, TradeManager.MIN_LOAD);
				if (am <= 0)
					continue;

				int price = buyer.faction().buyer().buyPrice(r, am);
				double toll = TradeManager.totalFee(player, buyer.faction(), buyer.distance(), r, am);

				price -= toll;


				if (price <= 0)
					continue;

				if (!SETT.ROOMS().EXPORT.tally.okPrice(r,  (int) Math.ceil(price/am)))
					continue;

				Holder h = holders[hI++];

				h.p = buyer;
				h.value = price/am;
				h.price = price;
				t.traders.add(h);

			}

			if (resTrees[r.index()].traders.size() > 0) {
				t.value = SETT.ROOMS().EXPORT.tally.prio(r);
				tree.add(t);
			}
		}

		while(tree.hasMore()) {
			ResTree t = tree.pollSmallest();
			Holder h = t.traders.pollGreatest();

			int am = CLAMP.i(player.seller().forSale(t.res), 0, TradeManager.MIN_LOAD);

			if (am <= 0)
				continue;



			h.p.trade(t.res, am);

			h.p.faction().buyer().buy(t.res, am, h.price, player);
			player.seller().sell(t.res, am, h.price, h.p.faction());
			//LOG.ln(h.res.name + " " + MIN_LOAD + " " + (h.price-h.toll) + " " + h.f.appearence().name());

			//LOG.ln("sold " + t.res.name + " to " + h.p.faction().name + " for: " + h.price);

			am = CLAMP.i(player.seller().forSale(t.res), 0, TradeManager.MIN_LOAD);

			if (am > 0) {
				int price = h.p.faction().buyer().buyPrice(t.res, am);
				double toll = TradeManager.totalFee(player, h.p.faction(), h.p.distance(), t.res, am);
				price -= toll;

				if (price > 0 && SETT.ROOMS().EXPORT.tally.okPrice(t.res, (int) Math.ceil(price/am))) {
					h.value = price/am;
					h.price = price;
					t.traders.add(h);
				}
			}



			if (t.traders.size() > 0) {
				t.value = SETT.ROOMS().EXPORT.tally.prio(t.res);
				tree.add(t);
			}
		}

	}


	void buy(Faction buyer, TradeShipper shipper) {

		tree.clear();
		int hI = 0;


		for (RESOURCE r : RESOURCES.ALL()) {

			ResTree resSort = resTrees[r.index()];
			resSort.traders.clear();

			if (buyer != GAME.player() && buyer.buyer().buyPrice(r, 1) <= 0)
				continue;




			for (int i = 0; i < shipper.partners(); i++) {

				Partner seller = shipper.partner(i);

				if (seller.faction() == buyer)
					continue;

				if (seller.faction() == FACTIONS.player())
					continue;



				int am = TradeManager.MIN_LOAD;

				if (seller.faction().seller().forSale(r) <= am)
					continue;

				int sellPrice = seller.faction().seller().priceSell(r, am);
				int toll = TradeManager.totalFee(seller.faction(), buyer, seller.distance(), r, am);

				int price = sellPrice+toll;

				double v = buyer.buyer().buyPriority(r, am, price);



				//LOG.ln(price + " " + toll + " " + buyer.buyer().buyPrice(r, TradeManager.MIN_LOAD) + " " + v);
				if (v > 0) {
//
//					if (buyer == FACTIONS.player()) {
//						LOG.ln("adding: " + r + " from " + " " + seller.faction().name + " at $" + (price/TradeManager.MIN_LOAD) + " " + toll/32.0 + " " + seller.faction().seller().priceSell(r, 32)/32 + " " + seller.faction().seller().priceSell(r, 1) + " " + TradeManager.toll(seller.faction(), buyer, seller.distance(), seller.faction().seller().priceSell(r, 1)));
//					}

					Holder h = holders[hI++];
					h.p = seller;
					h.value = -price;
					h.price = price;
					resSort.traders.add(h);


				}

			}

			if (resSort.traders.hasMore()) {

				double v = buyer.buyer().buyPriority(r, TradeManager.MIN_LOAD, resSort.traders.greatest().price);
				resSort.value = v;
				tree.add(resSort);
			}

		}

		while(tree.hasMore()) {

			ResTree t = tree.pollGreatest();

			Holder h = t.traders.pollGreatest();
			int sellPrice = h.price;



			int am = TradeManager.MIN_LOAD;

			double v = buyer.buyer().buyPriority(t.res, am, sellPrice);

			if (v > 0 && am > 0) {

				h.p.trade(t.res, am);

				buyer.buyer().buy(t.res, am,  sellPrice, h.p.faction());

				if (buyer == FACTIONS.player()) {
					h.p.faction().seller().sell(t.res, am,  sellPrice-TradeManager.totalFee(h.p.faction(), buyer, h.p.distance(), t.res, am), h.p.faction());
				}else {
					h.p.faction().seller().sell(t.res, am, sellPrice, buyer);
				}





//				if (buyer == FACTIONS.player()) {
//					LOG.ln(v +  " buying: " + t.res + " from " + " " + h.p.faction().name + " at $" + (sellPrice/TradeManager.MIN_LOAD));
//				}

				//LOG.ln(buyer.name + " " + " <- " + h.p.faction().name + " " + t.res + " " + sellPrice + " " + buyer.seller().priceSell(t.res, TradeManager.MIN_LOAD));

				am = TradeManager.MIN_LOAD;

				if (am > 0 && h.p.faction().seller().forSale(t.res) > am) {

					int sp = h.p.faction().seller().priceSell(t.res, am);
					h.price = sp + TradeManager.totalFee(h.p.faction(), buyer, h.p.distance(), t.res, am);
					h.value = -h.price;
					t.traders.add(h);
				}


			}else {
				t.traders.clear();
			}

			if (t.traders.size() > 0) {
				h = t.traders.greatest();
				t.value = buyer.buyer().buyPriority(t.res, TradeManager.MIN_LOAD, h.price);
				if (t.value > 0)
					tree.add(t);
			}

		}



	}

	private static class Holder {
		Partner p;
		int price;
		private double value;
	}

	private static class ResTree {

		double value;
		final RESOURCE res;

		ResTree(RESOURCE res){
			this.res = res;
		}

		final Tree<Holder> traders = new Tree<TradeSorter.Holder>(FACTIONS.MAX) {

			@Override
			protected boolean isGreaterThan(Holder current, Holder cmp) {
				return current.value > cmp.value;
			}

		};

	}


}
