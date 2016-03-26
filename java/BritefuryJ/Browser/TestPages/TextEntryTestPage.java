//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.TextEntry;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class TextEntryTestPage extends TestPage
{
	protected TextEntryTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Text entry test";
	}
	
	protected String getDescription()
	{
		return "Text entry element: permits the entry of text. Click the links to switch them for text entries to change their text.";
	}
	
	
	private static class TextEntryTest implements Presentable
	{
		private LiveValue value;
		private Pattern validationRegex = null;
		private String validationFailMsg = null;
		
		public TextEntryTest(LiveValue value, Pattern validationRegex, String validationFailMessage)
		{
			this.value = value;
			this.validationRegex = validationRegex;
			this.validationFailMsg = validationFailMessage;
		}

		public TextEntryTest(LiveValue value)
		{
			this.value = value;
		}

		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			final LiveValue message = new LiveValue( new Label( "No change yet..." ) );
			
			TextEntry.TextEntryListener listener = new TextEntry.TextEntryListener()
			{
				@Override
				public void onAccept(TextEntry.TextEntryControl textEntry, String text)
				{
					value.setLiteralValue( text );
					message.setLiteralValue( new Label( "Accepted" ) );
				}

				@Override
				public void onCancel(TextEntry.TextEntryControl textEntry)
				{
					message.setLiteralValue( new Label( "Cancelled" ) );
				}

				@Override
				public void onTextInserted(TextEntry.TextEntryControl textEntry, int position, String textInserted)
				{
					message.setLiteralValue( new Label( "INSERTED @" + position + " :" +  textInserted ) );
				}

				@Override
				public void onTextRemoved(TextEntry.TextEntryControl textEntry, int position, int length)
				{
					message.setLiteralValue( new Label( "REMOVED " + position + " to " + ( position + length ) ) );
				}

				@Override
				public void onTextReplaced(TextEntry.TextEntryControl textEntry, int position, int length, String replacementText)
				{
					message.setLiteralValue( new Label( "REPLACED " + position + " to " + ( position + length ) + " with: " + replacementText ) );
				}
			};
			
			Pres entry;
			if ( validationRegex != null )
			{
				entry = new TextEntry( value, listener ).regexValidated( validationRegex, validationFailMsg );
			}
			else
			{
				entry = new TextEntry( value, listener );
			}
			
			return new Row( new Object[] { entry.padX( 5.0 ),
					new Row( new Object[] { new Label( "Value: " ), value } ).padX( 5.0 ),
					new Row( new Object[] { new Label( "Message: " ), message } ).padX( 5.0 ) } );
		}
	}
	
	
	protected Pres createContents()
	{
		TextEntryTest hello = new TextEntryTest( new LiveValue( "Hello" ) );
		TextEntryTest world = new TextEntryTest( new LiveValue( "World" ) );
		TextEntryTest identifier = new TextEntryTest( new LiveValue( "abc" ), Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" ), "Please enter a valid identifier.\n(alphabetic or underscore, followed by alphanumeric or underscore)" );
		TextEntryTest integer = new TextEntryTest( new LiveValue( "123" ), Pattern.compile( "[0-9]+" ), "Please enter a valid integer." );
		
		Pres identifierLine = new Paragraph( new Object[] { new Label( "Identifier: "), identifier } );
		Pres integerLine = new Paragraph( new Object[] { new Label( "Integer: "), integer } );
		
		LiveValue live = new LiveValue( "Its alive..." );
		Pres liveLine = new Paragraph( new Object[] { new Label( "Live: "), TextEntry.textEntryCommitOnChange( live ), new Spacer( 10.0, 0.0 ), live } );
		LiveValue live2 = new LiveValue( "2013" );
		Pres liveValidatedLine = new Paragraph( new Object[] { new Label( "Live (4-digit integers): "),
				TextEntry.textEntryCommitOnChange( live2 ).regexValidated( Pattern.compile( "[0-9_]{4}" ),
						"Please enter a valid identifier.\n(alphabetic or underscore, followed by alphanumeric or underscore)" ),
						new Spacer( 10.0, 0.0 ), live2 } );
		
		Pres entriesBox = new Column( new Object[] { hello, world, identifierLine, integerLine, liveLine, liveValidatedLine } );
		
		return new Body( new Pres[] { new Heading2( "Text entries" ), entriesBox } );
	}
}
