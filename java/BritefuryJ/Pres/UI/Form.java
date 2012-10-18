//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.UI;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.GridRow;
import BritefuryJ.Pres.Primitive.RGrid;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Form extends Pres
{
	public static class Section extends Pres
	{
		private Pres title, notes, contents;
		
		public Section(String title, String notes, Object contents)
		{
			this.title = new SectionHeading2( title );
			this.notes = notes != null  ?  new NotesText( notes )  :  null;
			this.contents = Pres.coerce( contents );
		}
		
		
		@Override
		public LSElement present(PresentationContext ctx, StyleValues style)
		{
			StyleValues childStyle = UI.useFormSectionAttrs( style );
			Pres column0;
			if ( notes == null )
			{
				column0 = title;
			}
			else
			{
				double notesSpacing = (Double)style.get( UI.formNotesSpacing );
				column0 = new Column( new Pres[] { title, new Spacer( 0.0, notesSpacing ), notes } );
			}
			return new GridRow( new Pres[] { column0.alignHPack(), contents } ).alignHExpand().present( ctx, childStyle );
		}
		
	}
	
	
	
	private Pres title, sections[];
	
	public Form(String title, Object sections[])
	{
		this.title = title != null  ?  new SectionHeading1( title )  :  null;
		this.sections = Pres.mapCoerce( sections );
	}
	
	public Form(String title, List<Object> sections)
	{
		this.title = title != null  ?  new SectionHeading1( title )  :  null;
		this.sections = Pres.mapCoerce( sections );
	}
	

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle = UI.useFormAttrs( style );
		StyleSheet tableStyle = (StyleSheet)style.get( UI.formTableStyle );
		Pres table = tableStyle.applyTo( new RGrid( sections ) );
		double hPadding = (Double)style.get( UI.formHPadding ), vPadding = (Double)style.get( UI.formVPadding );
		Pres p;
		if ( title != null )
		{
			double spacing = (Double)style.get( UI.formTitleSpacing );
			p = new Column( new Pres[] { title, new Spacer( 0.0, spacing ), table } ).alignHExpand();
		}
		else
		{
			p = table.alignHExpand();
		}
		return p.pad( hPadding, vPadding ).present( ctx, childStyle );
	}
}
