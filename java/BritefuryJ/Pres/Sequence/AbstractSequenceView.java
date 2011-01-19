//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Sequence;

import java.util.List;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.SequentialPres;

public abstract class AbstractSequenceView extends SequentialPres
{
	protected Pres beginDelim, endDelim, separator, spacing;
	protected TrailingSeparator trailingSeparator;
	
	
	public AbstractSequenceView(Object children[], Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children );
		this.beginDelim = beginDelim;
		this.endDelim = endDelim;
		this.separator = separator;
		this.spacing = spacing;
		this.trailingSeparator = trailingSeparator;
	}
	
	public AbstractSequenceView(List<Object> children, Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children );
		this.beginDelim = beginDelim;
		this.endDelim = endDelim;
		this.separator = separator;
		this.spacing = spacing;
		this.trailingSeparator = trailingSeparator;
	}



	public static boolean trailingSeparatorRequired(int numElements, TrailingSeparator trailingSeparator)
	{
		return numElements > 0  &&  ( trailingSeparator == TrailingSeparator.ALWAYS  ||  ( trailingSeparator == TrailingSeparator.ONE_ELEMENT && numElements == 1 ) );
	}
}
