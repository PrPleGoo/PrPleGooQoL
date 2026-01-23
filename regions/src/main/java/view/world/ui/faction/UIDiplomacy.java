package view.world.ui.faction;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.diplomacy.deal.Deal;
import game.faction.diplomacy.deal.DealDrawfter;
import game.faction.diplomacy.deal.DealParty;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.OpsStance;
import game.faction.royalty.opinion.ROPINIONS;
import init.settings.S;
import init.sprite.UI.UI;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.diplomacy.UIDealConfig;
import view.ui.diplomacy.UIDealList;
import world.region.RD;

final class UIDiplomacy extends GuiSection{
	private static boolean canTrade(DealParty party) {
		if (party.f() == FACTIONS.player()) {
			return true;
		}

		return !KingLevels.isActive() || RD.DIST().reachable(party.f()) || RD.DIST().factionCanAttackPlayerAllies(party.f());
	}


	private static CharSequence ¤¤What = "What do you wish to offer us?";
	private static CharSequence ¤¤Barter = "Barter";
	private static CharSequence ¤¤BarterD = "Allow the Faction to compose a deal that they feel comfortable with based on your demands.";
	private static CharSequence ¤¤desc = "The value of a deal is weighed by the faction's perception of the value of its components. A deal needs to have a possible value in order to go through. A high positive value indicate generosity on your part, and will increase the faction's opinion of you.";
	private static CharSequence ¤¤Accept = "The deal will be accepted";
	private static CharSequence ¤¤AcceptNo = "The deal will not be accepted";
	private static CharSequence ¤¤OpinionD = "The change of opinion of the faction's ruler if this deal is accepted.";
	private static CharSequence ¤¤No = "You have nothing of worth to offer the faction.";

	static {
		D.ts(UIDiplomacy.class);
	}

	private final Deal deal;
	public final GuiSection section = new GuiSection();
	private double timer = 0;

	public UIDiplomacy(GETTER<FactionNPC> g, Deal deal, int height){

		this.deal = new Deal();

		addRelBody(0, DIR.S,  new GText(UI.FONT().M, ¤¤What).lablifySub());

		GuiSection op = new GuiSection();

		{
			op.addRightC(0, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, (long) (deal.valueCredits()));
				}
			}.hh(Dic.¤¤Value).hoverInfoSet(¤¤desc));

			op.addRightC(100, new GStat() {

				@Override
				public void update(GText text) {

					GFORMAT.f0(text, deal.opinionChange());
				}
			}.hh(ROPINIONS.¤¤name).hoverInfoSet(¤¤OpinionD));

			op.addRightC(100, new GStat() {

				@Override
				public void update(GText text) {

					GFORMAT.f0(text, -deal.betrayal());
				}

				@Override
				public void hoverInfoGet(GBox b) {
					deal.hoverBetrayal(b);
				};

			}.hh(OpsStance.¤¤betrayal));

			op.addRightC(100, new GButt.ButtPanel(Dic.¤¤Accept) {

				@Override
				protected void renAction() {
					activeSet(deal.canBeAccepted() || S.get().developer);
				}

				@Override
				protected void clickA() {
					if (deal.canBeAccepted() || S.get().developer)
						deal.execute(true);

					super.clickA();
				}

				@Override
				public void hoverInfoGet(GUI_BOX text) {

					GBox b = (GBox) text;
					b.text(¤¤desc);

					{
						b.NL(8);
						b.textLL(Dic.¤¤Value);
						b.tab(6);
						b.add(UI.icons().s.money);
						b.add(GFORMAT.i(b.text(), (long) (deal.valueCredits())));
					}

					{
						b.NL();
						b.textLL(ROPINIONS.¤¤name);
						b.tab(6);

						b.add(GFORMAT.f0(b.text(), deal.opinionChange()));
						GText t = b.text();
						t.add('(');
						GFORMAT.f(t, ROPINIONS.current(deal.npc.npc()));
						t.add(')');
						b.add(t);
					}


					b.NL(8);
					if (deal.canBeAccepted())
						b.textL(¤¤Accept);
					else
						b.error(¤¤AcceptNo);
				}


			}).hoverInfoSet(¤¤desc);

			op.addRightC(16, new GButt.ButtPanel(¤¤Barter) {


				private GTextR t = new GText(UI.FONT().M, ¤¤No).warnify().r(DIR.N);

				@Override
				protected void clickA() {
					DealDrawfter.draft(deal, true, true);
					if (deal.hasDeal() && !deal.canBeAccepted()) {
						timer = 5;
						VIEW.inters().popup.show(t, this);
					}
					super.clickA();
				}

				@Override
				protected void renAction() {
					activeSet(deal.hasDeal() && canTrade(deal.npc));
				}

			}).hoverInfoSet(¤¤BarterD);
		}

		int h = height-body().height()-op.body().height()-16;



		GuiSection s = new GuiSection();

		s.add(new UIDealConfig(deal, h));
		s.addRelBody(16, DIR.E, new SPRITE.Imp(1, h) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.UI().border().render(r, X1, X2, Y1, Y2);
			}
		});
		s.addRelBody(16, DIR.E, new UIDealList(deal, h));

		addRelBody(8, DIR.S, s);

		addRelBody(8, DIR.S, op);

	}

	@Override
	public void render(SPRITE_RENDERER r, float ds){

		GAME.SPEED.tmpPause();
		if (timer > 0) {
			timer -= ds;
			if (timer <= 0)
				VIEW.inters().popup.close();
		}
		super.render(r, ds);
	}


	public void openPeace(FactionNPC other) {
		GAME.SPEED.tmpPause();
		deal.setFactionAndClear(other, true);
		DealDrawfter.draftPeace(deal, other, true);
	}

}
