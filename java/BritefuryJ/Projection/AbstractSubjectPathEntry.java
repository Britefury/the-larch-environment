//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Projection;

public abstract class AbstractSubjectPathEntry
{
	public abstract Subject follow(Subject outerSubject);
	
	public abstract boolean canPersist();
}
