//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.LSpace.Input.Keyboard.KeyboardInteractor;
import BritefuryJ.Projection.Subject;

public abstract class AbstractCommandConsole implements Presentable
{
	private CommandConsoleListener listener = null;
	
	
	
	public void setListener(CommandConsoleListener listener)
	{
		this.listener = listener;
	}
	
	
	
	protected void notifyFinished()
	{
		if ( listener != null )
		{
			listener.finished( this );
		}
	}

	
	public abstract Subject getSubject();
	public abstract void pageChanged(Subject subject);
	public abstract KeyboardInteractor getShortcutKeyboardInteractor();
}
