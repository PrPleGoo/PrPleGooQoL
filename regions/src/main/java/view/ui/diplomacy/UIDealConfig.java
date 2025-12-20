package view.ui.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.deal.Deal;
import game.faction.diplomacy.deal.DealBool;
import game.faction.diplomacy.deal.DealParty;
import init.race.Race;
import init.resources.RESOURCE;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.common.UIPickerRaceAm;
import util.gui.common.UIPickerRegion;
import util.gui.common.UIPickerResAm;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;

import java.util.LinkedList;

public final class UIDealConfig extends GuiSection{
	private static boolean canTrade(DealParty party) {
		if (party.f() == FACTIONS.player()) {
			return true;
		}

		return RD.DIST().reachable(party.f()) || RD.DIST().factionCanAttackPlayerAllies(party.f());
	}
	
	private static CharSequence ¤¤offer = "¤Offer";
	private static CharSequence ¤¤demand = "¤Demand";
	private static CharSequence ¤¤item = "¤/Item";
	static {
		D.ts(UIDealConfig.class);
	}
	
	private static int BW = 345;
	private static int BH = 34;
	
	public UIDealConfig(Deal deal, int height){

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		
		for (DealBool b : deal.bools.all())
			rows.add(new Bool(b));
		
		rows.add(h(¤¤offer));
		rows.add(new Regionlist(deal, deal.player, deal.npc));
		rows.add(new Reslist(deal, deal.player));
		rows.add(new Slavelist(deal, deal.player));
		rows.add(new Sum(deal.player));

		rows.add(h(¤¤demand));
		rows.add(new Regionlist(deal, deal.npc, deal.npc));
		rows.add(new Reslist(deal, deal.npc));
		rows.add(new Slavelist(deal, deal.npc));
		rows.add(new Sum(deal.npc));
		
		add(new GScrollRows(rows, height).view());
		
	}
	
	private RENDEROBJ h(CharSequence t) {
		return new RenderImp(BW, BH) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.T().H1.bind();
				UI.FONT().H2.renderCX(r, body.cX(), body().y2()-UI.FONT().H2.height()-4, t);
			}
		};
	}


	private static class Bool extends GButt.ButtPanel {

		private final DealBool bool;
		
		public Bool(DealBool bool) {
			super(bool.info.name);
			this.bool = bool;
			icon(bool.icon);
			body().setDim(BW, BH);

		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isActive = bool.problem() == null;
			isSelected = bool.is();
			super.render(r, ds, isActive, isSelected, isHovered);
		}
		
		@Override
		protected void clickA() {
			if (bool.problem() == null)
				bool.toggle();
			super.clickA();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			bool.hover(b);
			
			CharSequence p = bool.problem();
			if (p != null)
				b.error(p);
		}
		
	}
	
	
	
	private static class Regionlist extends GButt.ButtPanel{
		
		private final GuiSection pop;
		private final DealParty p;
		
		Regionlist(Deal deal, DealParty p, DealParty ff){
			super(Dic.¤¤Regions);
			this.p = p;
			icon(UI.icons().s.world);
			body().setDim(BW, BH);
			GETTER<Faction> gg = new GETTER<Faction>() {

				@Override
				public Faction get() {
					return p.f();
				}
				
			};
			pop = new UIPickerRegion(gg, 400) {
				
				@Override
				protected void toggle(Region reg) {
					p.regs.select(reg, !p.regs.selected(reg));
				}
				
				@Override
				protected boolean active(Region reg) {
					return p.regs.selecteCan(reg);
				}
				
				@Override
				protected boolean selected(Region reg) {
					return p.regs.selected(reg);
				}
				
				@Override
				protected void hoverInfo(GBox b, Region reg) {
					
					b.add(UI.icons().s.money);
					b.add(GFORMAT.i(b.text(), (long) p.regs.value(reg)));
					b.NL(8);
					
					super.hoverInfo(b, reg);
				}
				
			};
		}
		
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(pop, this);
		}
		
		@Override
		protected void renAction() {
			activeSet(p.f().realm().regions() > 1 && canTrade(p));
		}
		
		
	}
	
	private static class Reslist extends GButt.ButtPanel{
		
		private final GuiSection pop;
		private final DealParty p;

		Reslist(Deal deal, DealParty p){
			super(Dic.¤¤Resource);
			this.p = p;
			icon(UI.icons().s.storage);
			body().setDim(BW, BH);
			pop = new UIPickerResAm(p.resources, 16) {
				@Override
				protected void addToRow(GuiSection row, GETTER<RESOURCE> g) {
					
					row.addRelBody(8, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							text.add('/');
							GFORMAT.i(text, p.resources.max(g.get()));
						}
					});
					row.body().incrW(64);
					row.addRelBody(8, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, p.valueResource(g.get(), p.resources.get(g.get())));
						}
					}.hh(UI.icons().s.money));
					row.addRelBody(128, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							int am = p.resources.get(g.get());
							if (am == 0)
								am = 1;
							GFORMAT.i(text, p.valueResource(g.get(), am)/am);
							text.s();
							text.add(¤¤item);
						}
					}.r(DIR.E));
					
					
				}
			};
		}
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(pop, this);
		}
		
		@Override
		protected void renAction() {
			activeSet(canTrade(p));
		}
		
		
	}
	
	private static class Slavelist extends GButt.ButtPanel{
		
		private final GuiSection pop;
		private final DealParty p;

		Slavelist(Deal deal, DealParty p){
			super(HCLASSES.SLAVE().names);
			this.p = p;
			icon(UI.icons().s.slave);
			body().setDim(BW, BH);
			
			pop = new UIPickerRaceAm(p.slaves, 16) {
				@Override
				protected void addToRow(GuiSection row, GETTER<Race> g) {
					
					row.addRelBody(8, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							text.add('/');
							GFORMAT.i(text, p.slaves.max(g.get()));
						}
					});
					row.body().incrW(64);
					row.addRelBody(8, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, p.valueSlave(g.get(), p.slaves.get(g.get())));
						}
					}.hh(UI.icons().s.money));
					row.body().incrW(64);
				}
			};
		}
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(pop, this);
		}
		
		@Override
		protected void renAction() {
			activeSet(canTrade(p));
		}
		
		
	}
	
	private class Sum extends GInputInt{
		private final DealParty p;
		Sum(DealParty party){
			super(party.credits, true, true);
			this.p = party;
			body().setHeight(BH);
			addRelBody(4, DIR.W,UI.icons().s.money);
			addRelBody(8, DIR.E, new GStat() {
				
				@Override
				public void update(GText text) {
					text.add('(');
					GFORMAT.i(text, party.credits.max());
					text.add(')');
				}
			});
			body().setWidth(BW);

		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			activeSet(canTrade(p));
			super.render(r, ds);
		}
	}
	
}
