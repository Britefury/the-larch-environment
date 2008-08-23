package BritefuryJ.DocPresent;

import java.awt.event.KeyEvent;

public interface StateKeyListener {
	public void onStateKeyPress(KeyEvent event);
	public void onStateKeyRelease(KeyEvent event);
	public void onStateKeyTyped(KeyEvent event);
}
