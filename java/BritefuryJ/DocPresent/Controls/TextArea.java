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
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Clipboard.TextEditHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class TextArea extends Control
{
	public static interface TextAreaListener
	{
		public void onAccept(TextArea textArea, String text);
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
				if ( lineText.contains( "\n" ) )
				{
					recomputeText();
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
				if ( !whitespaceElement.getTextRepresentation().equals( "\n" ) )
				{
					recomputeText();
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
			String newText = textBox.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  textBox.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
			changeText( newText );
		}
		
		protected void insertText(Marker marker, String text)
		{
			marker.getElement().insertText( marker, text );
		}
		
		protected void replaceText(Selection selection, String replacement)
		{
			String newText = textBox.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  replacement  +  textBox.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
			changeText( newText );
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
		
		changeText( text );
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
		changeText( text );
	}
	
	
	private void recomputeText()
	{
		changeText( textBox.getTextRepresentation() );
	}
	
	private void changeText(String text)
	{
		String lines[] = text.split( "\n" );
		
		textLines.clear();
		textLines.addAll( Arrays.asList( lines ) );
		
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
}
