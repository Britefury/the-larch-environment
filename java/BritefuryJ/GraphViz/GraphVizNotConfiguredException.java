//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GraphViz;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.ObjectPres.ErrorBoxWithFields;
import BritefuryJ.Pres.ObjectPres.VerticalField;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.UI.SectionHeading2;
import BritefuryJ.Pres.UI.SectionHeading3;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


public class GraphVizNotConfiguredException extends RuntimeException implements Presentable
{
	private static final long serialVersionUID = 1L;


	public GraphVizNotConfiguredException()
	{
		super( "GraphViz not configured - set up in the GraphViz configuration page" );
	}


	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres title = new SectionHeading2( "GraphViz not configured " );

		Pres explanation = new NormalText( "You need to configure GraphViz before graphs can be displayed (GraphViz will need to be installed on this machine)" );

		Pres configure = null;

		Subject configSubject = Configuration.configurationPageSubject;
		if ( configSubject != null )
		{
			Pres configureLink = new Hyperlink( "click here", configSubject );
			configure = new NormalText( new Object[] { "To configure GraphViz, please ", configureLink, "." } );
		}

		Pres messageContents[] = configure != null  ?  new Pres[] { title, explanation, configure }  :  new Pres[] { title, explanation };

		Pres message = messageColStyle.applyTo( new Column( messageContents ) );


		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		printStackTrace( new PrintStream( buf ) );
		String stackTrace;
		try
		{
			stackTrace = buf.toString( "ISO-8859-1" );
		}
		catch (UnsupportedEncodingException e1)
		{
			stackTrace = toString();
		}
		String stackTraceLines[] = stackTrace.split( "\n" );
		Pres stackTraceElements[] = new Pres[stackTraceLines.length];

		for (int i = 0; i < stackTraceLines.length; i++)
		{
			stackTraceElements[i] = stackTraceStyle.applyTo( new NormalText( stackTraceLines[i] ) );
		}

		Pres fields[] = {
				message,
				new DropDownExpander( new SectionHeading3( "Traceback" ), new Column( stackTraceElements ) )
		};

		return new ErrorBoxWithFields( "JAVA EXCEPTION", fields );
	}



	protected static final StyleSheet stackTraceStyle = StyleSheet.style( Primitive.editable.as( false ), Primitive.foreground.as( new Color( 0.75f, 0.1f, 0.4f ) ) );
	protected static final StyleSheet messageColStyle = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) );
}
