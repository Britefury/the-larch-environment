//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.util.regex.Pattern;

import BritefuryJ.Controls.EditableLabel;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class EditableLabelTestPage extends TestPage
{
	protected EditableLabelTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Editable label test";
	}
	
	protected String getDescription()
	{
		return "Editable label control: click the label to edit its text";
	}
	
	

	
	protected Pres createContents()
	{
		Pres notSet = notSetStyle.applyTo( new Label( "<not set>" ) );
		EditableLabel hello = new EditableLabel( new LiveValue( "Hello" ), notSet );
		EditableLabel world = new EditableLabel( new LiveValue( "World" ), notSet );
		EditableLabel identifier = new EditableLabel( new LiveValue( "abc" ), notSet ).regexValidated( Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" ), "Please enter a valid identifier.\n(alphabetic or underscore, followed by alphanumeric or underscore)" );
		EditableLabel integer = new EditableLabel( new LiveValue( "123" ), notSet ).regexValidated( Pattern.compile( "[0-9]+" ), "Please enter a valid integer." );
		
		Pres identifierLine = new Paragraph( new Pres[] { new Label( "Identifier: "), identifier } );
		Pres integerLine = new Paragraph( new Pres[] { new Label( "Integer: "), integer } );
		
		Pres entriesBox = new Column( new Pres[] { hello, world, identifierLine, integerLine } );
		
		return new Body( new Pres[] { new Heading2( "Editable labels" ), entriesBox } );
	}


	private static final StyleSheet notSetStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.0f ) ) );
}
