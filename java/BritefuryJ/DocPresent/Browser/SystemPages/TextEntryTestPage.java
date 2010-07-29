//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.regex.Pattern;

import BritefuryJ.Controls.ControlPres;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.Controls.Hyperlink.HyperlinkControl;
import BritefuryJ.Controls.TextEntry;
import BritefuryJ.Controls.TextEntry.TextEntryControl;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Proxy;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

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
	
	
	private static class EditableLink extends ControlPres
	{
		private static class EditableLinkControl extends Control
		{
			private class TextListener extends TextEntry.TextEntryListener
			{
				public void onAccept(TextEntry.TextEntryControl textEntry, String text)
				{
					link.setText( text );
					proxy.setChild( link.getElement() );
				}
	
				public void onCancel(TextEntry.TextEntryControl textEntry, String originalText)
				{
					proxy.setChild( link.getElement() );
				}
				
				public void onTextInserted(TextEntry.TextEntryControl textEntry, int position, String textInserted)
				{
					status.setText( "INSERTED @" + position + " :" +  textInserted );
				}
	
				public void onTextRemoved(TextEntry.TextEntryControl textEntry, int position, int length)
				{
					status.setText( "REMOVED " + position + " to " + ( position + length ) );
				}
				
				public void onTextReplaced(TextEntry.TextEntryControl textEntry, int position, int length, String replacementText)
				{
					status.setText( "REPLACED " + position + " to " + ( position + length ) + " with: " + replacementText );
				}
			}
			
			private class LinkListener implements Hyperlink.LinkListener
			{
				
				public void onLinkClicked(Hyperlink.HyperlinkControl link, PointerButtonEvent event)
				{
					status.setText( "" );
					entry.setText( link.getText() );
					proxy.setChild( StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 10.0 ).applyTo( new HBox( new Object[] { entry.getElement(), status } ) ).present( ctx ) );
					entry.grabCaret();
				}
			}
			
			
			private DPText status;
			private Hyperlink.HyperlinkControl link;
			private TextEntry.TextEntryControl entry;
			private DPProxy proxy;
			
			
			public EditableLinkControl(PresentationContext ctx)
			{
				super( ctx );
			}
			
			
			LinkListener linkListener()
			{
				return new LinkListener();
			}
			
			TextListener entryListener()
			{
				return new TextListener();
			}
			
			
			
			
			@Override
			public DPElement getElement()
			{
				return proxy;
			}
		}
		
		
		
		private String initialText;
		private Pattern validationRegex = null;
		private String validationFailMsg = null;
		
		
		public EditableLink(String text)
		{
			this.initialText = text;
		}
		
		public EditableLink(String text, Pattern validationRegex, String validationFailMessage)
		{
			this.initialText = text;
			this.validationRegex = validationRegex;
			this.validationFailMsg = validationFailMessage;
		}
		
		
		@Override
		public Control createControl(PresentationContext ctx)
		{
			EditableLinkControl ctl = new EditableLinkControl( ctx );
			
			StaticText status = new StaticText( "" );
			DPText statusElement = (DPText)status.present( ctx );
			
			Hyperlink link = new Hyperlink( initialText, ctl.linkListener() );
			TextEntry entry;
			
			if ( validationRegex == null )
			{
				entry = new TextEntry( initialText, ctl.entryListener() );
			}
			else
			{
				entry = new TextEntry( initialText, ctl.entryListener(), validationRegex, validationFailMsg );
			}
			
			Proxy proxy = new Proxy( link );
			DPProxy proxyElement = (DPProxy)proxy.present( ctx );
			
			ctl.status = statusElement;
			ctl.link = (HyperlinkControl)link.createControl( ctx );
			ctl.entry = (TextEntryControl)entry.createControl( ctx );
			ctl.proxy = proxyElement;
			
			return ctl;
		}
	}

	

	
	protected DPElement createContents()
	{
		EditableLink hello = new EditableLink( "Hello" );
		EditableLink world = new EditableLink( "World" );
		EditableLink identifier = new EditableLink( "abc", Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" ), "Please enter a valid identifier.\n(alphabetic or underscore, followed by alphanumeric or underscore)" );
		EditableLink integer = new EditableLink( "123", Pattern.compile( "[0-9]+" ), "Please enter a valid integer." );
		
		Pres identifierLine = new Paragraph( new Pres[] { new StaticText( "Identifier: "), identifier } );
		Pres integerLine = new Paragraph( new Pres[] { new StaticText( "Integer: "), integer } );
		
		Pres entriesBox = new VBox( new Pres[] { hello, world, identifierLine, integerLine } );
		
		return new Body( new Pres[] { new Heading2( "Text entries" ), entriesBox } ).present();
	}
}
