//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Controls.TextArea;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.Heading6;
import BritefuryJ.StyleSheet.StyleSheet;

public class TextAreaTestPage extends TestPage
{
	private static final StyleSheet redText = StyleSheet.style( Primitive.foreground.as( Color.RED ) );
	
	protected TextAreaTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Text area test";
	}
	
	protected String getDescription()
	{
		return "Text area control: a multi-line text editor.";
	}
	
	

	private class AreaListener extends TextArea.TextAreaListener
	{
		private LiveValue resultArea, eventList;
		private LiveFunction eventArea;
		private LiveValue text;
		//private DPColumn resultArea, eventArea;
		private String prevText;
		
		public AreaListener(LiveValue text)
		{
			this.resultArea = new LiveValue( new Blank() );
			this.eventList = new LiveValue( new ArrayList<Object>() );
			LiveFunction.Function eventBox = new LiveFunction.Function()
			{
				@Override
				public Object evaluate()
				{
					@SuppressWarnings("unchecked")
					List<Object> x = (List<Object>)eventList.getValue();
					return new Column( x.toArray() );
				}
			};
			eventArea = new LiveFunction( eventBox );
			this.text = text;
			this.prevText = (String)text.getStaticValue();
		}
		
		
		public LiveInterface getResultArea()
		{
			return resultArea;
		}
		
		public LiveInterface getEventArea()
		{
			return eventArea;
		}
		
		public void onAccept(TextArea.TextAreaControl textArea, String text)
		{
			String[] lines = text.split( "\n" );
			
			ArrayList<LSElement> lineElements = new ArrayList<LSElement>();
			for (String line: lines)
			{
				lineElements.add( new Label( line ).present() );
			}
			
			resultArea.setLiteralValue( new Column( lineElements.toArray() ) );
			
			this.text.setLiteralValue( text );
			
			prevText = text;
		}
		
		public void onTextInserted(TextArea.TextAreaControl textArea, int position, String textInserted)
		{
			String text = "Inserted text @" + position + ":\n" + textInserted.replace( "\n", "\\n" );
			setEventText( text );
			prevText = prevText.substring( 0, position ) + textInserted + prevText.substring( position );
			if ( !prevText.equals( textArea.getDisplayedText() ) )
			{
				@SuppressWarnings("unchecked")
				List<Object> x = (List<Object>)eventList.getValue();
				x.add( redText.applyTo( new Label( "Insert event was invalid" ) ).present() );
				eventList.setLiteralValue( x );
			}
		}

		public void onTextRemoved(TextArea.TextAreaControl textArea, int position, int length)
		{
			String textRemoved = prevText.substring( position, position + length );
			String text = "Removed " + position + " to " + ( position + length ) + ":\n" + textRemoved.replace( "\n", "\\n" );
			setEventText( text );
			prevText = prevText.substring( 0, position ) + prevText.substring( position + length );
			if ( !prevText.equals( textArea.getDisplayedText() ) )
			{
				@SuppressWarnings("unchecked")
				List<Object> x = (List<Object>)eventList.getValue();
				x.add( redText.applyTo( new Label( "Remove event was invalid" ) ).present() );
				eventList.setLiteralValue( x );
			}
		}
		
		public void onTextReplaced(TextArea.TextAreaControl textArea, int position, int length, String replacementText)
		{
			String text = "Replaced " + position + " to " + ( position + length ) + " with:\n" + replacementText;
			setEventText( text );
			prevText = prevText.substring( 0, position ) + replacementText.replace( "\n", "\\n" ) + prevText.substring( position + length );
			if ( !prevText.equals( textArea.getDisplayedText() ) )
			{
				@SuppressWarnings("unchecked")
				List<Object> x = (List<Object>)eventList.getValue();
				x.add( redText.applyTo( new Label( "Replace event was invalid" ) ).present() );
				eventList.setLiteralValue( x );
			}
		}
		
		private void setEventText(String text)
		{
			String[] lines = text.split( "\n" );
			
			ArrayList<LSElement> lineElements = new ArrayList<LSElement>();
			for (String line: lines)
			{
				lineElements.add( new Label( line ).present() );
			}
			
			@SuppressWarnings("unchecked")
			List<Object> x = (List<Object>)eventList.getValue();
			x.clear();
			x.addAll( lineElements );
			eventList.setLiteralValue( x );
		}
	}

	
	
	private static final String testString = "This is a test of the capabilities of the text area control.\nA text area is a mult-line text editor.\nThe text is in the form of lines separated by newline characters.\n";

	
	
	protected Pres createContents()
	{
		LiveValue text = new LiveValue( testString );
		AreaListener listener = new AreaListener( text );
		TextArea area = new TextArea( testString, listener );
		
		Pres resultBox = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) ).applyTo( new Column( new Object[] { new Heading6( "Text:" ), listener.getResultArea(),
				new Heading6( "Event:" ), listener.getEventArea() } ) );
		
		Pres areaBox = StyleSheet.style( Primitive.columnSpacing.as( 10.0 ) ).applyTo( new Column( new Object[] { area.alignHExpand(), resultBox.alignHExpand() } ) );
		
		return new Body( new Pres[] { new Heading2( "Text area" ), areaBox.alignHExpand() } ).alignHExpand();
	}
}
