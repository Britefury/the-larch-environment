//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.IdentityHashMap;

import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Clipboard.TextClipboardHandler;
import BritefuryJ.LSpace.Interactor.CaretCrossingElementInteractor;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Shortcut.Shortcut;
import BritefuryJ.Shortcut.ShortcutElementAction;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;
import BritefuryJ.Util.UnaryFn;

public class EditableTextCell
{
	private static class Cell extends CompositePres
	{
		private static final StyleSheet invalidStyle = StyleSheet.style( Primitive.textSquiggleUnderlinePaint.as( Color.RED ) );
		private static final StyleSheet uncommittedStyle = StyleSheet.style( Primitive.background.as( new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.75f ), new Color( 1.0f, 0.75f, 0.5f ) ) ) );
		
		
		TreeEventListener textListener = new TreeEventListener()
		{
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					String t = element.getTextRepresentation();
					if ( !t.contains( "\n" ) )
					{
						// Text has been modified
						modified = true;
						
						// Set the text and valid fields
						text = t;
						valid = textToValue.invoke( t ) != null;
					}

					// We are queuing a refresh, so the element will be re-created
					// Clear the caret-entered flag
					caretEntered = false;
					listeners = PresentationStateListenerList.onPresentationStateChanged( listeners, Cell.this );
					return true;
				}
				return false;
			}
		};
		
		
		private final CaretCrossingElementInteractor caretInteractor = new CaretCrossingElementInteractor()
		{
			@Override
			public void caretEnter(LSElement element, Caret c)
			{
				// The caret has entered
				caretEntered = true;
			}

			@Override
			public void caretLeave(LSElement element, Caret c)
			{
				// Note: there have been prior problems with:
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
				if ( element.isRealised()  &&  modified  &&  caretEntered )
				{
					// Source element is realised, modifications have been made and the caret has previously entered.
					// the modified flag persists across refreshes
					// the truth value of caret-entered will therefore indicate that the caret has left the cell without any edits
					// therefore the user intends to leave the cell, so commit
					Object value = textToValue.invoke( text );
					if ( value != null )
					{
						commit( element, value );
					}
				}
			}
		};
		
		
		
		
		
		private UnaryFn textToValue;
		private String text;
		private boolean valid, modified, caretEntered;
		private PresentationStateListenerList listeners = null;
		
		
		// Commit on enter
		private static final Shortcut enterShortcut = new Shortcut( '\n', 0 );
		
		private ShortcutElementAction onEnter = new ShortcutElementAction()
		{
			@Override
			public void invoke(LSElement element)
			{
				Object value = textToValue.invoke( text );
				if ( value != null )
				{
					commit( element, value );
				}
			}
		};
		
		
		public Cell(UnaryFn textToValue, String text)
		{
			this.textToValue = textToValue;
			this.text = text;
			this.valid = true;
			this.modified = false;
			this.caretEntered = false;
		}
		
		
		private void commit(LSElement element, Object value)
		{
			modified = false;
			CellEditPerspective.notifySetCellValue( element, value );
			listeners = PresentationStateListenerList.onPresentationStateChanged( listeners, Cell.this );
		}
		
		
		@Override
		public Pres pres(PresentationContext ctx, StyleValues style)
		{
			ctx.getFragment().disableInspector();
			
			listeners = PresentationStateListenerList.addListener( listeners, ctx.getFragment() );
			Pres textPres = new Segment( new Text( text ) ).withTreeEventListener( textListener );
			
			if ( !valid )
			{
				textPres = invalidStyle.applyTo( textPres );
			}
			
			if ( modified )
			{
				textPres = uncommittedStyle.applyTo( textPres );
			}
			
			textPres = textPres.withShortcut( enterShortcut, onEnter );
			
			return new Bin( new Region( textPres, clipboardHandler ) ).withElementInteractor( caretInteractor );
		}
	}
	
	
	public static Pres textCellWithCachedListener(String text, UnaryFn textToValue)
	{
		return new Bin( new InnerFragment( new Cell( textToValue, text ) ) );
	}


	public static Pres textCell(String text, UnaryFn textToValue)
	{
		return new Bin( new InnerFragment( new Cell( textToValue, text ) ) );
	}


/*	public static Pres textCellWithCachedListener(String text, UnaryFn textToValue)
	{
		TreeEventListener commitListener = cachedCommitTreeEventListenerFor( textToValue );
		Pres textPres = new Segment( new Text( text ) );
		textPres = new Bin( new Region( textPres, clipboardHandler ) );
		return textPres.withElementInteractor( caretInteractor ).withElementInteractor( keyInteractor ).withTreeEventListener( commitListener );
	}


	public static Pres textCell(String text, UnaryFn textToValue)
	{
		TreeEventListener commitListener = commitTreeEventListenerFor( textToValue );
		Pres textPres = new Segment( new Text( text ) );
		textPres = new Bin( new Region( textPres, clipboardHandler ) );
		return textPres.withElementInteractor( caretInteractor ).withElementInteractor( keyInteractor ).withTreeEventListener( commitListener );
	}*/


	public static Pres blankTextCellWithCachedListener(String text, UnaryFn textToValue)
	{
		TreeEventListener textListener = cachedTreeEventListenerFor( textToValue );
		Pres textPres = new Segment( new Text( text ) ).withTreeEventListener( textListener ).withElementInteractor( keyInteractor );
		return new Bin( new Region( textPres, clipboardHandler ) );
	}


	public static Pres blankTextCell(String text, UnaryFn textToValue)
	{
		TreeEventListener textListener = treeEventListenerFor( textToValue );
		Pres textPres = new Segment( new Text( text ) ).withTreeEventListener( textListener ).withElementInteractor( keyInteractor );
		return new Bin( new Region( textPres, clipboardHandler ) );
	}


	private static final TextClipboardHandler clipboardHandler = new TextClipboardHandler()
	{
		@Override
		protected void deleteText(TextSelection selection, Caret caret)
		{
			LSContentLeafEditable textElement = (LSContentLeafEditable)selection.getStartMarker().getElement();
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		@Override
		protected void insertText(Marker marker, String text)
		{
			LSContentLeafEditable textElement = (LSContentLeafEditable)marker.getElement();
			textElement.insertText( marker, text );
		}
		
		@Override
		protected void replaceText(TextSelection selection, Caret caret, String replacement)
		{
			LSContentLeafEditable textElement = (LSContentLeafEditable)selection.getStartMarker().getElement();
			textElement.replaceText( selection.getStartMarker(), selection.getEndMarker(), replacement );
		}
		
		@Override
		protected String getText(TextSelection selection)
		{
			return selection.getStartMarker().getElement().getRootElement().getTextRepresentationInSelection( selection );
		}
	};
	
	
//	private static final CaretCrossingElementInteractor caretInteractor = new CaretCrossingElementInteractor()
//	{
//		@Override
//		public void caretEnter(LSElement element, Caret c)
//		{
//		}
//
//		@Override
//		public void caretLeave(LSElement element, Caret c)
//		{
//			// Note: we have had prior problems with:
//			// - When pasting data from a spreadsheet into a table editor, a cell is chosen to position the pasted data. Choosing this position will normally alter
//			//   the position of the caret, but it will not be visible, as the table highlight is the current target. The paste operation will cause the table to be refreshed.
//			//   This involves removing presentation elements, which will cause the caret to leave them, resulting in this event being received. This will result in the
//			//   text representation of the *old* data being retrieved, and commit, overwriting the pasted data, preventing the current cell from being successfully
//			//   altered by the paste operation.
//			// Initially this was addressed by:
//			// - ensuring that the caret is valid, and it is the current target
//			// Unfortunately, this prevents changes from being committed to cells when the target was changed to a non-caret target
//			// Since caret crossing (enter/leave) events are now only sent when the caret is valid, and the target is the caret,
//			// (with enter/leave events being sent when the target becomes the caret, or when it is set to something else),
//			// these checks are no longer necessary - in fact they are detrimental.
//			if ( element.isRealised() )
//			{
//				element.postTreeEvent( CommitEvent.instance );
//			}
//		}
//	};
	
	
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
	
	
//	private static final IdentityHashMap<UnaryFn, TreeEventListener> textCellCommitTreeEventListeners = new IdentityHashMap<UnaryFn, TreeEventListener>();
//	
//	private static TreeEventListener cachedCommitTreeEventListenerFor(final UnaryFn textToValue)
//	{
//		TreeEventListener listener = textCellCommitTreeEventListeners.get( textToValue );
//		
//		if ( listener == null )
//		{
//			listener = commitTreeEventListenerFor( textToValue );
//			textCellCommitTreeEventListeners.put( textToValue, listener );
//		}
//		
//		return listener;
//	}
	
	
//	private static TreeEventListener commitTreeEventListenerFor(final UnaryFn textToValue)
//	{
//		TreeEventListener listener = new TreeEventListener()
//		{
//			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
//			{
//				if ( event instanceof CommitEvent )
//				{
//					// Attempt to commit the value
//					String textValue = element.getTextRepresentation();
//					Object value = textToValue.invoke( textValue );
//					if ( value != null )
//					{
//						CellEditPerspective.notifySetCellValue( element, value );
//					}
//					return true;
//				}
//				return false;
//			}
//		};
//		
//		return listener;
//	}
	
	
	
	private static TreeEventListener treeEventListenerFor(final UnaryFn textToValue)
	{
		TreeEventListener listener = new TreeEventListener()
		{
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if ( event instanceof CommitEvent  ||  event instanceof TextEditEvent )
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
