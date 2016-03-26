//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.event.KeyEvent;

public interface StateKeyListener {
	public void onStateKeyPress(KeyEvent event);
	public void onStateKeyRelease(KeyEvent event);
	public void onStateKeyTyped(KeyEvent event);
}
