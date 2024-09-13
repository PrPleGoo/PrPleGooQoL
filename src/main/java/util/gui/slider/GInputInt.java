package util.gui.slider;

import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GInput;

public class GInputInt extends GuiSection{

	private final INTE in;


	private final StringInputSprite sp = new StringInputSprite(10, UI.FONT().S) {

		@Override
		protected void change() {
			int num = 0;
			int sign = 1;
			for (int i = 0; i < text().length(); i++) {
				if (i == 0 && text().charAt(i) == '-') {
					sign = -1;
					continue;
				}

				int n = text().charAt(i)- '0';

				if (n >= 0 && n < 10) {
					if (num*10 + n > in.max())
						break;
					num*= 10;
					num += n;
				}else {
					unfuck();
					return;
				}
			}
			if (num == 0 && sign == -1 && in.min() < 0) {
				in.set(0);
				text().clear().add('-');
			}else {
				in.set(CLAMP.i(num*sign, in.min(), in.max()));
				text().clear().add(in.get());
			}

		};


	}.placeHolder("0")
	 .setNumeric(true);
	
	public GInputInt(INTE in) {
		this(in, false, false);
	}
	
	public GInputInt(INTE in, boolean butts, boolean doublebutts){
		this.in = in;
		
		GInput inn = new GInput(sp);
		
		if (doublebutts) {
			GButt.ButtPanel pp = new GButt.ButtPanel(UI.icons().s.minifierBig) {
				
				@Override
				protected void clickA() {
					in.inc(-Math.max(1, (in.max()-in.min())/5));
				}
				

				@Override
				protected void renAction() {
					activeSet(in.get() > in.min());
				};
				
			};
			pp.repetativeSet(true);
			pp.body().setHeight(inn.body.height());
			addRightC(0, pp);
			
		}
		
		if (butts) {
			GButt.ButtPanel pp = new GButt.ButtPanel(UI.icons().s.minifier) {
				
				@Override
				protected void clickA() {
					if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
					{
						in.set(in.min());
					}
					else
					{
						in.inc(-1);
					}
				}
				

				@Override
				protected void renAction() {
					activeSet(in.get() > in.min());
				};
				
			};
			pp.repetativeSet(true);
			pp.body().setHeight(inn.body.height());
			addRightC(0, pp);
		}
		
		
		
		addRightC(0, inn);
		if (butts) {
			GButt.ButtPanel pp = new GButt.ButtPanel(UI.icons().s.magnifier) {
				
				@Override
				protected void clickA() {
					if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
					{
						in.set(in.max());
					}
					else
					{
						in.inc(1);
					}
				}
				
				@Override
				protected void renAction() {
					activeSet(in.get() < in.max());
				};
				
			};
			pp.repetativeSet(true);
			pp.body().setHeight(inn.body.height());
			addRightC(0, pp);
		}
		
		if (doublebutts) {
			GButt.ButtPanel pp = new GButt.ButtPanel(UI.icons().s.magnifierBig) {
				
				@Override
				protected void clickA() {
					in.inc(Math.max(1, (in.max()-in.min())/5));
				}
				

				@Override
				protected void renAction() {
					activeSet(in.get() < in.max());
				};
				
			};
			pp.repetativeSet(true);
			pp.body().setHeight(inn.body.height());
			addRightC(0, pp);
			
		}
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		unfuck();
		super.render(r, ds);
	}
	
	private void unfuck() {
		sp.text().clear();
		int am = in.get();
		if (am != 0)
			sp.text().add(am);
	}
	
}
