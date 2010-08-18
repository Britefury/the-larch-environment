//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.ContextMenu;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.ApplyStyleSheetFromAttribute;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class ControlsRow extends SequentialPres
{
	public ControlsRow(Object children[])
	{
		super( children );
	}
	
	public ControlsRow(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement xs[] = mapPresent( ctx, ContextMenuStyle.controlsRowUsage.useAttrs( style ), children );
		return new ApplyStyleSheetFromAttribute( ContextMenuStyle.controlsRowStyle, new Row( xs ) ).present( ctx, style );
	}
}
