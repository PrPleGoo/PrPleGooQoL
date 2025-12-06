package view.interrupter;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.panel.GPanel;
import util.text.Dic;
import view.keyboard.KEYS;

public final class ITextInput extends Interrupter{

	private final GuiSection s = new GuiSection();
	private final Str title = new Str(64);

	private final GInput in;
	private STRING_RECIEVER client;
	private final InterManager m;
	private StringInputSprite input;

	public ITextInput(InterManager m){
		this.m = m;


		s.addDownC(0, new SPRITE.Imp(400, UI.FONT().H2.height()*2) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.T().H1.bind();
				UI.FONT().H2.renderIn(r, X1, Y1, DIR.C, title, width(), height(), 1);
				COLOR.unbind();
			}
		});

		input = new StringInputSprite(48, UI.FONT().M);
		in = new GInput(input);


		s.addRelBody(16, DIR.S, in);

		GuiSection buttons = new GuiSection();

		buttons.add(new GButt.ButtPanel(UI.icons().m.ok) {
			@Override
			protected void clickA() {
				hide();
				client.acceptString(input.text());
			}
		}.hoverTitleSet(Dic.¤¤OK));

		buttons.addRightC(0, new GButt.ButtPanel(UI.icons().m.cancel) {
			@Override
			protected void clickA() {
				hide();
				client.acceptString(null);
			}
		}.hoverTitleSet(Dic.¤¤cancel));

		s.addRelBody(16, DIR.S, buttons);

		s.pad(8, 8);

		GPanel p = new GPanel(s.body());
		p.setBig();


		s.add(p);
		s.moveLastToBack();

		s.body().centerIn(C.DIM());

	}

	@Override
	protected boolean render(Renderer r, float ds) {

		in.listen();
		s.render(r, ds);

		return true;
	}

	public void requestNumericInput(STRING_RECIEVER client, CharSequence title) {
		input.setNumeric(true);
		this.title.clear().add(title);
		this.client = client;
		in.text().clear();
		in.focus();
		super.show(m);
	}

	public void requestInput(STRING_RECIEVER client, CharSequence title){

		requestInput(client, title, null);

	}

	public void requestInput(STRING_RECIEVER client, CharSequence title, CharSequence placeholder) {
		input.setNumeric(false);
		this.title.clear().add(title);
		this.client = client;
		in.text().clear();
		if (placeholder != null)
			in.text().add(placeholder);
		in.focus();
		super.show(m);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		s.hover(mCoo);
		return true;

	}

	@Override
	protected void hoverTimer(GBox text) {
		s.hoverInfoGet(text);
	}
	@Override
	protected void mouseClick(MButt button) {
		s.click();
	}

	@Override
	protected boolean update(float ds) {

		if (KEYS.MAIN().ESCAPE.consumeClick() || MButt.RIGHT.isDown()){
			client.acceptString(null);
			hide();
		} else if (KEYS.MAIN().ENTER.consumeClick()
			|| KEYS.MAIN().KP_ENTER.consumeClick())
		{
			client.acceptString(in.text());
			hide();
		}
		KEYS.clear();

		return false;
	}

}
