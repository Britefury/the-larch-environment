//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Focus;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;

public abstract class Target
{
	public interface TargetModificationListener
	{
		public void notifyTargetModified(Target t);
	}

	protected TargetModificationListener modificationListener;
	protected boolean active = false;


	public boolean drawWhenComponentNotFocused()
	{
		return true;
	}

	public abstract void draw(Graphics2D graphics);
	
	public boolean isAnimated()
	{
		return false;
	}
	
	
	public abstract LSElement getElement();
	
	public LSRegion getRegion()
	{
		return isValid()  ?  LSRegion.regionOf( getElement() )  :  null;
	}
	
	
	
	public LSElement getKeyboardInputElement()
	{
		return null;
	}
	
	
	// Notify target that is is current
	public void notifyActivate()
	{
		active = true;
	}
	
	// Notify target that is is no longer current
	public void notifyDeactivate()
	{
		active = false;
	}
	
	
	
	public abstract boolean isEditable();
	
	
	
	public abstract SelectionPoint createSelectionPoint();
	
	
	public boolean isValid()
	{
		return true;
	}
	
	
	public boolean onContentKeyPress(KeyEvent event)
	{
		return false;
	}

	public boolean onContentKeyRelease(KeyEvent event)
	{
		return false;
	}

	public boolean onContentKeyTyped(KeyEvent event)
	{
		return false;
	}



	public void moveLeft()
	{
	}

	public void moveRight()
	{
	}
	
	public void moveUp()
	{
	}
	
	public void moveDown()
	{
	}
	

	public void moveToHome()
	{
	}
	
	public void moveToEnd()
	{
	}
	
	
	
	public void ensureVisible()
	{
	}



	public void setModificationListener(TargetModificationListener modificationListener)
	{
		this.modificationListener = modificationListener;
	}


	
	
	protected void notifyModified()
	{
		if ( modificationListener != null )
		{
			modificationListener.notifyTargetModified( this );
		}
	}
}
