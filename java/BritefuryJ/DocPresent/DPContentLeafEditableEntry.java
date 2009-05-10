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
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;

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
				boolean bNonEntryContentCleared = false;
				while ( !left.isEditableEntry() )
				{
					bNonEntryContentCleared |= left.clearContent();
					left = left.getEditableContentLeafToLeft();
					if ( left == null )
					{
						return false;
					}
				}
				left.moveMarkerToEnd( caret.getMarker() );
				if ( !bNonEntryContentCleared )
				{
					left.removeContentFromEnd( 1 );
				}
				return true;
			}
		}
		else
		{
			removeContent( caret.getMarker().getIndex() - 1, 1 );
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
				boolean bNonEntryContentCleared = false;
				while ( !right.isEditableEntry() )
				{
					bNonEntryContentCleared |= right.clearContent();
					right = right.getEditableContentLeafToRight();
					if ( right == null )
					{
						return false;
					}
				}
				right.moveMarkerToStart( caret.getMarker() );
				if ( !bNonEntryContentCleared )
				{
					right.removeContentFromStart( 1 );
				}
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
		EditableEntryLeafElement entryElement = getElement();
		if ( entryElement != null )
		{
			if ( entryElement.onKeyPress( event ) )
			{
				return true;
			}
		}
		
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
		EditableEntryLeafElement entryElement = getElement();
		if ( entryElement != null )
		{
			if ( entryElement.onKeyRelease( event ) )
			{
				return true;
			}
		}
		
		return false;
	}

	protected boolean onKeyTyped(Caret caret, KeyEvent event)
	{
		EditableEntryLeafElement entryElement = getElement();
		if ( entryElement != null )
		{
			if ( entryElement.onKeyTyped( event ) )
			{
				return true;
			}
		}
		
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			String str = String.valueOf( event.getKeyChar() );
			if ( str.length() > 0 )
			{
				insertContent( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
				return true;
			}
		}

		return false;
	}
	
	
	


	//
	// TYPE METHODS
	//
	
	public boolean isEditableEntry()
	{
		return true;
	}
}
