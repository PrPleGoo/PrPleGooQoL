package prplegoo.regions.buildings.supply;

import game.GAME;
import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import settlement.room.infra.logistics.MoveDic;
import settlement.room.infra.logistics.MoveOrderPullUI;
import settlement.room.military.supply.SupplyTally.TallyData;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.Dic;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import world.army.AD;
import world.army.ADSupply;

import java.util.Arrays;

import static settlement.room.infra.logistics.MoveDic.*;

class Gui extends UIRoomModuleImp<SupplyInstance, ROOM_SUPPLY> {

	private static CharSequence ¤¤away = "¤Shipments Underway";
	private static CharSequence ¤¤ready = "¤Wagons ready";
	
	private static CharSequence ¤¤liveStockD = "¤A {0} is used up for every 10th trip.";

	private static CharSequence ¤¤consumed = "¤Daily Consumption";
	private static CharSequence ¤¤needed = "¤Needed Delivery";
	private static CharSequence ¤¤underway = "¤Deliveries Underway";
	private static CharSequence ¤¤possible = "¤Possible";
	private static CharSequence ¤¤possibleD = "¤Some armies can not currently be supplied. To be supplied they need to be fortified and in own or allied territory.";
	private static CharSequence ¤¤closedD = "¤Your capitol is closed and deliveries can not be undertaken.";
	private static CharSequence ¤¤deliver = "¤There are no armies that needs the selected resources delivered.";
	static {
		D.ts(Gui.class);
	}
	
	private final Crate crate;
	
	Gui(ROOM_SUPPLY s) {
		super(s);
		crate = new Crate(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<SupplyInstance> g, int x1, int y1) {

		
		{
			GuiSection s = new GuiSection();
			
			{
				
				GButt.ButtPanel p = new GButt.ButtPanel(UI.icons().m.wheel) {
					
					@Override
					protected void renAction() {
						selectedSet(g.get().fetching());
					}
					
					@Override
					protected void clickA() {
						g.get().fetchingToggle();
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						
						super.render(r, ds, isActive, isSelected, isHovered);
						if (g.get().fetching() && g.get().coolFetch > 0) {
							GCOLOR.UI().SOSO.hovered.bind();
							UI.icons().s.alert.render(r, body.x1()+6, body.y1()+6);
							COLOR.unbind();
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(¤¤fetch);
						b.text(¤¤fetchD);
						b.NL();
						if (g.get().fetching() && g.get().coolFetch > 0) {
							b.add(b.text().warnify().add(MoveDic.¤¤fetchProblem));
						}
						super.hoverInfoGet(text);
					}
					
				};
				p.hoverTitleSet(¤¤fetch);
				p.hoverInfoSet(¤¤fetchD);
				p.body.setDim(48);
				s.addRightC(0, p);
				
				p = new GButt.ButtPanel(UI.icons().m.priority) {
					
					@Override
					protected void renAction() {
						selectedSet(g.get().prioritizing());
					}
					
					@Override
					protected void clickA() {
						g.get().prioritizeToggle();
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						
						super.render(r, ds, isActive, isSelected, isHovered);
						if (g.get().prioritizing() && g.get().coolFetch > 0) {
							GCOLOR.UI().SOSO.hovered.bind();
							UI.icons().s.alert.render(r, body.x1()+6, body.y1()+6);
							COLOR.unbind();
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(MoveDic.¤¤prio);
						b.text(MoveDic.¤¤prioD);
						b.NL();
						if (g.get().prioritizing() && g.get().coolFetch > 0) {
							b.add(b.text().warnify().add(MoveDic.¤¤fetchProblem));
						}
						
					}
					
				};
				
				p.body.setDim(48);
				s.addRightC(0, p);
			
			}
			
			MoveOrderPullUI ui = new MoveOrderPullUI(g, g, null, SupplyInstance.ORDERS);
			s.addRightC(8, ui);

			
			section.addRelBody(4, DIR.S, s);
		}
		{
			GuiSection s = new GuiSection();
			
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					cache(g.get());
					GFORMAT.iofkInv(text, livestock, carts);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(blueprint.liveStock.name);
					GText t = b.text();
					t.add(¤¤liveStockD);
					t.insert(0, blueprint.liveStock.name);
					b.add(t);
				};
			}.hh(blueprint.liveStock.icon()));
			
			s.addRightC(80, new GStat() {
				
				@Override
				public void update(GText text) {
					cache(g.get());
					GFORMAT.iofk(text, away, carts);
				}
			}.hh(UI.icons().m.arrow_right).hoverTitleSet(¤¤away));
			
			section.addRelBody(4, DIR.S, s);
			
		}
		
		GuiSection s = new GuiSection();

		int k =0;
		
		GText text = new GText(UI.FONT().S, 16);
		for (RESOURCE res : AD.supplies().resses()) {
			
			ClickableAbs c = new ClickableAbs(64, 64) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
					
					res.icon().renderC(r, body.cX(), body.cY()-8);
					text.clear();
					GFORMAT.i(text, blueprint.tally.amount.get(g.get(), res));
					text.adjustWidth();
					text.renderC(r, body.cX(), body.cY()+12);
					GButt.ButtPanel.renderFrame(r, body);
					
				}
				
				@Override
				protected void renAction() {
					selectedSet(g.get().allowed().has(res));
				}
				@Override
				protected void clickA() {
					
					g.get().allowedToggle(res);
					g.get().reset();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(res.name);

					

					b.textLL(¤¤underway);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), blueprint.tally.spaceReserved.get(g.get(), res) + blueprint.tally.amount.get(g.get(), res)));
					b.NL();
					
					b.textLL(¤¤ready);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), ready(res, g.get())));
					b.sep();
					
					hoverNeeded(b, res);

					if (S.get().developer) {
						b.add(UI.icons().s.storage);
						b.tab(7);
						b.add(GFORMAT.i(b.text(), blueprint.tally.capacity(g.get(), res, g.get().allowed()) ));
						b.NL();
						
					
						b.add(UI.icons().s.allRight);
						b.tab(7);
						b.add(GFORMAT.i(b.text(), blueprint.cache.deliverable(res)));
						b.NL();

						b.NL(8);

						b.textL(Dic.¤¤Needed);
						b.add(GFORMAT.i(b.text(), blueprint.cache.needed(res)));
						b.NL();
						b.add(UI.icons().s.allRight);
						b.add(GFORMAT.bool(b.text(), g.get().moveOrderPullAvailable().has(res)));
						b.NL();
						
						b.NL(8);
						
						for (TallyData d : blueprint.tally.datas) {
							b.textLL(d.name);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), d.get(g.get(), res)));
							b.tab(10);
							b.add(GFORMAT.i(b.text(), d.total(res)));
							b.NL();
							
						}
						
					}
					
					

					
				}
				
			};

			
			s.addGrid(c, k++, 5, 0, 0);

		}
		
		section.addRelBody(8, DIR.S, s);
		
	}
	
	@Override
	protected void hover(GBox b, SupplyInstance i) {
		super.hover(b, i);
		b.sep();
		if (i.fetch) {
			b.textL(¤¤fetching);
			b.NL();
		}

//		for (RESOURCE res : AD.supplies().resses()) {
//			if (i.allowed.has(res)) {
//				b.add(res.icon());
//				b.NL();
//				for (TallyData d : blueprint.tally.datas) {
//					b.textLL(d.name);
//					b.tab(7);
//					b.add(GFORMAT.i(b.text(), d.get(i, res)));
//					b.NL();
//					
//				}
//			}
//		}
	}
	
	
	
	private void hoverNeeded(GBox b, RESOURCE res) {
		b.textLL(Dic.¤¤Armies);
		b.NL();
		
		int stored = 0;
		int max = 0;
		int minimum = 0;
		double consumed = 0;
		for (ADSupply s : AD.supplies().get(res)) {
			max += s.targetAmount(FACTIONS.player());
			stored += s.current().faction(FACTIONS.player());
			minimum += s.minimumAmount(FACTIONS.player());
			consumed += -s.consumedPerDayCurrent(FACTIONS.player());
		}
		
		b.textL(Dic.¤¤Stored);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), stored));
		b.NL();
		
		b.textL(Dic.¤¤Minimum);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), minimum));
		b.NL();
		
		b.textL(Dic.¤¤Max);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), max));
		b.NL();
		
		b.textL(¤¤consumed);
		b.tab(6);
		b.add(GFORMAT.f0(b.text(), consumed));
		b.NL();
		
		b.textL(SETT.ROOMS().STOCKPILE.info.names);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)));
		b.NL();
		
		b.NL(4);
		
		b.textLL(¤¤needed);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), blueprint.cache.needed(res)));
		b.NL();
		
		int dd = blueprint.cache.deliverableSecret(res);
		b.textLL(¤¤possible);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), dd));
		b.NL();
		
		if (dd < blueprint.cache.needed(res)){
			b.error(¤¤possibleD);
			b.NL();
		}
		
		if (SETT.ENTRY().isClosed()) {
			b.error(¤¤closedD);
			b.NL();
		}
		
		b.sep();
	}

	@Override
	protected void appendMain(GGrid icons, GGrid gridtext, GuiSection sExtra) {
		
		GuiSection s = new GuiSection();

		int k =0;
		
		for (RESOURCE res : AD.supplies().resses()) {
			
			ClickableAbs c = new ClickableAbs(64, 40) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
					
					double stored = 0;
					double max = 0;
					
					for (ADSupply s : AD.supplies().get(res)) {
						max += s.targetAmount(FACTIONS.player());
						stored += s.current().faction(FACTIONS.player());
					}
					
					double d = stored/max;
					stored += blueprint.tally.amount.total(res)+blueprint.tally.spaceReserved.total(res);
					double d2 = stored/max;
					if (max > 0)
						GMeter.renderDelta(r, d, d2, body.x1()+5, body.x2()-5, body.y1()+5, body.y2()-5, false, false);
					
					res.icon().renderC(r, body.cX(), body.cY());
					GButt.ButtPanel.renderFrame(r, body);
					
				}
				
				private boolean sel() {
					for (int i = 0; i < blueprint.instancesSize(); i++) {
						if (blueprint.getInstance(i).allowed().has(res))
							return true;
					}
					return false;
				}
				
				@Override
				protected void renAction() {
					selectedSet(sel());
				}
				@Override
				protected void clickA() {
					boolean sel = sel();
					for (int i = 0; i < blueprint.instancesSize(); i++) {
						SupplyInstance ins = blueprint.getInstance(i);
						if (sel == ins.allowed().has(res)) {
							ins.allowedToggle(res);
							ins.reset();
						}	
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(res.name);

					hoverNeeded(b, res);

					b.textLL(¤¤underway);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), blueprint.tally.amount.total(res)+blueprint.tally.spaceReserved.total(res)));
					b.NL();
					
					if (S.get().developer) {
						for (TallyData d : blueprint.tally.datas) {
							b.textLL(d.name);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), d.total(res)));
							b.NL();
							
						}
						b.add(UI.icons().s.question);
						b.add(GFORMAT.i(b.text(), blueprint.cache.deliverable(res)));
					}
						
					

					
				}
				
			};

			
			s.addGrid(c, k++, 5, 0, 0);

		}
		
		gridtext.add(s);
	}
	
	@Override
	protected void problem(SupplyInstance i, Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings) {
		if (SETT.ENTRY().isClosed()) {
			errors.add(¤¤closedD);
		}
		
		boolean deliver = false;
		for (RESOURCE r : RESOURCES.ALL()) {
			if (!i.getWork().resourceReachable(r)) {
				if (i.blueprintI().tally.fetchAmount(r) <= 0)
					deliver = true;
					
			}
		}
		
		if (deliver) {
			warnings.add(¤¤deliver);
		}
	}
	
	private int[] ready = new int[RESOURCES.ALL().size()];
	private int livestock;
	private int carts;
	private int away;
	private int upI = -1;
	
	private void cache(SupplyInstance i) {
		if (upI == GAME.updateI())
			return;
		upI = GAME.updateI();
		Arrays.fill(ready, 0);
		livestock = 0;
		carts = 0;
		away = 0;
		for (int ji = 0; ji < i.jobs.size(); ji++) {
			COORDINATE j = i.jobs.get(ji);
			Crate cr = crate.get(j.x(), j.y());
			carts++;
			if (cr.animalHas())
				livestock ++;
			RESOURCE res = crate.get(j.x(), j.y()).realResource();
			if (res != null) {
				ready[res.index()] += cr.resAmount()*(cr.goIsReady() == 0 ? 1 : 0);
				
			}
			away += (cr.away() || cr.awayReserved()) ? 1 : 0;;
		}
	}
	
	private int ready(RESOURCE res, SupplyInstance i) {
		cache(i);
		return ready[res.index()];
	}
	
	

	
	
	

}
