package BritefuryJ.Editor.RichText.Attrs;

import BritefuryJ.Util.HashUtils;

public class Intersection<T> {
	public T intersection, dIntersectionA, dIntersectionB;

	public Intersection(T intersection, T dIntersectionA, T dIntersectionB) {
		this.intersection = intersection;
		this.dIntersectionA = dIntersectionA;
		this.dIntersectionB = dIntersectionB;
	}


	@Override
	public boolean equals(Object other) {
		if (other instanceof Intersection) {
			Intersection<T> i = (Intersection<T>)other;

			if (intersection == null  &&  i.intersection == null  ||
					intersection != null && i.intersection != null && intersection.equals(i.intersection)) {
				if (dIntersectionA == null  &&  i.dIntersectionA == null  ||
						dIntersectionA != null && i.dIntersectionA != null && dIntersectionA.equals(i.dIntersectionA)) {
					if (dIntersectionB == null  &&  i.dIntersectionB == null  ||
							dIntersectionB != null && i.dIntersectionB != null && dIntersectionB.equals(i.dIntersectionB)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return HashUtils.tripleHash(intersection != null ? intersection.hashCode() : 0,
				dIntersectionA != null ? dIntersectionA.hashCode() : 0,
				dIntersectionB != null ? dIntersectionB.hashCode() : 0);
	}
}
