//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.awt.event.KeyEvent;
import java.util.IdentityHashMap;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Caret.Caret;
import BritefuryJ.LSpace.Clipboard.TextClipboardHandler;
import BritefuryJ.LSpace.Interactor.CaretCrossingElementInteractor;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Selection.TextSelection;
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
			LSText textElement = (LSText)selection.getStartMarker().getElement();
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		@Override
		protected void insertText(Marker marker, String text)
		{
			LSText textElement = (LSText)marker.getElement();
			textElement.insertText( marker, text );
		}
		
		@Override
		protected void replaceText(TextSelection selection, Caret caret, String replacement)
		{
			LSText textElement = (LSText)selection.getStartMarker().getElement();
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
		public void caretEnter(LSElement element, Caret c)
		{
		}

		@Override
		public void caretLeave(LSElement element, Caret c)
		{
			// Note: we have had prior problems with:
			// - When pasting data from a spreadsheet into a table editor, a cell is chosen to position the pasted data. Choosing this position will normally alter
			//   the position of the caret, but it will not be visible, as the table highlight is the current target. The paste operation will cause the table to be refreshed.
			//   This involves removing presentation elements, which will cause the caret to leave them, resulting in this event being received. This will result in the
			//   text representation of the *old* data being retrieved, and commit, overwriting the pasted data, preventing the current cell from being successfully
			//   altered by the paste operation.
			// Initially this was addressed by:
			// - ensuring that the caret is valid, and it is the current target
			// Unfortunately, this prevents changes from being committed to cells when the target was changed to a non-caret target
			// Since caret crossing (enter/leave) events are now only sent when the caret is valid, and the target is the caret,
			// (with enter/leave events being sent when the target becomes the caret, or when it is set to something else),
			// these checks are no longer necessary - in fact they are detrimental.
			if ( element.isRealised() )
			{
				element.postTreeEvent( CommitEvent.instance );
			}
		}
	};
	
	
	private static final KeyElementInteractor keyInteractor = new KeyElementInteractor()
	{
		@Override
		public boolean keyPressed(LSElement element, KeyEvent event)
		{
			return false;
		}

		@Override
		public boolean keyReleased(LSElement element, KeyEvent event)
		{
			return false;
		}

		@Override
		public boolean keyTyped(LSElement element, KeyEvent event)
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
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
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
