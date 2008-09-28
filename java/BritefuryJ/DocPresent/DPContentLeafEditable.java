//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.ElementTree.EditableLeafElement;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;

public abstract class DPContentLeafEditable extends DPContentLeaf
{
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditable()
	{
		super();
	}
	
	protected DPContentLeafEditable(ContentLeafStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	

	
	
	
	//
	// Specialised getElement()
	//
	
	public EditableLeafElement getElement()
	{
		return (EditableLeafElement)element;
	}
	
	
	
	//
	// Content modification
	//
	
	public void insertContent(Marker marker, String x)
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.insertContent( marker, x );
		}
	}

	public void removeContent(Marker marker, int length)
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.removeContent( marker, length );
		}
	}
	
	public void replaceContent(Marker marker, int length, String x)
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.replaceContent( marker, length, x );
		}
	}
	
	
	

	//
	//
	// INPUT EVENT HANDLING
	//
	//
	
	protected boolean handleBackspace(Caret caret)
	{
		if ( isMarkerAtStart( caret.getMarker() ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			if ( left == null )
			{
				return false;
			}
			else
			{
				left.moveMarkerToEnd( caret.getMarker() );
				return true;
			}
		}
		else
		{
			removeContent( markerToLeft( caret.getMarker(), false, true ), 1 );
			return true;
		}
	}
	
	protected boolean handleDelete(Caret caret)
	{
		if ( isMarkerAtEnd( caret.getMarker() ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				right.moveMarkerToStart( caret.getMarker() );
				return true;
			}
		}
		else
		{
			removeContent( caret.getMarker(), 1 );
			return true;
		}
	}
	
	protected boolean onKeyPress(Caret caret, KeyEvent event)
	{
		if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE )
		{
			return handleBackspace( caret );
		}
		else if ( event.getKeyCode() == KeyEvent.VK_DELETE )
		{
			return handleDelete( caret );
		}
		return false;
	}

	protected boolean onKeyRelease(Caret caret, KeyEvent event)
	{
		return false;
	}

	protected boolean onKeyTyped(Caret caret, KeyEvent event)
	{
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			insertContent( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	
	protected boolean onButtonDown(PointerButtonEvent event)
	{
		if ( event.getButton() == 1 )
		{
			Caret caret = presentationArea.getCaret();
			int markerPos = getMarkerPositonForPoint( event.getPointer().getLocalPos() );
			moveMarker( caret.getMarker(), markerPos, Marker.Bias.START );
			return true;
		}
		else
		{
			return false;
		}
	}




	//
	// Type methods
	//
	
	public boolean isEditable()
	{
		return true;
	}
}
