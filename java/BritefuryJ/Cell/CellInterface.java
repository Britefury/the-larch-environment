//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;


import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalValueListener;



public abstract class CellInterface implements IncrementalOwner
{
	public abstract void addListener(IncrementalValueListener listener);
	public abstract void removeListener(IncrementalValueListener listener);
	
	
	
	public abstract Object getLiteralValue();
	public abstract void setLiteralValue(Object value);
	public abstract boolean isLiteral();
	
	public abstract Object getValue();
}
