//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSColumn;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.LSWhitespace;
import BritefuryJ.LSpace.ElementValueFunction;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TextEditEventInsert;
import BritefuryJ.LSpace.TextEditEventRemove;
import BritefuryJ.LSpace.TextEditEventReplace;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Clipboard.TextClipboardHandler;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StreamValue.StreamValueBuilder;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.Primitive.Whitespace;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class TextArea extends ControlPres
{
	public static class TextAreaListener
	{
		public void onAccept(TextAreaControl textArea, String text)
		{
		}

		public void onTextInserted(TextAreaControl textArea, int position, String textInserted)
		{
		}

		public void onTextRemoved(TextAreaControl textArea, int position, int length)
		{
		}
		
		public void onTextReplaced(TextAreaControl textArea, int position, int length, String replacementText)
		{
		}
	}
	
	
	public static class TextAreaControl extends Control implements IncrementalMonitorListener
	{
		private class TextAreaInteractor implements KeyElementInteractor
		{
			private TextAreaInteractor()
			{
			}
			
			
			@Override
			public boolean keyPressed(LSElement element, KeyEvent event)
			{
				return event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER;
			}
	
			@Override
			public boolean keyReleased(LSElement element, KeyEvent event)
			{
				if ( event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER )
				{
					accept();
					return true;
				}
				return false;
			}
	
			@Override
			public boolean keyTyped(LSElement element, KeyEvent event)
			{
				return event.isControlDown()  &&  event.getKeyChar() == KeyEvent.VK_ENTER;
			}
		}
		
		
		private class TextAreaTextLineTreeEventListener implements TreeEventListener
		{
			@Override
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					LSText textElement = (LSText)sourceElement;
					String lineText = textElement.getText();
					int lineOffset = textElement.getTextRepresentationOffsetInSubtree( textBox );
					
					if ( lineText.contains( "\n" ) )
					{
						recomputeText();
					}
	
					if ( listener != null )
					{
						if ( event instanceof TextEditEventInsert )
						{
							TextEditEventInsert insert = (TextEditEventInsert)event;
							listener.onTextInserted( TextAreaControl.this, lineOffset + insert.getPosition(), insert.getTextInserted() );
						}
						else if ( event instanceof TextEditEventRemove )
						{
							TextEditEventRemove remove = (TextEditEventRemove)event;
							listener.onTextRemoved( TextAreaControl.this, lineOffset + remove.getPosition(), remove.getLength() );
						}
						else if ( event instanceof TextEditEventReplace )
						{
							TextEditEventReplace replace = (TextEditEventReplace)event;
							listener.onTextReplaced( TextAreaControl.this, lineOffset + replace.getPosition(), replace.getLength(), replace.getReplacement() );
						}
					}
					
					return true;
				}
				return false;
			}
		}
	
		private class TextAreaNewlineTreeEventListener implements TreeEventListener
		{
			@Override
			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					LSWhitespace whitespaceElement = (LSWhitespace)sourceElement;
					
					int lineOffset = whitespaceElement.getTextRepresentationOffsetInSubtree( textBox );
					
					if ( !whitespaceElement.getTextRepresentation().equals( "\n" ) )
					{
						recomputeText();
					}
	
					if ( listener != null )
					{
						if ( event instanceof TextEditEventInsert )
						{
							TextEditEventInsert insert = (TextEditEventInsert)event;
							listener.onTextInserted( TextAreaControl.this, lineOffset + insert.getPosition(), insert.getTextInserted() );
						}
						else if ( event instanceof TextEditEventRemove )
						{
							TextEditEventRemove remove = (TextEditEventRemove)event;
							listener.onTextRemoved( TextAreaControl.this, lineOffset + remove.getPosition(), remove.getLength() );
						}
						else if ( event instanceof TextEditEventReplace )
						{
							TextEditEventReplace replace = (TextEditEventReplace)event;
							listener.onTextReplaced( TextAreaControl.this, lineOffset + replace.getPosition(), replace.getLength(), replace.getReplacement() );
						}
					}
	
					return true;
				}
				return false;
			}
		}
		
		
		private class TextAreaClipboardHandler extends TextClipboardHandler
		{
			@Override
			protected void deleteText(TextSelection selection, Caret caret)
			{
				int startPosition = selection.getStartMarker().getClampedIndexInSubtree( textBox );
				int endPosition = selection.getEndMarker().getClampedIndexInSubtree( textBox );
	
				int caretPos = computeNewCaretPositionAfterRemove( getCaretIndex(), startPosition, endPosition - startPosition );
				String newText = textBox.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  textBox.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
				changeText( newText, caretPos );
				
				if ( listener != null )
				{
					listener.onTextRemoved( TextAreaControl.this, startPosition, endPosition - startPosition );
				}
			}
			
			@Override
			protected void insertText(Marker marker, String text)
			{
				marker.getElement().insertText( marker, text );
				
				// Don't inform the listener - the text edit event will take care of that
			}
			
			@Override
			protected void replaceText(TextSelection selection, Caret caret, String replacement)
			{
				int startPosition = selection.getStartMarker().getClampedIndexInSubtree( textBox );
				int endPosition = selection.getEndMarker().getClampedIndexInSubtree( textBox );
	
				int caretPos = computeNewCaretPositionAfterReplace( getCaretIndex(), startPosition, endPosition - startPosition, replacement.length() );
				String newText = textBox.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  replacement  +  textBox.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
				changeText( newText, caretPos );
				
				if ( listener != null )
				{
					listener.onTextReplaced( TextAreaControl.this, startPosition, endPosition - startPosition, replacement );
				}
			}
			
			@Override
			protected String getText(TextSelection selection)
			{
				return textBox.getRootElement().getTextRepresentationInSelection( selection );
			}
		}
		
		
		private class ValueFn implements ElementValueFunction
		{
			public Object computeElementValue(LSElement element)
			{
				return getDisplayedText();
			}
	
			public void addStreamValuePrefixToStream(StreamValueBuilder builder, LSElement element)
			{
			}
	
			public void addStreamValueSuffixToStream(StreamValueBuilder builder, LSElement element)
			{
			}
		}
	
		
		
		private LSElement element;
		private LSColumn textBox;
		private LiveInterface value;
		private TextAreaListener listener;
		private TextAreaTextLineTreeEventListener textLineTreeEventListener = new TextAreaTextLineTreeEventListener();
		private TextAreaNewlineTreeEventListener newlineTreeEventListener = new TextAreaNewlineTreeEventListener();
		
		
		
		protected TextAreaControl(PresentationContext ctx, StyleValues style, LSElement element, LSRegion region, LSColumn textBox, TextAreaListener listener, LiveInterface value)
		{
			super( ctx, style );
			
			this.value = value;
			this.value.addListener( this );
			
			this.element = element;
			this.textBox = textBox;
			this.listener = listener;
		
			element.setValueFunction( new ValueFn() );
			
			this.textBox.addElementInteractor( new TextAreaInteractor() );
			region.setClipboardHandler( new TextAreaClipboardHandler() );
			
			queueRebuild();
		}
		
	
		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		public String getDisplayedText()
		{
			return textBox.getTextRepresentation();
		}
		
		public void setDisplayedText(String text)
		{
			changeText( text, getCaretIndex() );
		}
		
		
		
		public void grabCaret()
		{
			textBox.grabCaret();
		}
		
		public void ungrabCaret()
		{
			textBox.ungrabCaret();
		}
	
		
		public void accept()
		{
			ungrabCaret();
			if ( listener != null )
			{
				listener.onAccept( this, getDisplayedText() );
			}
		}
	
	
	
		private int computeNewCaretPositionAfterInsert(int caretPos, int insertPos, int insertLength)
		{
			if ( caretPos >= insertPos )
			{
				return caretPos + insertLength;
			}
			else
			{
				return caretPos;
			}
		}
		
		private int computeNewCaretPositionAfterRemove(int caretPos, int removePos, int removeLength)
		{
			if ( caretPos >= removePos )
			{
				int end = removePos + removeLength;
				if ( caretPos >= end )
				{
					return caretPos - removeLength;
				}
				else
				{
					return removePos;
				}
			}
			else
			{
				return caretPos;
			}
		}
		
		private int computeNewCaretPositionAfterReplace(int caretPos, int replacePos, int replaceLength, int replacementLength)
		{
			if ( replacementLength > replaceLength )
			{
				return computeNewCaretPositionAfterInsert( caretPos, replacePos + replaceLength, replacementLength - replaceLength );
			}
			else if ( replacementLength < replaceLength )
			{
				return computeNewCaretPositionAfterRemove( caretPos, replacePos + replacementLength, replaceLength - replacementLength );
			}
			else
			{
				return caretPos;
			}
		}
		
		
		
		
		private int getCaretIndex()
		{
			try
			{
				return textBox.getRootElement().getCaret().getClampedIndexInSubtree( textBox );
			}
			catch (LSElement.IsNotInSubtreeException e)
			{
				return -1;
			}
		}
		
		private void recomputeText()
		{
			changeText( textBox.getTextRepresentation(), getCaretIndex() );
		}
		
		private void changeText(String text, int caretPos)
		{
			String textLines[];
			if ( text.endsWith( "\n\n" ) )
			{
				// This handles the behaviour of String#split, which will not add a final blank line where the text ends with two new line characters.
				// To correct for this, add a space character, then remove that final artificial line afterwards
				text = text + " ";
				String lines[] = text.split( "\\r?\\n" );
				textLines = new String[lines.length-1];
				System.arraycopy( lines, 0, textLines, 0, lines.length - 1 );
			}
			else
			{
				textLines = text.split( "\\r?\\n" );
			}
			
			
			ArrayList<LSElement> lineElements = new ArrayList<LSElement>();
			for (String line: textLines)
			{
				Pres textPres = new Text( line );
				LSElement textElement = textPres.present( ctx, style );
				Pres seg = new Segment( true, true, textElement );
				Pres newline = new Whitespace( "\n" );
				LSElement newlineElement = newline.present( ctx, style );
				textElement.addTreeEventListener( textLineTreeEventListener );
				newlineElement.addTreeEventListener( newlineTreeEventListener );
				
				Pres linePres = new Row( new Object[] { seg, newlineElement } );
				LSElement lineElement = linePres.present( ctx, style );
				lineElements.add( lineElement );
			}
			
			textBox.setChildren( lineElements );
			
			if ( caretPos != -1 )
			{
				PresentationComponent.RootElement root = textBox.getRootElement();
				root.getCaret().moveToPositionAndBiasWithinSubtree( textBox, root.getDefaultTextRepresentationManager(), caretPos, Marker.Bias.START );
			}
		}


		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			queueRebuild();
		}
		
		
		private void queueRebuild()
		{
			Runnable event = new Runnable()
			{
				@Override
				public void run()
				{
					String text = (String)value.getValue();
					changeText( text, getCaretIndex() );
				}
			};
			element.queueImmediateEvent( event );
		}
	}
	
	
	private static class CommitListener extends TextAreaListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onAccept(TextAreaControl textEntry, String text)
		{
			value.setLiteralValue( text );
		}
	}
	
	
	private LiveSource valueSource;
	private TextAreaListener listener;
	
	
	private TextArea(LiveSource valueSource, TextAreaListener listener)
	{
		this.valueSource = valueSource;
		this.listener = listener;
	}

	public TextArea(String initialText, TextAreaListener listener)
	{
		this( new LiveSourceValue( initialText ), listener );
	}
	
	public TextArea(LiveInterface value, TextAreaListener listener)
	{
		this( new LiveSourceRef( value ), listener );
	}
	
	public TextArea(LiveValue value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ) );
	}
	


	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		style = style.withAttr( Primitive.editable, true );
		StyleSheet textAreaStyleSheet = style.get( Controls.textAreaAttrs, StyleSheet.class );
		
		StyleValues textAreaStyle = style.withAttrs( textAreaStyleSheet );
		
		LiveInterface value = valueSource.getLive();

		Pres textBoxPres = new Column( new Pres[] {} );
		LSColumn textBox = (LSColumn)textBoxPres.present( ctx, textAreaStyle );
		Pres regionPres = new Region( textBox );
		LSRegion region = (LSRegion)regionPres.present( ctx, textAreaStyle );
		Pres elementPres = new Border( region );
		LSBorder element = (LSBorder)elementPres.present( ctx, textAreaStyle );
		
		return new TextAreaControl( ctx, style, element, region, textBox, listener, value );
	}
}
