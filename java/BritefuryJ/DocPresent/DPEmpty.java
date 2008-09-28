//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class DPEmpty extends DPWidget
{
	public DPEmpty()
	{
		this( WidgetStyleSheet.defaultStyleSheet );
	}
	
	public DPEmpty(WidgetStyleSheet styleSheet)
	{
		super( styleSheet );
	}

	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return new HMetrics();
	}

	protected HMetrics computePreferredHMetrics()
	{
		return new HMetrics();
	}


	protected VMetrics computeMinimumVMetrics()
	{
		return new VMetrics();
	}
	
	protected VMetrics computePreferredVMetrics()
	{
		return new VMetrics();
	}
}
