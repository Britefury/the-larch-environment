//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.Corner;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.TextEditEventInsert;
import BritefuryJ.DocPresent.TextEditEventRemove;
import BritefuryJ.DocPresent.TextEditEventReplace;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.TextClipboardHandler;
import BritefuryJ.DocPresent.Interactor.KeyElementInteractor;
import BritefuryJ.DocPresent.Interactor.RealiseElementInteractor;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
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
import BritefuryJ.StyleSheet.StyleValues;

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
			
			
			public boolean keyPressed(DPElement element, KeyEvent event)
			{
				return event.getKeyCode() == KeyEvent.VK_ENTER  ||  event.getKeyCode() == KeyEvent.VK_ESCAPE;
			}
	
			public boolean keyReleased(DPElement element, KeyEvent event)
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
	
			public boolean keyTyped(DPElement element, KeyEvent event)
			{
				return event.getKeyChar() == KeyEvent.VK_ENTER  ||  event.getKeyChar() == KeyEvent.VK_ESCAPE;
			}
			
			
			public void elementRealised(DPElement element)
			{
				if ( bGrabCaretOnRealise )
				{
					grabCaret();
					bGrabCaretOnRealise = false;
				}

				if ( bSelectAllOnRealise )
				{
					selectAll();
					bSelectAllOnRealise = false;
				}
			}

			public void elementUnrealised(DPElement element)
			{
			}
		}
		
		
		private class TextEntryTreeEventListener implements TreeEventListener
		{
			public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEventInsert )
				{
					TextEditEventInsert insert = (TextEditEventInsert)event;
					if ( listener != null )
					{
						listener.onTextInserted( TextEntryControl.this, insert.getPosition(), insert.getTextInserted() );
					}
					validate( getDisplayedText() );
					return true;
				}
				else if ( event instanceof TextEditEventRemove )
				{
					TextEditEventRemove remove = (TextEditEventRemove)event;
					if ( listener != null )
					{
						listener.onTextRemoved( TextEntryControl.this, remove.getPosition(), remove.getLength() );
					}
					validate( getDisplayedText() );
					return true;
				}
				else if ( event instanceof TextEditEventReplace )
				{
					TextEditEventReplace replace = (TextEditEventReplace)event;
					if ( listener != null )
					{
						listener.onTextReplaced( TextEntryControl.this, replace.getPosition(), replace.getLength(), replace.getReplacement() );
					}
					validate( getDisplayedText() );
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
				textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
			}
	
			@Override
			protected void insertText(Marker marker, String text)
			{
				textElement.insertText( marker, text );
			}
			
			@Override
			protected void replaceText(TextSelection selection, Caret caret, String replacement)
			{
				textElement.replaceText( selection.getStartMarker(), selection.getEndMarker(), replacement );
			}
			
			@Override
			protected String getText(TextSelection selection)
			{
				return textElement.getRootElement().getTextRepresentationInSelection( selection );
			}
		}
		
		
		private DPBorder outerElement;
		private DPText textElement;
		private BritefuryJ.Graphics.AbstractBorder validBorder, invalidBorder;
		private TextEntryListener listener;
		private TextEntryValidator validator;
		private LiveInterface text;
		private boolean bGrabCaretOnRealise, bSelectAllOnRealise;
	
	
		
		protected TextEntryControl(PresentationContext ctx, StyleValues style, LiveInterface text, DPBorder outerElement, DPRegion region, DPText textElement, TextEntryListener listener, TextEntryValidator validator,
				BritefuryJ.Graphics.AbstractBorder validBorder, BritefuryJ.Graphics.AbstractBorder invalidBorder)
		{
			super( ctx, style );
			
			this.text = text;
			text.addListener( this );
			
			this.outerElement = outerElement;
			this.textElement = textElement;
			this.listener = listener;
			this.validator = validator;
			
			this.validBorder = validBorder;
			this.invalidBorder = invalidBorder;
	
			this.textElement.addElementInteractor( new TextEntryInteractor() );
			this.textElement.addTreeEventListener( new TextEntryTreeEventListener() );
			
			outerElement.setValueFunction( text.elementValueFunction() );
			
			region.setClipboardHandler( new TextEntryClipboardHandler() );
			
			requestRefresh();
		}
		
		
		public DPElement getElement()
		{
			return outerElement;
		}
		
	
		public String getTextValue()
		{
			return (String)text.getStaticValue();
		}
		
		public String getDisplayedText()
		{
			return textElement.getText();
		}
		
		public void setDisplayedText(String x)
		{
			textElement.setText( x );
			validate( x );
		}
		
		
		public void selectAll()
		{
			PresentationComponent.RootElement root = textElement.getRootElement();
			if ( root != null )
			{
				root.setSelection( new TextSelection( textElement, textElement.markerAtStart(), textElement.markerAtEnd() ) );
			}
			else
			{
				throw new RuntimeException( "Could not get root element - text element is not realised" );
			}
		}
		
		public void selectAllOnRealise()
		{
			bSelectAllOnRealise = true;
		}
		
		
		public void grabCaret()
		{
			textElement.grabCaret();
		}
		
		public void grabCaretOnRealise()
		{
			bGrabCaretOnRealise = true;
		}
		
		public void ungrabCaret()
		{
			textElement.ungrabCaret();
		}
	
		
		public void accept()
		{
			String t = getDisplayedText();
			if ( !validate( t ) )
			{
				String failMessage = getValidationMessage( t );
				if ( failMessage != null )
				{
					Tooltip tooltip = new Tooltip( failMessage, 5.0 );
					tooltip.popup( outerElement, Corner.TOP_LEFT, Corner.BOTTOM_LEFT, ctx, style );
				}
				return;
			}
			ungrabCaret();
			listener.onAccept( this, t );
		}
	
		public void cancel()
		{
			ungrabCaret();
			listener.onCancel( this );
		}
		
		
		private boolean validate(String t)
		{
			boolean bValid = validator == null || validator.validateText( this, t );
			outerElement.setBorder( bValid  ?  validBorder  :  invalidBorder );
			return bValid;
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
	
	public static TextEntry validated(String initialText, TextEntryListener listener, TextEntryValidator validator)
	{
		return new TextEntry( new LiveSourceValue( initialText ), listener, validator );
	}
	
	public static TextEntry regexValidated(String initialText, TextEntryListener listener, Pattern validatorRegex, String validationFailMessage)
	{
		return new TextEntry( new LiveSourceValue( initialText ), listener, new RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
	}
	
	
	public TextEntry(LiveInterface value, TextEntryListener listener)
	{
		this( new LiveSourceRef( value ), listener, null );
	}
	
	public static TextEntry validated(LiveInterface value, TextEntryListener listener, TextEntryValidator validator)
	{
		return new TextEntry( new LiveSourceRef( value ), listener, validator );
	}
	
	public static TextEntry regexValidated(LiveInterface value, TextEntryListener listener, Pattern validatorRegex, String validationFailMessage)
	{
		return new TextEntry( new LiveSourceRef( value ), listener, new RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
	}
	
	
	public TextEntry(LiveValue value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ), null );
	}
	
	public static TextEntry validated(LiveValue value, TextEntryValidator validator)
	{
		return new TextEntry( new LiveSourceRef( value ), new CommitListener( value ), validator );
	}
	
	public static TextEntry regexValidated(LiveValue value, Pattern validatorRegex, String validationFailMessage)
	{
		return new TextEntry( new LiveSourceRef( value ), new CommitListener( value ), new RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
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
		
		LiveInterface value = valueSource.getLive();
		DPText textElement = (DPText)new Text( "" ).present( ctx, style );
		Pres line = new Row( new Pres[] { new Segment( false, false, textElement ) } );
		Pres region = new Region( line );
		DPRegion regionElement = (DPRegion)region.present( ctx, style );
		Pres outer = new Border( regionElement ).alignVRefY();
		DPBorder outerElement = (DPBorder)outer.present( ctx, style );
		
		TextEntryControl control = new TextEntryControl( ctx, style, value, outerElement, regionElement, textElement, listener, validator, validBorder, invalidBorder );
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
