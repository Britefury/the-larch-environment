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
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;

public abstract class DPContentLeafEditableEntry extends DPContentLeafEditable
{
	public static class EditableEntryLeafElementFilter implements WidgetFilter
	{
		public boolean testElement(DPWidget element)
		{
			return element instanceof DPContentLeafEditableEntry;
		}
	}
	
	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditableEntry(String textRepresentation)
	{
		super( textRepresentation );
	}
	
	protected DPContentLeafEditableEntry(ContentLeafStyleSheet styleSheet, String textRepresentation)
	{
		super( styleSheet, textRepresentation );
	}
	

	
	
	
	//
	//
	// INPUT EVENT HANDLING
	//
	//
	
	protected boolean handleBackspace(Caret caret)
	{
		if ( presentationArea.isSelectionValid() )
		{
			presentationArea.deleteSelection();
			return true;
		}
		else if ( isMarkerAtStart( caret.getMarker() ) )
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
					bNonEntryContentCleared |= left.clearText();
					left = left.getEditableContentLeafToLeft();
					if ( left == null )
					{
						return false;
					}
				}
				left.moveMarkerToEnd( caret.getMarker() );
				if ( !bNonEntryContentCleared )
				{
					left.removeTextFromEnd( 1 );
				}
				return true;
			}
		}
		else
		{
			removeText( caret.getMarker().getClampedIndex() - 1, 1 );
			return true;
		}
	}
	
	protected boolean handleDelete(Caret caret)
	{
		if ( presentationArea.isSelectionValid() )
		{
			presentationArea.deleteSelection();
			return true;
		}
		else if ( isMarkerAtEnd( caret.getMarker() ) )
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
					bNonEntryContentCleared |= right.clearText();
					right = right.getEditableContentLeafToRight();
					if ( right == null )
					{
						return false;
					}
				}
				right.moveMarkerToStart( caret.getMarker() );
				if ( !bNonEntryContentCleared )
				{
					right.removeTextFromStart( 1 );
				}
				return true;
			}
		}
		else
		{
			removeText( caret.getMarker(), 1 );
			return true;
		}
	}
	
	protected boolean onKeyPress(Caret caret, KeyEvent event)
	{
		if ( propagateKeyPress( event ) )
		{
			return true;
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
		return propagateKeyRelease( event );
	}

	protected boolean onKeyTyped(Caret caret, KeyEvent event)
	{
		if ( propagateKeyTyped( event ) )
		{
			return true;
		}
		
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			String str = String.valueOf( event.getKeyChar() );
			if ( str.length() > 0 )
			{
				if ( presentationArea.isSelectionValid() )
				{
					presentationArea.replaceSelection( str );
				}
				else
				{
					insertText( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
				}
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
