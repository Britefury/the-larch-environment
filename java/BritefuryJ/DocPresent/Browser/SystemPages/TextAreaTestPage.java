//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.Controls.TextArea;
import BritefuryJ.DocPresent.DPColumn;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.Heading6;
import BritefuryJ.StyleSheet.StyleSheet;

public class TextAreaTestPage extends SystemPage
{
	private static final StyleSheet redText = StyleSheet.instance.withAttr( Primitive.foreground, Color.RED );
	
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
		private DPColumn resultArea, eventArea;
		private String prevText;
		
		public AreaListener(DPColumn resultArea, DPColumn eventArea, String text)
		{
			this.resultArea = resultArea;
			this.eventArea = eventArea;
			this.prevText = text;
		}
		
		public void onAccept(TextArea.TextAreaControl textArea, String text)
		{
			String[] lines = text.split( "\n" );
			
			ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
			for (String line: lines)
			{
				lineElements.add( new Label( line ).present() );
			}
			
			resultArea.setChildren( lineElements );
		}
		
		public void onTextInserted(TextArea.TextAreaControl textArea, int position, String textInserted)
		{
			String text = "Inserted text @" + position + ":\n" + textInserted;
			setEventText( text );
			prevText = prevText.substring( 0, position ) + textInserted + prevText.substring( position );
			if ( !prevText.equals( textArea.getText() ) )
			{
				eventArea.append( redText.applyTo( new Label( "Insert event was invalid" ) ).present() );
			}
		}

		public void onTextRemoved(TextArea.TextAreaControl textArea, int position, int length)
		{
			String text = "Removed " + position + " to " + ( position + length );
			setEventText( text );
			prevText = prevText.substring( 0, position ) + prevText.substring( position + length );
			if ( !prevText.equals( textArea.getText() ) )
			{
				eventArea.append( redText.applyTo( new Label( "Insert event was invalid" ) ).present() );
			}
		}
		
		public void onTextReplaced(TextArea.TextAreaControl textArea, int position, int length, String replacementText)
		{
			String text = "Replaced " + position + " to " + ( position + length ) + " with:\n" + replacementText;
			setEventText( text );
			prevText = prevText.substring( 0, position ) + replacementText + prevText.substring( position + length );
			if ( !prevText.equals( textArea.getText() ) )
			{
				eventArea.append( redText.applyTo( new Label( "Insert event was invalid" ) ).present() );
			}
		}
		
		private void setEventText(String text)
		{
			String[] lines = text.split( "\n" );
			
			ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
			for (String line: lines)
			{
				lineElements.add( new Label( line ).present() );
			}
			
			eventArea.setChildren( lineElements );
		}
	}

	
	
	private static final String testString = "This is a test of the capabilities of the text area control.\nA text area is a mult-line text editor.\nThe text is in the form of lines separated by newline characters.\n";

	
	
	protected Pres createContents()
	{
		DPColumn resultArea = (DPColumn)new Column( new Pres[] {} ).present();
		DPColumn eventArea = (DPColumn)new Column( new Pres[] {} ).present();
		Pres resultBox = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( new Column( new Object[] { new Heading6( "Text:" ), resultArea, new Heading6( "Event:" ), eventArea } ) );
		
		TextArea area = new TextArea( testString, new AreaListener( resultArea, eventArea, testString ) );
		
		Pres areaBox = StyleSheet.instance.withAttr( Primitive.columnSpacing, 10.0 ).applyTo( new Column( new Object[] { area.alignHExpand(), resultBox.alignHExpand() } ) );
		
		return new Body( new Pres[] { new Heading2( "Text area" ), areaBox.alignHExpand() } ).alignHExpand();
	}
}
