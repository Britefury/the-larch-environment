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
import BritefuryJ.DocPresent.ElementTree.EditableEntryLeafElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.Math.Point2;

public abstract class DPContentLeafEditableEntry extends DPContentLeafEditable
{
	public static class EditableEntryLeafWidgetFilter extends WidgetFilter
	{
		public boolean testLeaf(DPContentLeaf leaf)
		{
			return leaf instanceof DPContentLeafEditableEntry;
		}
	}
	
	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditableEntry()
	{
		super();
	}
	
	protected DPContentLeafEditableEntry(ContentLeafStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	

	
	
	
	//
	// Specialised getElement()
	//
	
	public EditableEntryLeafElement getElement()
	{
		return (EditableEntryLeafElement)element;
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
			DPContentLeafEditable left = getEditableContentLeafToLeft();
			if ( left == null )
			{
				return false;
			}
			else
			{
				while ( !left.isEditableEntry() )
				{
					left.clearContent();
					left = left.getEditableContentLeafToLeft();
					if ( left == null )
					{
						return false;
					}
				}
				left.moveMarkerToEnd( caret.getMarker() );
				left.removeContentFromEnd( 1 );
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
			DPContentLeafEditable right = getEditableContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				while ( !right.isEditableEntry() )
				{
					right.clearContent();
					right = right.getEditableContentLeafToRight();
					if ( right == null )
					{
						return false;
					}
				}
				right.moveMarkerToStart( caret.getMarker() );
				right.removeContentFromStart( 1 );
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
	
	
	
//	protected boolean onButtonDown(PointerButtonEvent event)
//	{
//		if ( event.getButton() == 1 )
//		{
//			Caret caret = presentationArea.getCaret();
//			int markerPos = getMarkerPositonForPoint( event.getPointer().getLocalPos() );
//			moveMarker( caret.getMarker(), markerPos, Marker.Bias.START );
//			return true;
//		}
//		else
//		{
//			return false;
//		}
//	}
	
	
	
	protected void placeCursor(Point2 localPos)
	{
		Caret caret = presentationArea.getCaret();
		int markerPos = getMarkerPositonForPoint( localPos );
		moveMarker( caret.getMarker(), markerPos, Marker.Bias.START );
	}




	//
	// TYPE METHODS
	//
	
	public boolean isEditableEntry()
	{
		return true;
	}
}
