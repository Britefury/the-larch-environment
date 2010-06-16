//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TextEditEventInsert;
import BritefuryJ.DocPresent.TextEditEventRemove;
import BritefuryJ.DocPresent.TextEditEventReplace;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Clipboard.TextEditHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class TextArea extends Control
{
	public static class TextAreaListener
	{
		public void onAccept(TextArea textArea, String text)
		{
		}

		public void onTextInserted(TextArea textArea, int position, String textInserted)
		{
		}

		public void onTextRemoved(TextArea textArea, int position, int length)
		{
		}
		
		public void onTextReplaced(TextArea textArea, int position, int length, String replacementText)
		{
		}
	}
	
	
	private class TextAreaInteractor extends ElementInteractor
	{
		private TextAreaInteractor()
		{
		}
		
		
		public boolean onKeyPress(DPElement element, KeyEvent event)
		{
			return event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER;
		}

		public boolean onKeyRelease(DPElement element, KeyEvent event)
		{
			if ( event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER )
			{
				accept();
				return true;
			}
			return false;
		}

		public boolean onKeyTyped(DPElement element, KeyEvent event)
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

				if ( event instanceof TextEditEventInsert )
				{
					TextEditEventInsert insert = (TextEditEventInsert)event;
					listener.onTextInserted( TextArea.this, lineOffset + insert.getPosition(), insert.getTextInserted() );
				}
				else if ( event instanceof TextEditEventRemove )
				{
					TextEditEventRemove remove = (TextEditEventRemove)event;
					listener.onTextRemoved( TextArea.this, lineOffset + remove.getPosition(), remove.getLength() );
				}
				else if ( event instanceof TextEditEventReplace )
				{
					TextEditEventReplace replace = (TextEditEventReplace)event;
					listener.onTextReplaced( TextArea.this, lineOffset + replace.getPosition(), replace.getLength(), replace.getReplacement() );
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

				if ( event instanceof TextEditEventInsert )
				{
					TextEditEventInsert insert = (TextEditEventInsert)event;
					listener.onTextInserted( TextArea.this, lineOffset + insert.getPosition(), insert.getTextInserted() );
				}
				else if ( event instanceof TextEditEventRemove )
				{
					TextEditEventRemove remove = (TextEditEventRemove)event;
					listener.onTextRemoved( TextArea.this, lineOffset + remove.getPosition(), remove.getLength() );
				}
				else if ( event instanceof TextEditEventReplace )
				{
					TextEditEventReplace replace = (TextEditEventReplace)event;
					listener.onTextReplaced( TextArea.this, lineOffset + replace.getPosition(), replace.getLength(), replace.getReplacement() );
				}

				return true;
			}
			return false;
		}
	}
	
	
	private class TextAreaEditHandler extends TextEditHandler
	{
		protected void deleteText(Selection selection)
		{
			int startPosition = selection.getStartMarker().getClampedIndexInSubtree( textBox );
			int endPosition = selection.getEndMarker().getClampedIndexInSubtree( textBox );

			int caretPos = computeNewCaretPositionAfterRemove( getCaretIndex(), startPosition, endPosition - startPosition );
			String newText = textBox.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  textBox.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
			changeText( newText, caretPos );
			
			listener.onTextRemoved( TextArea.this, startPosition, endPosition - startPosition );
		}
		
		protected void insertText(Marker marker, String text)
		{
			marker.getElement().insertText( marker, text );
			
			// Don't inform the listener - the text edit event will take care of that
		}
		
		protected void replaceText(Selection selection, String replacement)
		{
			int startPosition = selection.getStartMarker().getClampedIndexInSubtree( textBox );
			int endPosition = selection.getEndMarker().getClampedIndexInSubtree( textBox );

			int caretPos = computeNewCaretPositionAfterReplace( getCaretIndex(), startPosition, endPosition - startPosition, replacement.length() );
			String newText = textBox.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  replacement  +  textBox.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
			changeText( newText, caretPos );
			
			listener.onTextReplaced( TextArea.this, startPosition, endPosition - startPosition, replacement );
		}
		
		protected String getText(Selection selection)
		{
			return textBox.getRootElement().getTextRepresentationInSelection( selection );
		}
	}
	
	
	
	private DPElement element;
	private DPVBox textBox;
	private TextAreaListener listener;
	private PrimitiveStyleSheet textStyle;
	private TextAreaTextLineTreeEventListener textLineTreeEventListener = new TextAreaTextLineTreeEventListener();
	private TextAreaNewlineTreeEventListener newlineTreeEventListener = new TextAreaNewlineTreeEventListener();
	
	private ArrayList<String> textLines = new ArrayList<String>();
	
	
	
	protected TextArea(DPElement element, DPRegion region, DPVBox textBox, TextAreaListener listener, PrimitiveStyleSheet textStyle, String text)
	{
		this.element = element;
		this.textBox = textBox;
		this.listener = listener;
		this.textStyle = textStyle;
	
		this.textBox.addInteractor( new TextAreaInteractor() );
		region.setEditHandler( new TextAreaEditHandler() );
		
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
		listener.onAccept( this, getText() );
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
			DPText textElement = textStyle.text( line );
			DPSegment segmentElement = textStyle.segment( true, true, textElement );
			DPWhitespace newline = textStyle.whitespace( "\n" );
			textElement.addTreeEventListener( textLineTreeEventListener );
			newline.addTreeEventListener( newlineTreeEventListener );
			
			DPElement lineElement = textStyle.hbox( new DPElement[] { segmentElement, newline } );
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
