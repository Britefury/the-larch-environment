//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.TextEditEventInsert;
import BritefuryJ.DocPresent.TextEditEventRemove;
import BritefuryJ.DocPresent.TextEditEventReplace;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Clipboard.TextEditHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;

public class TextEntry extends Control
{
	public static class TextEntryListener
	{
		public void onAccept(TextEntry textEntry, String text)
		{
		}

		public void onCancel(TextEntry textEntry, String originalText)
		{
		}

		public void onTextInserted(TextEntry textEntry, int position, String textInserted)
		{
		}

		public void onTextRemoved(TextEntry textEntry, int position, int length)
		{
		}
		
		public void onTextReplaced(TextEntry textEntry, int position, int length, String replacementText)
		{
		}
	}
	
	
	public static abstract class TextEntryValidator
	{
		public abstract boolean validateText(TextEntry textEntry, String text);
		
		public String validationMessage(TextEntry textEntry, String text)
		{
			return null;
		}
	}
	
	
	private static class RegexTextEntryValidator extends TextEntryValidator
	{
		private Pattern pattern;
		private String failMessage;
		
		private RegexTextEntryValidator(Pattern pattern, String failMessage)
		{
			this.pattern = pattern;
			this.failMessage = failMessage;
		}
		
		
		@Override
		public boolean validateText(TextEntry textEntry, String text)
		{
			return pattern.matcher( text ).matches();
		}

		@Override
		public String validationMessage(TextEntry textEntry, String text)
		{
			return failMessage;
		}
	}
	

	private class TextEntryInteractor extends ElementInteractor
	{
		private TextEntryInteractor()
		{
		}
		
		
		public boolean onKeyPress(DPElement element, KeyEvent event)
		{
			return event.getKeyCode() == KeyEvent.VK_ENTER  ||  event.getKeyCode() == KeyEvent.VK_ESCAPE;
		}

		public boolean onKeyRelease(DPElement element, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ENTER )
			{
				accept();
				return true;
			}
			else if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				cancel();
				return true;
			}
			return false;
		}

		public boolean onKeyTyped(DPElement element, KeyEvent event)
		{
			return event.getKeyChar() == KeyEvent.VK_ENTER  ||  event.getKeyChar() == KeyEvent.VK_ESCAPE;
		}
	}
	
	
	private class TextEntryTreeEventListener implements TreeEventListener
	{
		@Override
		public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
		{
			if ( event instanceof TextEditEventInsert )
			{
				TextEditEventInsert insert = (TextEditEventInsert)event;
				listener.onTextInserted( TextEntry.this, insert.getPosition(), insert.getTextInserted() );
				return true;
			}
			else if ( event instanceof TextEditEventRemove )
			{
				TextEditEventRemove remove = (TextEditEventRemove)event;
				listener.onTextRemoved( TextEntry.this, remove.getPosition(), remove.getLength() );
				return true;
			}
			else if ( event instanceof TextEditEventReplace )
			{
				TextEditEventReplace replace = (TextEditEventReplace)event;
				listener.onTextReplaced( TextEntry.this, replace.getPosition(), replace.getLength(), replace.getReplacement() );
				return true;
			}
			return false;
		}
	}
	
	
	private class TextEntryEditHandler extends TextEditHandler
	{
		protected void deleteText(Selection selection)
		{
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		protected void insertText(Marker marker, String text)
		{
			textElement.insertText( marker, text );
		}
		
		protected void replaceText(Selection selection, String replacement)
		{
			textElement.replaceText(selection.getStartMarker(), selection.getEndMarker(), replacement );
		}
		
		protected String getText(Selection selection)
		{
			return textElement.getTextRepresentationBetweenMarkers( selection.getStartMarker(), selection.getEndMarker() );
		}

		@Override
		public boolean canShareSelectionWith(EditHandler editHandler)
		{
			return false;
		}
	}
	
	
	
	private DPBorder outerElement;
	private DPText textElement;
	private TextEntryListener listener;
	private TextEntryValidator validator;
	private ControlsStyleSheet styleSheet;
	private String originalText;


	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, listener, (TextEntryValidator)null, styleSheet );
	}
	
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, TextEntryValidator validator, ControlsStyleSheet styleSheet)
	{
		this.outerElement = outerElement;
		this.textElement = textElement;
		this.listener = listener;
		this.validator = validator;
		this.styleSheet = styleSheet;

		this.textElement.addInteractor( new TextEntryInteractor() );
		this.textElement.addTreeEventListener( new TextEntryTreeEventListener() );
		originalText = textElement.getText();
		frame.setEditHandler( new TextEntryEditHandler() );
	}
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, Pattern validationRegex, String validationFailMessage,
			ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, listener, new RegexTextEntryValidator( validationRegex, validationFailMessage ), styleSheet );
	}
	
	
	public DPElement getElement()
	{
		return outerElement;
	}
	

	public String getText()
	{
		return textElement.getText();
	}
	
	public String getOriginalText()
	{
		return originalText;
	}
	
	public void setText(String text)
	{
		textElement.setText( text );
	}
	
	
	public void selectAll()
	{
		PresentationComponent.RootElement root = textElement.getRootElement();
		if ( root != null )
		{
			Selection selection = root.getSelection();
			selection.setSelection( textElement.markerAtStart(), textElement.markerAtEnd() );
		}
		else
		{
			throw new RuntimeException( "Could not get root element - text element is not realised" );
		}
	}
	
	
	public void grabCaret()
	{
		textElement.grabCaret();
	}
	
	public void ungrabCaret()
	{
		textElement.ungrabCaret();
	}

	
	public void accept()
	{
		if ( validator != null  &&  !validator.validateText( this, getText() ) )
		{
			String failMessage = validator.validationMessage( this, getText() );
			if ( failMessage != null )
			{
				TimedPopup tooltip = styleSheet.tooltip( failMessage, 5.0 );
				tooltip.popupBelow( outerElement );
			}
			return;
		}
		ungrabCaret();
		listener.onAccept( this, getText() );
	}

	public void cancel()
	{
		ungrabCaret();
		listener.onCancel( this, originalText );
	}
}
