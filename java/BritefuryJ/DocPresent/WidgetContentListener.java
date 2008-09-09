//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Marker.Marker;

public interface WidgetContentListener
{
	public void contentInserted(Marker m, String x);
	public void contentRemoved(Marker m, int length);
	public void contentReplaced(Marker m, int length, String x);
}
