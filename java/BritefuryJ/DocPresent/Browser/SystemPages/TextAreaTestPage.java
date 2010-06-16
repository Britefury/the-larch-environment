//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.TextArea;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class TextAreaTestPage extends SystemPage
{
	protected TextAreaTestPage()
	{
		register( "tests.controls.textarea" );
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
		private DPVBox resultArea, eventArea;
		private String prevText;
		
		public AreaListener(DPVBox resultArea, DPVBox eventArea, String text)
		{
			this.resultArea = resultArea;
			this.eventArea = eventArea;
			this.prevText = text;
		}
		
		public void onAccept(TextArea textArea, String text)
		{
			String[] lines = text.split( "\n" );
			
			ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
			for (String line: lines)
			{
				lineElements.add( styleSheet.staticText( line ) );
			}
			
			resultArea.setChildren( lineElements );
		}
		
		public void onTextInserted(TextArea textArea, int position, String textInserted)
		{
			String text = "Inserted text @" + position + ":\n" + textInserted;
			setEventText( text );
			prevText = prevText.substring( 0, position ) + textInserted + prevText.substring( position );
			if ( !prevText.equals( textArea.getText() ) )
			{
				eventArea.append( PrimitiveStyleSheet.instance.withForeground( Color.RED ).staticText( "Insert event was invalid" ) );
			}
		}

		public void onTextRemoved(TextArea textArea, int position, int length)
		{
			String text = "Removed " + position + " to " + ( position + length );
			setEventText( text );
			prevText = prevText.substring( 0, position ) + prevText.substring( position + length );
			if ( !prevText.equals( textArea.getText() ) )
			{
				eventArea.append( PrimitiveStyleSheet.instance.withForeground( Color.RED ).staticText( "Insert event was invalid" ) );
			}
		}
		
		public void onTextReplaced(TextArea textArea, int position, int length, String replacementText)
		{
			String text = "Replaced " + position + " to " + ( position + length ) + " with:\n" + replacementText;
			setEventText( text );
			prevText = prevText.substring( 0, position ) + replacementText + prevText.substring( position + length );
			if ( !prevText.equals( textArea.getText() ) )
			{
				eventArea.append( PrimitiveStyleSheet.instance.withForeground( Color.RED ).staticText( "Insert event was invalid" ) );
			}
		}
		
		private void setEventText(String text)
		{
			String[] lines = text.split( "\n" );
			
			ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
			for (String line: lines)
			{
				lineElements.add( styleSheet.staticText( line ) );
			}
			
			eventArea.setChildren( lineElements );
		}
	}

	
	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFontSize( 18 );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;
	
	private String testString = "This is a test of the capabilities of the text area control.\nA text area is a mult-line text editor.\nThe text is in the form of lines separated by newline characters.\n";

	
	
	protected DPElement section(String title, DPElement contents)
	{
		DPElement heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( new DPElement[] { heading.padY( 10.0 ), contents.alignHExpand() } ).alignHExpand();
	}
	
	protected DPElement createContents()
	{
		DPVBox resultArea = styleSheet.vbox( new DPElement[] {} );
		DPVBox eventArea = styleSheet.vbox( new DPElement[] {} );
		DPVBox resultBox = styleSheet.withVBoxSpacing( 5.0 ).vbox( new DPElement[] { styleSheet.staticText( "Text:" ), resultArea, styleSheet.staticText( "Event:" ), eventArea } );
		
		TextArea area = controlsStyleSheet.textArea( testString, new AreaListener( resultArea, eventArea, testString ) );
		
		DPElement areaBox = styleSheet.withVBoxSpacing( 10.0 ).vbox( new DPElement[] { area.getElement().alignHExpand(), resultBox.alignHExpand() } );
		DPElement textAreaSection = section( "Text area", areaBox.alignHExpand() );
		
		return textAreaSection;
	}
}
