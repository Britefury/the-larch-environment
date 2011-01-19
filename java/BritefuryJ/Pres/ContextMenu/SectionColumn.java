//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.ContextMenu;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.ApplyStyleSheetFromAttribute;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleValues;

public class SectionColumn extends SequentialPres
{
	public SectionColumn(Object children[])
	{
		super( children );
	}
	
	public SectionColumn(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement xs[] = mapPresent( ctx, ContextMenuStyle.sectionColumnUsage.useAttrs( style ), children );
		return new ApplyStyleSheetFromAttribute( ContextMenuStyle.sectionColumnStyle, new Column( xs ) ).present( ctx, style );
	}
}
