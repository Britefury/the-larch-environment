//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser;

import BritefuryJ.Projection.AbstractPerspective;

public interface EdgePane
{
	void setContent(Object contents, AbstractPerspective perspective, double size);
	void clearContent();
}
