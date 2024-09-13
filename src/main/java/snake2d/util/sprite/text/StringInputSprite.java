package snake2d.util.sprite.text;

import snake2d.CORE;
import snake2d.Input.CHAR_LISTENER;
import snake2d.Mouse;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;

public class StringInputSprite extends CHAR_LISTENER implements SPRITE{

	private Font f;
	private static final String promt = "|";
	private CharSequence placeholder;
	public int marker = 0;
	private static final Str tmp = new Str(512);
	
	private int selectedI = -1;
	private boolean isNumeric = false;

	public StringInputSprite(int size, Font font) {
		super(size);
		this.f = font;
	}

	public StringInputSprite placeHolder(CharSequence ph) {
		this.placeholder = ph;
		return this;
	}

	public StringInputSprite setNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
		return this;
	}

	public StringInputSprite font(Font f) {
		this.f = f;
		return this;
	}
	
	public Font font() {
		return f;
	}
	
	@Override
	protected void acceptChar(char c) {
		if (!listening())
			return;
		if (isNumeric && !Character.isDigit(c)) {
			return;
		}
		if (selectedI >= 0 && selectedI != marker()) {
			int s = Math.min(selectedI, marker);
			int e = Math.max(selectedI, marker);
			tmp.clear().add(text());
			Str tt = text();
			tt.clear();
			for (int i = 0; i < tmp.length(); i++) {
				if (i == s) {
					tt.add(c);
				}else if (i < s || i > e)
					tt.add(tmp.charAt(i));
			}
			marker = s+1;
		}else if (text().spaceLeft() > 0) {
			if (marker() == text().length()) {
				text().add(c);
				
			}else {
				tmp.clear().add(text());
				text().clear();
				int k = 0;
				for (int i = 0; i < tmp.length(); i++) {
					if (k++ == marker) {
						text().add(c);
						i--;
					}else {
						text().add(tmp.charAt(i));
					}
				}
			}
			marker++;
			
		}
		change();
		
		selectedI = -1;
	}

	@Override
	protected void backspace() {
		
		if (!listening())
			return;
		if (text().length() == 0)
			return;
		int m = marker();
		if (removeSelected()) {
			;
		}else if (m > 0) {
			
			tmp.clear().add(text());
			Str tt = text();
			tt.clear();
			for (int i = 0; i < tmp.length(); i++) {
				if (i+1 ==m) {
					;
				}else {
					tt.add(tmp.charAt(i));
				}
			}
			marker--;
			
		}
		selectedI = -1;
		change();
	}
	
	@Override
	public void del() {
		if (!listening())
			return;
		if (text().length() == 0)
			return;
		int m = marker();
		if (removeSelected()) {
			;
		}else if (m > 0) {
			
			tmp.clear().add(text());
			Str tt = text();
			tt.clear();
			for (int i = 0; i < tmp.length(); i++) {
				if (i == m) {
					;
				}else {
					tt.add(tmp.charAt(i));
				}
			}
		}
		selectedI = -1;
		change();
	}
	
	private boolean removeSelected() {
		if (!listening())
			return false;
		if (text().length() == 0)
			return false;
		int m = marker();
		if (selectedI >= 0 && selectedI != m) {
			int s = Math.min(selectedI, marker);
			int e = Math.max(selectedI, marker);
			tmp.clear().add(text());
			Str tt = text();
			tt.clear();
			for (int i = 0; i < tmp.length(); i++) {
				if (i < s || i >= e)
					tt.add(tmp.charAt(i));
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void left(boolean mod) {
		if (mod) {
			if (selectedI >= 0)
				selectedI --;
			else
				selectedI = marker-1;
			if (selectedI < 0)
				selectedI = 0;
		}else if (selectedI >= 0) {
			marker = selectedI;
			selectedI = -1;
		}else {
			selectedI = -1;
			marker --;
			if (marker < 0)
				marker = 0;
		}
	}

	@Override
	public void right(boolean mod) {
		if (mod) {
			if (selectedI >= 0)
				selectedI ++;
			else
				selectedI = marker+1;
			if (selectedI > text().length())
				selectedI = text().length();
		}else if (selectedI >= 0) {
			marker = selectedI;
			selectedI = -1;
		}else {
			
			marker ++;
			if (marker > text().length())
				marker = text().length();
		}
		
	}
	
	public void click(int x1){
		marker = findX(x1);
		selectedI = -1;
	}
	
	public void select(int x){
		selectedI = -1;
		selectedI = findX(x);
	}
	
	public void selectAll(){
		marker = 0;
		selectedI = text().length();
	}
	
	private int findX(int x1) {
		int x = 0;
		int m = marker();
		
		if (text().length() == 0)
			return 0;
		
		
		
		for (int i = 0; i < m; i++) {
			int w = width(i);
			x +=  w;
			if (x - w/2 >= x1) {
				return i;
			}
		}
		if (selectedI < 0) {
			x += f.width(promt.charAt(0), 1.0)+8;
			if (x > x1) {
				return m;
			}
		}
		
		for (int i = m; i < text().length(); i++) {
			int w = width(i);
			x +=  w;
			if (x - w/2 >= x1) {
				return i;
			}
		}
		return text().length();
	}
	
	private int width(int index) {
		int w = f.width(text().charAt(index), 1.0);
		if (index > 0)
			w -= f.getBack(text().charAt(index-1), text().charAt(index), 1.0);
		return w;
	}

	
	@Override
	public int width() {
		if (text().length() == 0) {
			if (placeholder != null && !listening())
				return f.getDim(placeholder).x();
			else
				return 0;
		}
		return f.getDim(text()).x();
	}
	
	@Override
	protected void enter() {
		Mouse.currentClicked = null;
	}

	@Override
	public int height() {
		return f.height();
	}

	private int marker() {
		if (marker > text().length())
			marker = text().length();
		if (marker < 0)
			marker = 0;
		return marker;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

		int marker = marker();
		tmp.clear().add(text());
		selectedI = CLAMP.i(selectedI, -1, tmp.length());

		renAction();
	
		
		if (!listening()) {
			if (tmp.length() == 0 && placeholder != null){
				COLOR.WHITE65.bind();
				f.render(r, placeholder, X1, Y1, X2-X1, 1);
				COLOR.unbind();
			}else {
				f.render(r, tmp, X1, Y1, X2-X1, 1);
			}
		}else if (selectedI >= 0 && selectedI != marker) {
			int i1 = Math.min(marker, selectedI);
			int i2 = Math.max(marker, selectedI);
			
			int sx1 = X1 + f.getDim(tmp, 0, i1, Integer.MAX_VALUE, 1.0).x()-2;
			int sx2 = sx1 + f.getDim(tmp, i1, i2, Integer.MAX_VALUE, 1.0).x()+2;
			COLOR.WHITE50.render(r, sx1, sx2, Y1, Y2);
			f.render(r, tmp, X1, Y1, X2-X1, 1);
		}else {
			f.render(CORE.renderer(), tmp, X1, Y1,0, marker,  1);
			
			X1 += f.getDim(text(), 0, marker, Integer.MAX_VALUE, 1.0).x()+4;
			COLOR.BLACK2WHITE.bind();
			f.render(r, promt, X1, Y1);
			COLOR.unbind();
			X1 += 4;
			if (marker() < tmp.length()) {
				f.render(CORE.renderer(), tmp,X1, Y1,marker,text().length(),  1);
			}
		}
//		
//		
//		if (text().length() == 0 && !listening() && selectedII < 0) {
//			if (placeholder != null)
//				f.render(r, placeholder, X1, Y1, X2-X1, 1);
//		}else if (listening() && selectedII < 0) {
//			f.render(CORE.renderer(), text(), X1, Y1,0, marker,  1);
//			
//			X1 += f.getDim(text(), 0, marker, Integer.MAX_VALUE, 1.0).x()+4;
//			COLOR.BLACK2WHITE.bind();
//			f.render(r, promt, X1, Y1);
//			COLOR.unbind();
//			X1 += 4;
//			if (marker < text().length()) {
//				f.render(CORE.renderer(), text(),X1, Y1,marker,text().length(),  1);
//			}
//			
//		}else {
//			f.render(r, text(), X1, Y1, X2-X1, 1);
//		}
	}
	
	public void renAction() {
		
	}
	
	@Override
	protected void change() {
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		// TODO Auto-generated method stub
		
	}
	
	public InputClickable c(DIR d) {
		return new InputClickable(this, d);
	}
	
	public static class InputClickable extends CLICKABLE.ClickableAbs {

		private final StringInputSprite input;
		private COLOR hoverC = COLOR.WHITE2WHITE;
		private COLOR color = COLOR.WHITE100;
		private COLOR active = color.shade(0.7);
		private DIR rep;
		
		InputClickable(StringInputSprite input, DIR rep){
			this.input = input;
			while (input.text().spaceLeft() > 0) {
				input.text().add('n');
			}
			int w = input.width();
			body.setWidth(w);
			body.setHeight(input.height());
			input.text().clear();
			this.rep = rep;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			input.renAction();
			if (Mouse.currentClicked == this)
				input.listen();
			
			int dx = (body().width()-input.width())/2;
			
			int x1 = body().x1() + (rep.x()+1)*dx;
			if (!isActive)
				active.bind();
			if (isHovered || Mouse.currentClicked == this)
				hoverC.bind();
			else
				color.bind();
			input.render(r, x1, body.y1());
			COLOR.unbind();
		}
		
		@Override
		public boolean click() {
			if (super.click()) {
				Mouse.currentClicked = this;
				input.listen();
				input.marker = input.text().length();
				return true;
			}
			return false;
		}
		
		public void focus() {
			Mouse.currentClicked = this;
			input.listen();
		}
		
		public InputClickable colors(COLOR normal, COLOR hover) {
			this.hoverC = hover;
			this.color = normal;
			this.active = color.shade(0.7);
			return this;
		}
		
	}


}
