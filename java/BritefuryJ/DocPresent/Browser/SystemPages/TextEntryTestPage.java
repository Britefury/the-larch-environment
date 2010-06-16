//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPText;
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
	
	
	private class EditableLink extends TextEntry.TextEntryListener implements Hyperlink.LinkListener
	{
		private DPProxy proxy;
		private Hyperlink link;
		private TextEntry entry;
		private DPText status;
		private PrimitiveStyleSheet primitive;
		
		
		public EditableLink(PrimitiveStyleSheet primitive, ControlsStyleSheet controls, String text)
		{
			link = controls.link( text, this );
			entry = controls.textEntry( text, this );
			proxy = primitive.proxy( link.getElement() );
			this.primitive = primitive;
		}
		
		public EditableLink(PrimitiveStyleSheet primitive, ControlsStyleSheet controls, String text, Pattern validationRegex, String validationFailMessage)
		{
			link = controls.link( text, this );
			entry = controls.textEntry( text, this, validationRegex, validationFailMessage );
			proxy = primitive.proxy( link.getElement() );
			this.primitive = primitive;
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
		
		public void onTextInserted(TextEntry textEntry, int position, String textInserted)
		{
			status.setText( "INSERTED @" + position + " :" +  textInserted );
		}

		public void onTextRemoved(TextEntry textEntry, int position, int length)
		{
			status.setText( "REMOVED " + position + " to " + ( position + length ) );
		}
		
		public void onTextReplaced(TextEntry textEntry, int position, int length, String replacementText)
		{
			status.setText( "REPLACED " + position + " to " + ( position + length ) + " with: " + replacementText );
		}

		public void onLinkClicked(Hyperlink link, PointerButtonEvent event)
		{
			status = primitive.staticText( "" );
			entry.setText( link.getText() );
			proxy.setChild( primitive.withHBoxSpacing( 10.0 ).hbox( new DPElement[] { entry.getElement(), status } ) );
			entry.grabCaret();
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
		EditableLink identifier = new EditableLink( styleSheet, controlsStyleSheet, "abc", Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" ), "Please enter a valid identifier.\n(alphabetic or underscore, followed by alphanumeric or underscore)" );
		EditableLink integer = new EditableLink( styleSheet, controlsStyleSheet, "123", Pattern.compile( "[0-9]+" ), "Please enter a valid integer." );
		
		DPElement identifierLine = styleSheet.paragraph( new DPElement[] { styleSheet.staticText( "Identifier: " ), identifier.getElement() } );
		DPElement integerLine = styleSheet.paragraph( new DPElement[] { styleSheet.staticText( "Integer: " ), integer.getElement() } );
		
		DPElement entriesBox = styleSheet.vbox( new DPElement[] { hello.getElement(), world.getElement(), identifierLine, integerLine } );
		DPElement entriesSection = section( "Text entries", entriesBox );
		
		return entriesSection;
	}
}
