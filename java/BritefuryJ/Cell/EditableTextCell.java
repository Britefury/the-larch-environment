//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.awt.event.KeyEvent;
import java.util.IdentityHashMap;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.TextClipboardHandler;
import BritefuryJ.DocPresent.Interactor.CaretCrossingElementInteractor;
import BritefuryJ.DocPresent.Interactor.KeyElementInteractor;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Util.UnaryFn;

public class EditableTextCell
{
	public static Pres textCellWithCachedListener(String text, UnaryFn textToValue)
	{
		TreeEventListener listener = cachedTreeEventListenerFor( textToValue );
		Pres textPres = new Segment( new Text( text ) );
		textPres = textPres.withElementInteractor( caretInteractor ).withElementInteractor( keyInteractor ).withTreeEventListener( listener );
		return new Bin( new Region( textPres, clipboardHandler ) );
	}


	public static Pres textCell(String text, UnaryFn textToValue)
	{
		TreeEventListener listener = treeEventListenerFor( textToValue );
		Pres textPres = new Segment( new Text( text ) );
		textPres = textPres.withElementInteractor( caretInteractor ).withElementInteractor( keyInteractor ).withTreeEventListener( listener );
		return new Bin( new Region( textPres, clipboardHandler ) );
	}


	private static final TextClipboardHandler clipboardHandler = new TextClipboardHandler()
	{
		@Override
		protected void deleteText(TextSelection selection, Caret caret)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		@Override
		protected void insertText(Marker marker, String text)
		{
			DPText textElement = (DPText)marker.getElement();
			textElement.insertText( marker, text );
		}
		
		@Override
		protected void replaceText(TextSelection selection, Caret caret, String replacement)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			textElement.replaceText( selection.getStartMarker(), selection.getEndMarker(), replacement );
		}
		
		@Override
		protected String getText(TextSelection selection)
		{
			return selection.getStartMarker().getElement().getRootElement().getTextRepresentationInSelection( selection );
		}
	};
	
	
	private static final CaretCrossingElementInteractor caretInteractor = new CaretCrossingElementInteractor()
	{
		@Override
		public void caretEnter(DPElement element, Caret c)
		{
		}

		@Override
		public void caretLeave(DPElement element, Caret c)
		{
			// Only send a commit event if:
			// - the caret is valid
			// - the current target is the caret - e.g. the user is editing text within the cell
			// If we do not check for this:
			// - When pasting data from a spreadsheet into a table editor, a cell is chosen to position the pasted data. Choosing this position will normally alter
			//   the position of the caret, but it will not be visible, as the table highlight is the current target. The paste operation will cause the table to be refreshed.
			//   This involves removing presentation elements, which will cause the caret to leave them, resulting in this event being received. This will result in the
			//   text representation of the *old* data being retrieved, and commit, overwriting the pasted data, preventing the current cell from being successfully
			//   altered by the paste operation.
			if ( c.isValid()  &&  element.getRootElement().getTarget() == c )
			{
				element.postTreeEvent( CommitEvent.instance );
			}
		}
	};
	
	
	private static final KeyElementInteractor keyInteractor = new KeyElementInteractor()
	{
		@Override
		public boolean keyPressed(DPElement element, KeyEvent event)
		{
			return false;
		}

		@Override
		public boolean keyReleased(DPElement element, KeyEvent event)
		{
			return false;
		}

		@Override
		public boolean keyTyped(DPElement element, KeyEvent event)
		{
			if ( event.getKeyChar() == '\n' )
			{
				element.postTreeEvent( CommitEvent.instance );
				return true;
			}
			return false;
		}
	};
	
	
	
	private static final IdentityHashMap<UnaryFn, TreeEventListener> textCellTreeEventListeners = new IdentityHashMap<UnaryFn, TreeEventListener>();
	
	private static TreeEventListener cachedTreeEventListenerFor(final UnaryFn textToValue)
	{
		TreeEventListener listener = textCellTreeEventListeners.get( textToValue );
		
		if ( listener == null )
		{
			listener = treeEventListenerFor( textToValue );
			textCellTreeEventListeners.put( textToValue, listener );
		}
		
		return listener;
	}
	
	
	private static TreeEventListener treeEventListenerFor(final UnaryFn textToValue)
	{
		TreeEventListener listener = new TreeEventListener()
		{
			public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
			{
				if ( event instanceof CommitEvent )
				{
					// Attempt to commit the value
					String textValue = element.getTextRepresentation();
					Object value = textToValue.invoke( textValue );
					if ( value != null )
					{
						CellEditPerspective.notifySetCellValue( element, value );
					}
					return true;
				}
				else if ( event instanceof TextEditEvent )
				{
					// Ignore text edit events - let the text element have its contents modified, until a commit event is received
					return true;
				}
				return false;
			}
		};
		
		return listener;
	}
	
	
	
	private static class CommitEvent
	{
		public static final CommitEvent instance = new CommitEvent();
	}
}
