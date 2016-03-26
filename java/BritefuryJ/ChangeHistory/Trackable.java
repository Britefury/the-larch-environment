//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ChangeHistory;



public interface Trackable
{
	public ChangeHistory getChangeHistory();
	public void setChangeHistory(ChangeHistory h);
	
	public Iterable<Object> getTrackableContents();			// Can return null.   Temporary - is read once - is not needed afterwards
}
