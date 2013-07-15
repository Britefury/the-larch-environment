//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Clipboard.TextClipboardHandler;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Interactor.RealiseElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleValues;

import javax.swing.*;

public class TextEntry extends ControlPres
{
	public static class TextEntryListener
	{
		public void onAccept(TextEntryControl textEntry, String text)
		{
		}

		public void onCancel(TextEntryControl textEntry)
		{
		}

		public void onTextChanged(TextEntryControl textEntry)
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
	
	
	private static class ChangeListener extends TextEntryListener
	{
		private LiveValue value;
		
		public ChangeListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onTextChanged(TextEntryControl textEntry)
		{
			if ( textEntry.isDisplayedTextValid() )
			{
				String text = textEntry.getDisplayedText();
				if ( !text.equals( value.getStaticValue() ) )
				{
					value.setLiteralValue( text );
				}
			}
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
	
	public static class RegexTextEntryValidator extends TextEntryValidator
	{
		private Pattern pattern;
		private String failMessage;
		
		public RegexTextEntryValidator(Pattern pattern, String failMessage)
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
	

	
	public static class TextEntryControl extends Control implements IncrementalMonitorListener
	{
		private class TextEntryInteractor implements KeyElementInteractor, RealiseElementInteractor
		{
			private TextEntryInteractor()
			{
			}
			
			
			public boolean keyPressed(LSElement element, KeyEvent event)
			{
				return event.getKeyCode() == KeyEvent.VK_ENTER  ||  event.getKeyCode() == KeyEvent.VK_ESCAPE;
			}
	
			public boolean keyReleased(LSElement element, KeyEvent event)
			{
				if ( event.getKeyCode() == KeyEvent.VK_ENTER )
				{
					accept();
					return true;
				}
				else if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
				{
					return cancel();
				}
				return false;
			}
	
			public boolean keyTyped(LSElement element, KeyEvent event)
			{
				if ( event.getKeyChar() == KeyEvent.VK_ENTER)
				{
					return true;
				}
				else if (event.getKeyChar() == KeyEvent.VK_ESCAPE)
				{
					return hasUncommittedChanges();
				}
				return false;
			}
			
			
			public void elementRealised(LSElement element)
			{
				Runnable realiseTask = new Runnable() {
					public void run() {
						if ( grabCaretOnRealise )
						{
							grabCaret();
							grabCaretOnRealise = false;
						}

						if ( selectAllOnRealise )
						{
							selectAll();
							selectAllOnRealise = false;
						}
					}
				};

				if (grabCaretOnRealise || selectAllOnRealise) {
					SwingUtilities.invokeLater(realiseTask);
				}
			}

			public void elementUnrealised(LSElement element)
			{
			}
		}
		
		
		private class TextEntryTreeEventListener implements TreeEventListener
		{
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if (event instanceof TextEditEvent) {
					String text = outerElement.getTextRepresentation();
					displayedText.setLiteralValue(text);

					validate( text );

					if (listener != null) {
						int offset = sourceElement.getTextRepresentationOffsetInSubtree( outerElement );
						if ( event instanceof TextEditEventInsert )
						{
							TextEditEventInsert insert = (TextEditEventInsert)event;
							listener.onTextInserted(TextEntryControl.this, offset + insert.getPosition(), insert.getTextInserted());
						}
						else if ( event instanceof TextEditEventRemove )
						{
							TextEditEventRemove remove = (TextEditEventRemove)event;
							listener.onTextRemoved(TextEntryControl.this, offset + remove.getPosition(), remove.getLength());
						}
						else if ( event instanceof TextEditEventReplace )
						{
							TextEditEventReplace replace = (TextEditEventReplace)event;
							listener.onTextReplaced(TextEntryControl.this, offset + replace.getPosition(), replace.getLength(), replace.getReplacement());
						}
						listener.onTextChanged( TextEntryControl.this );
					}
					return true;
				}
				return false;
			}
		}
		
		
		private class TextEntryClipboardHandler extends TextClipboardHandler
		{
			@Override
			protected void deleteText(TextSelection selection, Caret caret)
			{
				int startPosition = selection.getStartMarker().getClampedIndexInSubtree( outerElement );
				int endPosition = selection.getEndMarker().getClampedIndexInSubtree( outerElement );

				String newText = outerElement.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  outerElement.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
				displayedText.setLiteralValue(newText);

				if ( listener != null )
				{
					listener.onTextRemoved( TextEntryControl.this, startPosition, endPosition - startPosition );
					listener.onTextChanged( TextEntryControl.this );
				}
			}
	
			@Override
			protected void insertText(Marker marker, String text)
			{
				marker.getElement().insertText(marker, text);

				// Don't inform the listener - the text edit event will take care of that
			}
			
			@Override
			protected void replaceText(TextSelection selection, Caret caret, String replacement)
			{
				int startPosition = selection.getStartMarker().getClampedIndexInSubtree( outerElement );
				int endPosition = selection.getEndMarker().getClampedIndexInSubtree( outerElement );

				String newText = outerElement.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  replacement  +  outerElement.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
				displayedText.setLiteralValue(newText);

				if ( listener != null )
				{
					listener.onTextReplaced( TextEntryControl.this, startPosition, endPosition - startPosition, replacement );
					listener.onTextChanged( TextEntryControl.this );
				}
			}
			
			@Override
			protected String getText(TextSelection selection)
			{
				return outerElement.getRootElement().getTextRepresentationInSelection( selection );
			}
		}
		
		
		private LSBorder outerElement;
		private LiveInterface text;
		private LiveValue displayedText;
		private BritefuryJ.Graphics.AbstractBorder validBorder, invalidBorder, changedBorder;
		private TextEntryListener listener;
		private TextEntryValidator validator;
		private boolean grabCaretOnRealise, selectAllOnRealise, textIsValid;
	
	
		
		protected TextEntryControl(PresentationContext ctx, StyleValues style, LiveInterface text, LiveValue displayedText,
				LSBorder outerElement, LSRegion region,
				TextEntryListener listener, TextEntryValidator validator,
				BritefuryJ.Graphics.AbstractBorder validBorder, BritefuryJ.Graphics.AbstractBorder invalidBorder, BritefuryJ.Graphics.AbstractBorder changedBorder)
		{
			super( ctx, style );
			
			this.text = text;
			this.displayedText = displayedText;
			text.addListener( this );
			
			this.outerElement = outerElement;
			this.listener = listener;
			this.validator = validator;
			
			this.validBorder = validBorder;
			this.invalidBorder = invalidBorder;
			this.changedBorder = changedBorder;
	
			this.outerElement.addElementInteractor( new TextEntryInteractor() );
			this.outerElement.addTreeEventListener( new TextEntryTreeEventListener() );
			
			outerElement.setValueFunction( text.elementValueFunction() );
			
			region.setClipboardHandler( new TextEntryClipboardHandler() );
			
			requestRefresh();
		}
		
		
		public LSElement getElement()
		{
			return outerElement;
		}
		
	
		public String getTextValue()
		{
			return (String)text.getStaticValue();
		}
		
		public String getDisplayedText()
		{
			return (String)displayedText.getStaticValue();
		}
		
		private void setDisplayedText(String x)
		{
			displayedText.setLiteralValue(x);
			validate( x );
		}
		
		
		public boolean isDisplayedTextValid()
		{
			return textIsValid;
		}
		
		
		public void selectAll()
		{
			LSRootElement root = outerElement.getRootElement();
			if ( root != null )
			{
				Marker startMarker = Marker.atStartOf(outerElement, true);
				Marker endMarker = Marker.atEndOf(outerElement, true);
				if (startMarker != null  &&  startMarker.isValid()  &&  endMarker != null  &&  endMarker.isValid())
				{
					root.setSelection( new TextSelection( startMarker.getElement(), startMarker, endMarker ) );
				}
				else
				{
					root.setSelection(null);
				}
			}
			else
			{
				throw new RuntimeException( "Could not get root element - text element is not realised" );
			}
		}
		
		public void selectAllOnRealise()
		{
			selectAllOnRealise = true;
		}
		
		
		public void grabCaret()
		{
			outerElement.grabCaret();
		}
		
		public void grabCaretOnRealise()
		{
			grabCaretOnRealise = true;
		}
		
		public void ungrabCaret()
		{
			outerElement.ungrabCaret();
		}
	
		
		public void accept()
		{
			String t = getDisplayedText();
			validate( t );
			if ( !textIsValid )
			{
				String failMessage = getValidationMessage( t );
				if ( failMessage != null )
				{
					TimedTip tooltip = new TimedTip( failMessage, 5.0 );
					tooltip.popup( outerElement, Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT, ctx, style );
				}
				return;
			}
			
			ungrabCaret();
			updateBorder();
			listener.onAccept( this, t );
		}
	
		public boolean cancel()
		{
			ungrabCaret();
			if (hasUncommittedChanges())
			{
				String current = (String)text.getStaticValue();
				displayedText.setLiteralValue(current != null  ?  current  :  "");
				listener.onCancel( this );
				updateBorder();
				return true;
			}
			return false;
		}


		public boolean hasUncommittedChanges()
		{
			String current = (String)text.getStaticValue();
			current = current != null  ?  current  :  "";
			return !current.equals(displayedText.getStaticValue());
		}


		private void validate(String t)
		{
			textIsValid = validator == null || validator.validateText( this, t );
			updateBorder();
		}
		
		private String getValidationMessage(String t)
		{
			if ( validator != null )
			{
				return validator.validationMessage( this, t );
			}
			else
			{
				return null;
			}
		}
		
		
		private void updateBorder()
		{
			AbstractBorder c = changedBorder != null  ?  changedBorder  :  validBorder;
			AbstractBorder b = hasUncommittedChanges() ?  c  :  validBorder;
			outerElement.setBorder( textIsValid  ?  b  :  invalidBorder );
		}


		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			requestRefresh();
		}
		
		private void requestRefresh()
		{
			Runnable refresh = new Runnable()
			{
				@Override
				public void run()
				{
					String t = (String)text.getValue();
					t = t != null  ?  t  :  "";
					setDisplayedText( t );
					updateBorder();
				}
			};
			outerElement.queueImmediateEvent( refresh );
		}
	}
	
	
	private static class CommitListener extends TextEntryListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		public void onAccept(TextEntryControl textEntry, String text)
		{
			value.setLiteralValue( text );
		}
	}
	
	
	private LiveSource valueSource;
	private TextEntryListener listener;
	private TextEntry.TextEntryValidator validator;
	private boolean bGrabCaretOnRealise, bSelectAllOnRealise;
	
	
	private TextEntry(LiveSource valueSource, TextEntryListener listener, TextEntryValidator validator)
	{
		this.valueSource = valueSource;
		this.listener = listener;
		this.validator = validator;
	}
	
	
	
	public TextEntry(String initialText, TextEntryListener listener)
	{
		this( new LiveSourceValue( initialText ), listener, null );
	}
	
	public TextEntry(LiveInterface value, TextEntryListener listener)
	{
		this( new LiveSourceRef( value ), listener, null );
	}
	
	public TextEntry(LiveValue value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ), null );
	}
	
	
	public static TextEntry textEntryCommitOnChange(LiveValue value)
	{
		return new TextEntry( new LiveSourceRef( value ), new ChangeListener( value ), null );
	}
	
	
	
	public TextEntry validated(TextEntryValidator v)
	{
		return new TextEntry( valueSource, listener, v );
	}
	
	public TextEntry regexValidated(Pattern validatorRegex, String validationFailMessage)
	{
		return validated( new RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
	}
	
	
	public void selectAllOnRealise()
	{
		bSelectAllOnRealise = true;
	}
	
	public void grabCaretOnRealise()
	{
		bGrabCaretOnRealise = true;
	}

	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		style = style.withAttr( Primitive.editable, true );

		BritefuryJ.Graphics.AbstractBorder validBorder = style.get( Controls.textEntryBorder, BritefuryJ.Graphics.AbstractBorder.class ); 
		BritefuryJ.Graphics.AbstractBorder invalidBorder = style.get( Controls.textEntryInvalidBorder, BritefuryJ.Graphics.AbstractBorder.class );
		BritefuryJ.Graphics.AbstractBorder changedBorder = style.get( Controls.textEntryChangedBorder, BritefuryJ.Graphics.AbstractBorder.class );
		boolean wordWrap = style.get( Controls.textEntryWordWrap, Boolean.class );

		LiveInterface value = valueSource.getLive();

		String current = (String)value.getStaticValue();
		current = current != null  ?  current  :  "";
		final LiveValue displayedText = new LiveValue(current);

		LiveFunction.Function function = null;

		if (wordWrap) {
			function = new LiveFunction.Function() {
				public Object evaluate() {
					return new NormalText((String)displayedText.getValue());
				}
			};
		}
		else {
			function = new LiveFunction.Function() {
				public Object evaluate() {
					return new Text((String)displayedText.getValue());
				}
			};
		}

		LiveFunction visual = new LiveFunction(function);

		Pres line = new Row( new Pres[] { new Segment( false, false, true, visual ) } );
		Pres region = new Region( line );
		LSRegion regionElement = (LSRegion)region.present( ctx, style );
		Pres outer = new Border( regionElement ).alignVRefY();
		LSBorder outerElement = (LSBorder)outer.present( ctx, style );
		
		TextEntryControl control = new TextEntryControl( ctx, style, value, displayedText, outerElement, regionElement, listener, validator,
				validBorder, invalidBorder, changedBorder );
		if ( bSelectAllOnRealise )
		{
			control.selectAllOnRealise();
		}
		if ( bGrabCaretOnRealise )
		{
			control.grabCaretOnRealise();
		}
		return control;
	}
}
