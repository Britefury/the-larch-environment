//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.Hyperlink;
import BritefuryJ.DocPresent.Controls.TextEntry;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class TextEntryTestPage extends SystemPage
{
	protected TextEntryTestPage()
	{
		register( "tests.controls.textentry" );
	}
	
	
	public String getTitle()
	{
		return "Text entry test";
	}
	
	protected String getDescription()
	{
		return "Text entry element: permits the entry of text. Click the links to switch them for text entries to change their text.";
	}
	
	
	private class EditableLink implements TextEntry.TextEntryListener, Hyperlink.LinkListener
	{
		private DPProxy proxy;
		private Hyperlink link;
		private TextEntry entry;
		
		
		public EditableLink(PrimitiveStyleSheet primitive, ControlsStyleSheet controls, String text)
		{
			link = controls.link( text, this );
			entry = controls.textEntry( text, this );
			proxy = primitive.proxy( link.getElement() );
		}
		
		
		public DPElement getElement()
		{
			return proxy;
		}

		public void onAccept(TextEntry textEntry, String text)
		{
			link.setText( text );
			proxy.setChild( link.getElement() );
		}

		public void onCancel(TextEntry textEntry, String originalText)
		{
			proxy.setChild( link.getElement() );
		}

		public boolean onLinkClicked(Hyperlink link, PointerButtonEvent event)
		{
			entry.setText( link.getText() );
			proxy.setChild( entry.getElement() );
			entry.grabCaret();
			return true;
		}
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFontSize( 18 );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	protected DPElement section(String title, DPElement contents)
	{
		DPElement heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( new DPElement[] { heading.padY( 10.0 ), contents } );
	}
	
	protected DPElement createContents()
	{
		EditableLink hello = new EditableLink( styleSheet, controlsStyleSheet, "Hello" );
		EditableLink world = new EditableLink( styleSheet, controlsStyleSheet, "World" );
		DPElement entriesBox = styleSheet.vbox( new DPElement[] { hello.getElement(), world.getElement() } );
		DPElement entriesSection = section( "Text entries", entriesBox );
		
		return entriesSection;
	}
}
