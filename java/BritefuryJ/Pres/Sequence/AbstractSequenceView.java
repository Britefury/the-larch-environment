//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
