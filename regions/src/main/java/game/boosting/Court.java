package view.world.ui.faction;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.emmi.EmiTypeRoy;
import game.faction.player.emmi.Emissaries;
import game.faction.royalty.NPCCourt;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import init.sprite.UI.UI;
import init.text.D;
import init.type.TRAIT;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.main.VIEW;

final class Court extends GuiSection{


	private static CharSequence ¤¤Heir = "¤First Heir";
	private static CharSequence ¤¤Heir2 = "¤Second Heir";
	private static CharSequence ¤¤Heir3 = "¤Third Heir";
	private static CharSequence ¤¤EDesc = "By clicking a royalty you can allocate emissaries to them to perform different actions.";

	private final CharSequence[] sss = new CharSequence[] {
			¤¤Heir,
			¤¤Heir2,
			¤¤Heir3
	};

	static {
		D.ts(Court.class);

	}

	private final UIRoyalty pop = new UIRoyalty();

	Court(GETTER<FactionNPC> f, int WIDTH, int HEIGHT){

		body().setDim(WIDTH, HEIGHT);

		GuiSection suckers = new GuiSection();

		suckers.add(king(f));
		suckers.add(butt(f, 1), 0, suckers.body().y2()+8);

		for (int i = 2; i < NPCCourt.MAX; i++) {
			suckers.addRightC(48, butt(f, i));
		}

		suckers.body().moveY1(0).moveX1(32);
		add(suckers);


		RENDEROBJ oo = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, get());
				text.s().add('|').s();
				GFORMAT.i(text, FACTIONS.player().emissaries.available());
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Emissaries.¤¤name);
				b.text(¤¤EDesc);
				b.NL();
				b.textLL(Dic.¤¤Allocated);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), get()));
				b.NL();
				b.textLL(Dic.¤¤Available);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), FACTIONS.player().emissaries.available()));
			};

			private int ui = 0;
			private int am = 0;

			private int get() {
				if (ui == GAME.updateI())
					return am;
				ui = GAME.updateI();
				am = 0;
				for (EmiTypeRoy e : FACTIONS.player().emissaries.roys) {
					am+= e.total(f.get());

				}
				return am;
			}
		}.hh(UI.icons().m.flag);

		add(oo, 260, body().y1()+200);

	}

	private CLICKABLE king(GETTER<FactionNPC> f) {
		GuiSection s = new GuiSection();

		s.add(new Portrait(4, new GETTER<Royalty>() {

			@Override
			public Royalty get() {
				return f.get().court().king().roy();
			}

		}));

		s.add(new GStat(new GText(UI.FONT().M, 32)) {

			@Override
			public void update(GText text) {
				f.get().court().king().intro(text);
			}
		}.r(DIR.NW), s.body().x2()+8, 16);


		s.addDown(8, new GStat(new GText(UI.FONT().H2, 32)) {

			@Override
			public void update(GText text) {
				text.lablify().add(f.get().court().king().name);

			}
		}.r(DIR.NW));

		s.addDown(8, new GStat(new GText(UI.FONT().M, 32)) {

			@Override
			public void update(GText text) {
				LIST<TRAIT> tt = f.get().court().king().roy().traits;
				for (int i = 0; i < tt.size(); i++) {
					text.add(tt.get(i).rTitle);
					if (i < tt.size()-1)
						text.add(',').s();
				}
				text.setMaxWidth(500);
				text.setMultipleLines(true);
			}
		}.r(DIR.NW));




		return s;
	}


	private CLICKABLE butt(GETTER<FactionNPC> f, int i) {

		return new Portrait(2, new GETTER<Royalty>() {

			@Override
			public Royalty get() {
				return f.get().court().all().get(i);
			}



		}) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(sss[i-1]);
				text.NL();
				super.hoverInfoGet(text);
			}
		};

	}


	static void hover(GUI_BOX box, Royalty ro) {
		GBox b = (GBox) box;

		b.title(ro.nameFull(b.text()));

		b.textLL(Dic.¤¤Age);
		b.tab(6);
		int y = (int) (STATS.POP().age.years.getD(ro.induvidual));
		b.add(GFORMAT.i(b.text(), y));
		b.NL(8);

		for (TRAIT info : ro.traits) {
			b.textL(info.info.name);
			b.NL();
			b.text(info.info.desc);
			b.NL(8);
		}

		ROPINIONS.hover(box, ro, true);


	}

	class Portrait extends ClickableAbs{



		private final SPRITE s;
		final GETTER<Royalty> g;
		final int scale;
		final int bar;
		public Portrait(int scale, GETTER<Royalty> g) {
			this.scale = scale;
			this.g = g;
			bar = (int) (8*Math.ceil(scale/2.0));
			s = new UIRoyalty.Portrait(scale, g);
			body.setDim(s.width()+4*scale, s.height()+6*scale);
		}



		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			Royalty ro = g.get();
			isActive &= ro != null;
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			if (ro == null)
				return;

			s.renderC(r, body.cX(), body.cY());

			double now = ROPINIONS.peaceValue(ro);
			now = 0.5+now/8.0;

			GMeter.render(r, GMeter.C_REDGREEN, now,  body.x1()+4*scale,  body().x2()-4*scale, body().y2()-6*scale, body().y2()-2*scale);

			GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (g.get() == null)
				return;
			Court.hover(text, g.get());
			text.NL(8);
			super.hoverInfoGet(text);
		}

		@Override
		protected void clickA() {
			Royalty ro = g.get();
			if (ro == null)
				return;
			VIEW.inters().popup.show(pop.get(ro), this, true);
		}

	}


}
