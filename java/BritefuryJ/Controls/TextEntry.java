//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.ElementValueFunction;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.TextEditEventInsert;
import BritefuryJ.DocPresent.TextEditEventRemove;
import BritefuryJ.DocPresent.TextEditEventReplace;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Clipboard.TextEditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Region;
import BritefuryJ.DocPresent.Combinators.Primitive.Segment;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class TextEntry extends ControlPres
{
	public static class TextEntryListener
	{
		public void onAccept(TextEntryControl textEntry, String text)
		{
		}

		public void onCancel(TextEntryControl textEntry, String originalText)
		{
		}

		public void onTextInserted(TextEntryControl textEntry, int position, String textInserted)
		{
		}

		public void onTextRemoved(TextEntryControl textEntry, int position, int length)
		{
		}
		
		public void onTextReplaced(TextEntryControl textEntry, int position, int length, String replacementText)
		{
		}
	}
	
	
	public static abstract class TextEntryValidator
	{
		public abstract boolean validateText(TextEntryControl textEntry, String text);
		
		public String validationMessage(TextEntryControl textEntry, String text)
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
		public boolean validateText(TextEntryControl textEntry, String text)
		{
			return pattern.matcher( text ).matches();
		}

		@Override
		public String validationMessage(TextEntryControl textEntry, String text)
		{
			return failMessage;
		}
	}
	

	
	public static class TextEntryControl extends Control
	{
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
					listener.onTextInserted( TextEntryControl.this, insert.getPosition(), insert.getTextInserted() );
					validate( getText() );
					return true;
				}
				else if ( event instanceof TextEditEventRemove )
				{
					TextEditEventRemove remove = (TextEditEventRemove)event;
					listener.onTextRemoved( TextEntryControl.this, remove.getPosition(), remove.getLength() );
					validate( getText() );
					return true;
				}
				else if ( event instanceof TextEditEventReplace )
				{
					TextEditEventReplace replace = (TextEditEventReplace)event;
					listener.onTextReplaced( TextEntryControl.this, replace.getPosition(), replace.getLength(), replace.getReplacement() );
					validate( getText() );
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
		
		
		private class ValueFn implements ElementValueFunction
		{
			public Object computeElementValue(DPElement element)
			{
				return getText();
			}
	
			public void addStreamValuePrefixToStream(StreamValueBuilder builder, DPElement element)
			{
			}
	
			public void addStreamValueSuffixToStream(StreamValueBuilder builder, DPElement element)
			{
			}
		}
		
		
		
		private DPBorder outerElement;
		private DPText textElement;
		private BritefuryJ.DocPresent.Border.AbstractBorder validBorder, invalidBorder;
		private TextEntryListener listener;
		private TextEntryValidator validator;
		private String originalText;
	
	
		
		protected TextEntryControl(PresentationContext ctx, StyleValues style, DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, TextEntryValidator validator,
				BritefuryJ.DocPresent.Border.AbstractBorder validBorder, BritefuryJ.DocPresent.Border.AbstractBorder invalidBorder)
		{
			super( ctx, style );
			
			this.outerElement = outerElement;
			this.textElement = textElement;
			this.listener = listener;
			this.validator = validator;
			
			this.validBorder = validBorder;
			this.invalidBorder = invalidBorder;
	
			this.textElement.addInteractor( new TextEntryInteractor() );
			this.textElement.addTreeEventListener( new TextEntryTreeEventListener() );
			originalText = textElement.getText();
			
			outerElement.setValueFunction( new ValueFn() );
			
			frame.setEditHandler( new TextEntryEditHandler() );
			
			validate( originalText );
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
					Tooltip tooltip = new Tooltip( failMessage, 5.0 );
					tooltip.popupBelow( outerElement, ctx, style );
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
		
		
		private void validate(String text)
		{
			boolean bValid = validator != null  ?  validator.validateText( this, text )  :  true;
			outerElement.setBorder( bValid  ?  validBorder  :  invalidBorder );
		}
	}
	
	
	private String initialText;
	private TextEntryListener listener;
	private TextEntry.TextEntryValidator validator;
	
	
	public TextEntry(String initialText, TextEntryListener listener)
	{
		this( initialText, listener, null );
	}
	
	public TextEntry(String initialText, TextEntryListener listener, TextEntryValidator validator)
	{
		this.initialText = initialText;
		this.listener = listener;
		this.validator = validator;
	}
	
	public TextEntry(String initialText, TextEntryListener listener, Pattern validatorRegex, String validationFailMessage)
	{
		this( initialText, listener, new RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		BritefuryJ.DocPresent.Border.AbstractBorder validBorder = style.get( Controls.textEntryBorder, BritefuryJ.DocPresent.Border.AbstractBorder.class ); 
		BritefuryJ.DocPresent.Border.AbstractBorder invalidBorder = style.get( Controls.textEntryInvalidBorder, BritefuryJ.DocPresent.Border.AbstractBorder.class );
		
		DPText textElement = (DPText)StyleSheet2.instance.withAttr( Primitive.editable, true ).applyTo( new Text( initialText ) ).present( ctx, style );
		Pres line = new HBox( new Pres[] { new Segment( false, false, textElement ) } );
		Pres region = new Region( line );
		DPRegion regionElement = (DPRegion)region.present( ctx, style );
		Pres outer = new Border( regionElement );
		DPBorder outerElement = (DPBorder)outer.present( ctx, style );
		
		return new TextEntryControl( ctx, style, outerElement, regionElement, textElement, listener, validator, validBorder, invalidBorder );
	}
}