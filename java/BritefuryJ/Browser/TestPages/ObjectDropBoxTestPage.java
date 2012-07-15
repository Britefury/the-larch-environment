//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.ObjectDropBox;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleSheet;

public class ObjectDropBoxTestPage extends TestPage
{
	protected ObjectDropBoxTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Object drop box test";
	}
	
	protected String getDescription()
	{
		return "Object drop box control: drop an object";
	}
	
	
	protected Pres createContents()
	{
		LiveValue value = new LiveValue( null );

		ObjectDropBox objectDrop = new ObjectDropBox( value );
		
		Pres dropLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Object: " ),
			    new SpaceBin( 100.0, -1.0, objectDrop.alignHExpand() ), value } ).padX( 5.0 ) );
		
		Pres instructions = new NormalText( "The object drop box receives object drops. To initiate a drop, ALT+SHIFT+left drag from (almost) anywhere within Larch to acquire " +
				"the data model that is displayed on a particular part of a Larch window. Drop the object here to take a look. Alternatively, you can ALT+SHIFT+right click to " +
				"inspect an object." );
		
		return new Body( new Pres[] { new Heading2( "Object drop box" ), instructions, dropLine } );
	}
}
