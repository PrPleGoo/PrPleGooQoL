package snake2d;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.GraphicContext;
import snake2d.Input.CHAR_LISTENER;
import snake2d.KEYCODES;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;

public class KeyBoard {

	private final GLFWKeyCallback callback;
	private final GLFWCharCallback charCallback;
	
	private final char[] chars = new char[128];
	private final int[] keys = new int[3*128];

	private volatile int charsI = 0;
	private volatile int keysI = 0;
	CHAR_LISTENER listener;
	private boolean listening = false;
	
	private final ArrayList<KeyEvent> pollsA = new ArrayList<>(128);
	private final ArrayList<KeyEvent> polls = new ArrayList<>(128);
	
	
	public enum KEYACTION{
		RELEASE,PRESS,REPEAT;
		
		public static final LIST<KEYACTION> ALL = new ArrayList<KEYACTION>(values());
	}
	
	KeyBoard(GraphicContext w){
		
		while (pollsA.hasRoom()) {
			pollsA.add(new KeyEvent());
		}

		callback = new GLFWKeyCallback() {

			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				
				if (keysI >= keys.length-1)
					return;
				
				if (KEYCODES.exists(key)){
					if (key == KEYCODES.KEY_PRINT_SCREEN){
						if (action == 1)
							w.takeScreenShot();
						return;
					}
					
					keys[keysI] = key;
					keys[keysI+1] = action;
					keys[keysI+2] = mods;
					
					
					keysI += 3;
				}
				
			}
		};
		GLFW.glfwSetKeyCallback(w.getWindow(), callback);
		
		charCallback = new GLFWCharCallback() {
			@Override
			public void invoke(long window, int codepoint) {
				if (charsI >= chars.length)
					return;
				chars[charsI] = (char) codepoint;
				charsI ++;
				
			}
		};
		
		glfwSetCharCallback(w.getWindow(), charCallback);
	}
	

	
	void poll (CORE_STATE current, boolean cleared){
		
		
		int pi = 0;
		for (int i = 0; i < keysI; i+=3){
			KEYACTION a = KEYACTION.ALL.get(keys[i+1]);
			int c = keys[i];
			if (listener != null && c != KEYCODES.KEY_ESCAPE) {
				if (listener != null && (a == KEYACTION.PRESS || a == KEYACTION.REPEAT)) {
					if (c == KEYCODES.KEY_ENTER
					|| c == KEYCODES.KEY_KP_ENTER)
						listener.enter();
					if (c == KEYCODES.KEY_BACKSPACE)
						listener.backspace();
					if (c == KEYCODES.KEY_LEFT)
						listener.left(GLFW.glfwGetKey(CORE.getGraphics().getWindow(), KEYCODES.KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS);
					if (c == KEYCODES.KEY_RIGHT)
						listener.right(GLFW.glfwGetKey(CORE.getGraphics().getWindow(), KEYCODES.KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS);
					if (c == KEYCODES.KEY_DELETE)
						listener.del();
				}
				if (c == KEYCODES.KEY_ENTER) {
					
					polls.add(pollsA.get(pi++).assign(keys[i], KEYACTION.ALL.get(keys[i+1]), keys[i+2]));
				}
					
			}else{
				polls.add(pollsA.get(pi++).assign(keys[i], KEYACTION.ALL.get(keys[i+1]), keys[i+2]));
			}
			
		}
		
		current.keyPush(polls, cleared);
		
		if (listener != null) {
			for (int i = 0; i < charsI; i++){
				listener.acceptChar(chars[i]);
			}
			listening = true;
		}else {
			listening = false;
		}
		clear();
	}
	
	void clear(){
		keysI = 0;
		charsI = 0;
		polls.clear();
		listener = null;
	}
	
	void release(){
		callback.close();
		charCallback.close();
	}
	
	public boolean isPressed(int code) {
		return CORE.getGraphics().focused() && listener == null && !listening && GLFW.glfwGetKey(CORE.getGraphics().getWindow(), code) == GLFW.GLFW_PRESS;
	}
	
	public String translate(int code) {
		return GLFW.glfwGetKeyName(code, GLFW.GLFW_KEY_UNKNOWN);
	}
	
	public static final class KeyEvent {
		
		private int code;
		private KEYACTION action;
		private int mod;
		
		private KeyEvent() {
			
		}
		
		KeyEvent assign(int code, KEYACTION action, int mod) {
			this.code = code;
			this.action = action;
			this.mod = mod;
			return this;
		}
		
		public int code() {
			return code;
		}
		
		public KEYACTION action() {
			return action;
		}
		
		public int mod() {
			return mod;
		}
		
	}
	
}
