//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPColumn;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.ElementValueFunction;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TextEditEventInsert;
import BritefuryJ.DocPresent.TextEditEventRemove;
import BritefuryJ.DocPresent.TextEditEventReplace;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.TextClipboardHandler;
import BritefuryJ.DocPresent.Interactor.KeyElementInteractor;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
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
	
	
	public static class TextAreaControl extends Control
	{
		private class TextAreaInteractor implements KeyElementInteractor
		{
			private TextAreaInteractor()
			{
			}
			
			
			@Override
			public boolean keyPressed(DPElement element, KeyEvent event)
			{
				return event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER;
			}
	
			@Override
			public boolean keyReleased(DPElement element, KeyEvent event)
			{
				if ( event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER )
				{
					accept();
					return true;
				}
				return false;
			}
	
			@Override
			public boolean keyTyped(DPElement element, KeyEvent event)
			{
				return event.isControlDown()  &&  event.getKeyChar() == KeyEvent.VK_ENTER;
			}
		}
		
		
		private class TextAreaTextLineTreeEventListener implements TreeEventListener
		{
			@Override
			public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					DPText textElement = (DPText)sourceElement;
					String lineText = textElement.getText();
					int lineOffset = textElement.getTextRepresentationOffsetInSubtree( textBox );
					
					if ( lineText.contains( "\n" ) )
					{
						int caretPos = getCaretIndex();
	
						recomputeText( caretPos );
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
			public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					DPWhitespace whitespaceElement = (DPWhitespace)sourceElement;
					
					int lineOffset = whitespaceElement.getTextRepresentationOffsetInSubtree( textBox );
					
					if ( !whitespaceElement.getTextRepresentation().equals( "\n" ) )
					{
						int caretPos = getCaretIndex();
	
						recomputeText( caretPos );
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
			protected void deleteText(Selection selection, Caret caret)
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
			protected void replaceText(Selection selection, Caret caret, String replacement)
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
			protected String getText(Selection selection)
			{
				return textBox.getRootElement().getTextRepresentationInSelection( selection );
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
	
		
		
		private DPElement element;
		private DPColumn textBox;
		private TextAreaListener listener;
		private TextAreaTextLineTreeEventListener textLineTreeEventListener = new TextAreaTextLineTreeEventListener();
		private TextAreaNewlineTreeEventListener newlineTreeEventListener = new TextAreaNewlineTreeEventListener();
		
		private ArrayList<String> textLines = new ArrayList<String>();
		
		
		
		protected TextAreaControl(PresentationContext ctx, StyleValues style, DPElement element, DPRegion region, DPColumn textBox, TextAreaListener listener, String text)
		{
			super( ctx, style );
			
			this.element = element;
			this.textBox = textBox;
			this.listener = listener;
		
			element.setValueFunction( new ValueFn() );
			
			this.textBox.addElementInteractor( new TextAreaInteractor() );
			region.setClipboardHandler( new TextAreaClipboardHandler() );
			
			changeText( text, -1 );
		}
		
	
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		public String getText()
		{
			return textBox.getTextRepresentation();
		}
		
		public void setText(String text)
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
				listener.onAccept( this, getText() );
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
			catch (DPElement.IsNotInSubtreeException e)
			{
				return -1;
			}
		}
		
		private void recomputeText(int caretPos)
		{
			changeText( textBox.getTextRepresentation(), caretPos );
		}
		
		private void changeText(String text, int caretPos)
		{
			if ( text.endsWith( "\n\n" ) )
			{
				// This handles the behaviour of String#split, which will not add a final blank line where the text ends with two new line characters.
				// To correct for this, add a space character, then remove that final artificial line afterwards
				text = text + " ";
				String lines[] = text.split( "\\r?\\n" );
				textLines.clear();
				textLines.addAll( Arrays.asList( lines ).subList( 0, lines.length - 1 ) );
			}
			else
			{
				String lines[] = text.split( "\\r?\\n" );
				textLines.clear();
				textLines.addAll( Arrays.asList( lines ) );
			}
			
			
			ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
			for (String line: textLines)
			{
				Pres textPres = new Text( line );
				DPElement textElement = textPres.present( ctx, style );
				Pres seg = new Segment( true, true, textElement );
				Pres newline = new Whitespace( "\n" );
				DPElement newlineElement = newline.present( ctx, style );
				textElement.addTreeEventListener( textLineTreeEventListener );
				newlineElement.addTreeEventListener( newlineTreeEventListener );
				
				Pres linePres = new Row( new Object[] { seg, newlineElement } );
				DPElement lineElement = linePres.present( ctx, style );
				lineElements.add( lineElement );
			}
			
			textBox.setChildren( lineElements );
			
			if ( caretPos != -1 )
			{
				System.out.println( "TextArea.changeText(): moving caret to " + caretPos );
				textBox.getRootElement().getCaret().moveToPositionAndBiasWithinSubtree( textBox, caretPos, Marker.Bias.START );
			}
		}
	}
	
	
	private String initialText;
	private TextAreaListener listener;
	
	
	public TextArea(String initialText, TextAreaListener listener)
	{
		this.initialText = initialText;
		this.listener = listener;
	}


	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		style = style.withAttr( Primitive.editable, true );
		StyleSheet textAreaStyleSheet = style.get( Controls.textAreaAttrs, StyleSheet.class );
		
		StyleValues textAreaStyle = style.withAttrs( textAreaStyleSheet );
		
		Pres textBoxPres = new Column( new Pres[] {} );
		DPColumn textBox = (DPColumn)textBoxPres.present( ctx, textAreaStyle );
		Pres regionPres = new Region( textBox );
		DPRegion region = (DPRegion)regionPres.present( ctx, textAreaStyle );
		Pres elementPres = new Border( region );
		DPBorder element = (DPBorder)elementPres.present( ctx, textAreaStyle );
		
		return new TextAreaControl( ctx, style, element, region, textBox, listener, initialText );
	}
}
