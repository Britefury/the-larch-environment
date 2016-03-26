//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;

public interface RealiseElementInteractor extends AbstractElementInteractor
{
	public void elementRealised(LSElement element);
	public void elementUnrealised(LSElement element);
}
