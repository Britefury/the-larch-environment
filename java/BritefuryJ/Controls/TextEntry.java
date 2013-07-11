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
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.LSRootElement;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.TextEditEventInsert;
import BritefuryJ.LSpace.TextEditEventRemove;
import BritefuryJ.LSpace.TextEditEventReplace;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Clipboard.TextClipboardHandler;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Interactor.RealiseElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
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
	/*
	Text entry listener
	Inherit and override methods
	 */
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
	

	/*
	Change listener; applies changes to a LiveValue
	 */
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
			if ( !textEntry.setTextInProgress )
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
	}
	
	

	/*
	Validator

	Override validateText; return true if text passes validation rules

	Optionally, override validationMessage; return message to display to user in tooltip to guide them
	 */
	public static abstract class TextEntryValidator
	{
		public abstract boolean validateText(TextEntryControl textEntry, String text);
		
		public String validationMessage(TextEntryControl textEntry, String text)
		{
			return null;
		}
	}

	/*
	Regular expression validator
	 */
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



	/*
	Abstract text entry control

	Defines basic behaviour of text entry control
	 */
	public abstract static class TextEntryControl extends Control implements IncrementalMonitorListener
	{
		private static class TextEntryRootPropertyKey
		{
			private static final TextEntryRootPropertyKey instance = new TextEntryRootPropertyKey();
		}

		/*
		Interactor

		Responds to presses of enter and escape keys

		Responds to a realise event by grabbing the caret or selecting the text if the grab or select flags are set
		 */
		protected class TextEntryInteractor implements KeyElementInteractor, RealiseElementInteractor
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
					return hasUncommittedChange();
				}
				return false;
			}


			public void elementRealised(LSElement element)
			{
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

			public void elementUnrealised(LSElement element)
			{
			}
		}



		/*
		Tree event listener

		Responds to edit events by performing validation and sending events to the text entry listener
		 */
		protected class TextEntryTreeEventListener implements TreeEventListener
		{
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEventInsert )
				{
					TextEditEventInsert insert = (TextEditEventInsert)event;
					notifyUncommittedChange();
					validate( getDisplayedText() );
					if ( listener != null )
					{
						int offset = sourceElement.getTextRepresentationOffsetInSubtree( outerElement );
						listener.onTextInserted( TextEntryControl.this, offset + insert.getPosition(), insert.getTextInserted() );
						listener.onTextChanged( TextEntryControl.this );
					}
					return true;
				}
				else if ( event instanceof TextEditEventRemove )
				{
					TextEditEventRemove remove = (TextEditEventRemove)event;
					notifyUncommittedChange();
					validate( getDisplayedText() );
					if ( listener != null )
					{
						int offset = sourceElement.getTextRepresentationOffsetInSubtree( outerElement );
						listener.onTextRemoved( TextEntryControl.this, offset + remove.getPosition(), remove.getLength() );
						listener.onTextChanged( TextEntryControl.this );
					}
					return true;
				}
				else if ( event instanceof TextEditEventReplace )
				{
					TextEditEventReplace replace = (TextEditEventReplace)event;
					notifyUncommittedChange();
					validate( getDisplayedText() );
					if ( listener != null )
					{
						int offset = sourceElement.getTextRepresentationOffsetInSubtree( outerElement );
						listener.onTextReplaced( TextEntryControl.this, offset + replace.getPosition(), replace.getLength(), replace.getReplacement() );
						listener.onTextChanged( TextEntryControl.this );
					}
					return true;
				}
				return false;
			}
		}





		protected LSBorder outerElement;
		protected TextEntryListener listener;
		protected TextEntryValidator validator;
		protected boolean grabCaretOnRealise, selectAllOnRealise, setTextInProgress, uncommittedChanges, textIsValid;
		private BritefuryJ.Graphics.AbstractBorder validBorder, invalidBorder, changedBorder;
		private LiveInterface text;


		protected TextEntryControl(PresentationContext ctx, StyleValues style, LiveInterface text, LSBorder outerElement, TextEntryListener listener, TextEntryValidator validator,
					   BritefuryJ.Graphics.AbstractBorder validBorder, BritefuryJ.Graphics.AbstractBorder invalidBorder, BritefuryJ.Graphics.AbstractBorder changedBorder)
		{
			super( ctx, style );

			this.text = text;
			text.addListener( this );


			this.outerElement = outerElement;

			this.listener = listener;
			this.validator = validator;

			this.validBorder = validBorder;
			this.invalidBorder = invalidBorder;
			this.changedBorder = changedBorder;

			requestRefresh();

			uncommittedChanges = false;
		}


		@Override
		public LSElement getElement()
		{
			return outerElement;
		}


		//
		// Value getters an setters
		//

		/*
		Get the text represented by the text entry.
		Acquires the value from the live value; does not return the text displayed; the live value and the displayed text may be out of sync
		 */
		public String getTextValue()
		{
			return (String)text.getStaticValue();
		}

		/*
		Get the displayed text
		 */
		public abstract String getDisplayedText();

		/*
		Override in subclasses
		Sets the text that is displayed
		 */
		protected abstract void setDisplayedTextContent(String x);

		/*
		Set the text displayed
		 */
		private void setDisplayedText(String x)
		{
			setTextInProgress = true;
			setDisplayedTextContent(x);
			setTextInProgress = false;
			validate( x );
		}



		/*
		Grab the caret
		 */
		public abstract void grabCaret();

		/*
		Ungrab the caret
		 */
		public abstract void ungrabCaret();

		/*
		Enable grab caret on realise
		 */
		public void grabCaretOnRealise()
		{
			grabCaretOnRealise = true;
		}



		/*
		Select all the text in the control
		 */
		public abstract void selectAll();

		/*
		Enable selecting all text in the control
		 */
		public void selectAllOnRealise()
		{
			selectAllOnRealise = true;
		}




		/*
		Determine if there are uncommitted changes (if the displayed text and the text contained in the live value are out of sync)
		 */
		public boolean hasUncommittedChange()
		{
			return uncommittedChanges;
		}


		/*
		Change notification
		 */
		private void notifyUncommittedChange()
		{
			if (!uncommittedChanges)
			{
				uncommittedChanges = true;
				updateBorder();
			}
		}



		/*
		Determine if displayed text is valid (passes validation rules)
		 */
		public boolean isDisplayedTextValid()
		{
			return textIsValid;
		}

		/*
		Get validation message for text @t
		 */
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

		/*
		Validate the supplied text and set the textIsValid flag accordingly
		 */
		private void validate(String t)
		{
			textIsValid = validator == null || validator.validateText( this, t );
			updateBorder();
		}

		/*
		Accept the dusplayed text
		 */
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
			uncommittedChanges = false;
			updateBorder();
			listener.onAccept( this, t );
		}

		/*
		Cancel
		 */
		public boolean cancel()
		{
			ungrabCaret();
			if (hasUncommittedChange())
			{
				listener.onCancel( this );
				return true;
			}
			return false;
		}


		/*
		Update the border, which informs the user of the state of the text entry
		 */
		private void updateBorder()
		{
			AbstractBorder c = changedBorder != null  ?  changedBorder  :  validBorder;
			AbstractBorder b = uncommittedChanges ?  c  :  validBorder;
			outerElement.setBorder( textIsValid  ?  b  :  invalidBorder );
		}




		protected LSElement getTextEntryRootElement(LSElement element)
		{
			LSElement.PropertyValue value = element.findPropertyInAncestors(TextEntryRootPropertyKey.instance);
			return value != null  ?  value.getElement()  :  null;
		}


		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			requestRefresh();
		}

		protected void requestRefresh()
		{
			Runnable refresh = new Runnable()
			{
				@Override
				public void run()
				{
					String t = (String)text.getValue();
					t = t != null  ?  t  :  "";
					setDisplayedText( t );
					uncommittedChanges = false;
					updateBorder();
				}
			};
			outerElement.queueImmediateEvent( refresh );
		}
	}

	
	public static class TextEntryControlText extends TextEntryControl
	{
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
		
		
		private LSText textElement;

	
		
		protected TextEntryControlText(PresentationContext ctx, StyleValues style, LiveInterface text,
					       LSBorder outerElement, LSRegion region, LSText textElement,
					       TextEntryListener listener, TextEntryValidator validator,
					       BritefuryJ.Graphics.AbstractBorder validBorder, BritefuryJ.Graphics.AbstractBorder invalidBorder, BritefuryJ.Graphics.AbstractBorder changedBorder)
		{
			super( ctx, style, text, outerElement, listener, validator, validBorder, invalidBorder, changedBorder );

			this.textElement = textElement;

			this.textElement.addElementInteractor( new TextEntryInteractor() );
			this.textElement.addTreeEventListener( new TextEntryTreeEventListener() );
			
			outerElement.setValueFunction( text.elementValueFunction() );
			
			region.setClipboardHandler( new TextEntryClipboardHandler() );
			
		}
		
		
		@Override
		public String getDisplayedText()
		{
			return textElement.getText();
		}

		@Override
		protected void setDisplayedTextContent(String x)
		{
			textElement.setText(x);
		}
		


		@Override
		public void grabCaret()
		{
			textElement.grabCaret();
		}

		@Override
		public void ungrabCaret()
		{
			textElement.ungrabCaret();
		}




		public void selectAll()
		{
			LSRootElement root = textElement.getRootElement();
			if ( root != null )
			{
				root.setSelection( new TextSelection( textElement, Marker.atStartOfLeaf( textElement ), Marker.atEndOfLeaf( textElement ) ) );
			}
			else
			{
				throw new RuntimeException( "Could not get root element - text element is not realised" );
			}
		}
	}


	public static class TextEntryControlParagraph extends TextEntryControl
	{
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


		private LSText textElement;



		protected TextEntryControlParagraph(PresentationContext ctx, StyleValues style, LiveInterface text,
					       LSBorder outerElement, LSRegion region, LSText textElement,
					       TextEntryListener listener, TextEntryValidator validator,
					       BritefuryJ.Graphics.AbstractBorder validBorder, BritefuryJ.Graphics.AbstractBorder invalidBorder, BritefuryJ.Graphics.AbstractBorder changedBorder)
		{
			super( ctx, style, text, outerElement, listener, validator, validBorder, invalidBorder, changedBorder );

			this.textElement = textElement;

			this.textElement.addElementInteractor( new TextEntryInteractor() );
			this.textElement.addTreeEventListener( new TextEntryTreeEventListener() );

			outerElement.setValueFunction( text.elementValueFunction() );

			region.setClipboardHandler( new TextEntryClipboardHandler() );

		}


		@Override
		public String getDisplayedText()
		{
			return textElement.getText();
		}

		@Override
		protected void setDisplayedTextContent(String x)
		{
			textElement.setText(x);
		}



		@Override
		public void grabCaret()
		{
			textElement.grabCaret();
		}

		@Override
		public void ungrabCaret()
		{
			textElement.ungrabCaret();
		}




		public void selectAll()
		{
			LSRootElement root = textElement.getRootElement();
			if ( root != null )
			{
				root.setSelection( new TextSelection( textElement, Marker.atStartOfLeaf( textElement ), Marker.atEndOfLeaf( textElement ) ) );
			}
			else
			{
				throw new RuntimeException( "Could not get root element - text element is not realised" );
			}
		}
	}


	private static class CommitListener extends TextEntryListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		public void onAccept(TextEntryControlText textEntry, String text)
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
		
		LiveInterface value = valueSource.getLive();
		LSText textElement = (LSText)new Text( "" ).present( ctx, style );
		Pres line = new Row( new Pres[] { new Segment( false, false, textElement ) } );
		Pres region = new Region( line );
		LSRegion regionElement = (LSRegion)region.present( ctx, style );
		Pres outer = new Border( regionElement ).alignVRefY();
		LSBorder outerElement = (LSBorder)outer.present( ctx, style );
		
		TextEntryControlText control = new TextEntryControlText( ctx, style, value, outerElement, regionElement, textElement, listener, validator,
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
