//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
/*
This array class has the ability to resize the array as required
The class consists of three data members:
  data	pointer to a C-array containing the data
  sz		the size or number of elements
  cap		the number of elements the array can hold until a resize is required
*/
#ifndef ARRAY_H__
#define ARRAY_H__

#include <memory.h>

#include <stdio.h>

#include <memory>
#include <algorithm>




#ifndef ARRAY_DEFAULTCAP__		//this is the default value for the initial capacity
#define ARRAY_DEFAULTCAP__ 4
#endif


#ifndef ARRAY_LIGHTOPTIMISE__
#define ARRAY_LIGHTOPTIMISE__ 8
#endif




/*
*******************************************************************************
											Array
*******************************************************************************
*/

template <typename T, typename ALLOC = std::allocator<T> > class Array
{
private:
	//the data, size, capacity, delta, and end pointer
	T *data;
	int sz, cap;
	ALLOC alloc;

	//typedef the self type for convenience
	typedef Array<T, ALLOC> Self;



	inline int computeIncrementedCapacity()
	{
		return cap == 0 ? ARRAY_DEFAULTCAP__  :  cap + cap;
	}

	//double capacity
	inline void incrementCapacity()
	{
		int newCap = computeIncrementedCapacity();
		setCapacity( newCap );
	}

	//increase capacity to store @n elements
	inline void increaseCapacity(int n)
	{
		if ( cap < n )
		{
			int newCap = computeIncrementedCapacity();

			newCap = std::max( n, newCap );

			setCapacity( newCap );
		}
	}




public:
	//typedef iterator types
	typedef T* iterator;
	typedef const T* const_iterator;

	//default constructor: 0 elements, initial capacity and delta of default value
	inline Array()
	{
		sz = 0;
		cap = 0;
		data = NULL;
	}

	//construct from C array
	inline Array(T *inData, int aSize)
	{
		sz = aSize;
		cap = aSize;
		data = allocateArray( cap );
		constructArray( data, sz, inData );
	}

	//construct array with single element
	inline Array(const T &element)
	{
		sz = 1;
		cap = 1;
		data = allocateArray( cap );
		constructElement( &data[0], element );
	}

	//copy constructor
	inline Array(const Self &a)
	{
		sz = a.sz;
		cap = a.cap;
		data = allocateArray( cap );
		constructArray( data, sz, a.data );
	}

	//copy constructor; different allocator
	template <typename A2> inline Array(const Array<T,A2> &a)
	{
		sz = a.size();
		cap = a.capacity();
		data = allocateArray( cap );
		constructArray( data, sz, a.begin() );
	}

	//copy Array subset
	template <typename A2> inline Array(const Array<T,A2> &a, int p, int n)
	{
		sz = n;
		cap = n;
		data = allocateArray( cap );
		constructArray( data, sz, a.begin() + p );
	}

	//destructor
	inline ~Array()
	{
		destroyArray( data, sz );
		freeArray( data, cap );
		sz = cap = 0;
	}



private:
	inline T * allocateArray(int n)
	{
		if ( n > 0 )
		{
			T *a = alloc.allocate( n );
			return a;
		}
		else
		{
			return NULL;
		}
	}

	inline void freeArray(T *a, int n)
	{
		if ( a != NULL )
		{
			alloc.deallocate( a, n );
		}
	}

	inline void constructElement(T *element)
	{
		constructElement( element, T() );
	}

	inline void constructElement(T *element, const T &value)
	{
		alloc.construct( element, value );
	}

	inline void destroyElement(T *element)
	{
		alloc.destroy( element );
	}

	inline void constructArray(T *a, int n)
	{
		constructArray( a, n, T() );
	}

	inline void constructArray(T *a, int n, const T &value)
	{
		if ( a != NULL )
		{
			for (int i = 0; i < n; i++)
			{
				constructElement( &a[i], value );
			}
		}
	}

	inline void constructArray(T *a, int n, const T *src)
	{
		if ( a != NULL )
		{
			if ( src != NULL )
			{
				for (int i = 0; i < n; i++)
				{
					constructElement( &a[i], src[i] );
				}
			}
			else
			{
				constructArray( a, n );
			}
		}
	}

	inline void destroyArray(T *a, int n)
	{
		if ( a != NULL )
		{
			for (int i = 0; i < n; i++)
			{
				destroyElement( &a[i] );
			}
		}
	}



public:
	//assignment operator: copy
	inline Self &operator=(const Self &a)
	{
		//delete old contents
		destroyArray( data, sz );
		freeArray( data, cap );

		//get new contents
		sz = a.sz;
		cap = a.cap;
		data = allocateArray( cap );
		constructArray( data, sz, a.data );

		//return a reference to this
		return *this;
	}

	//assignment operator; copy; different allocator type
	template <typename A2> inline Self &operator=(const Array<T,A2> &a)
	{
		//delete old contents
		destroyArray( data, sz );
		freeArray( data, cap );

		//get new contents
		sz = a.size();
		cap = a.capacity();
		data = allocateArray( cap );
		constructArray( data, sz, a.begin() );

		//return a reference to this
		return *this;
	}


	//== comparison operator
	template <typename A2> inline bool operator==(const Array<T,A2> &a) const
	{
		//compare sizes followed by each element
		if ( sz != a.size() )
		{
			return false;
		}

		for (int i = 0; i < sz; i++)
		{
			if ( data[i] != a[i] )
			{
				return false;
			}
		}

		return true;
	}


	template <typename A2> inline bool operator!=(const Array<T,A2> &a) const
	{
		return !( *this == a );
	}



	//find an element
	inline int find(const T &element) const
	{
		for (int i = 0; i < sz; i++)
		{
			if ( data[i] == element )
			{
				return i;
			}
		}

		return -1;
	}



	//check if an element exists in @this
	inline bool contains(const T &element) const
	{
		return find( element )  !=  -1;
	}



	//set the capacity
	inline void setCapacity(int c)
	{
		if ( cap != c )
		{
			//clamp the new size to be equal to or less than the new capacity
			int newSize = std::min( sz, c );

			//allocate the new array
			T *newData = allocateArray( c );
			//construct the contents
			constructArray( newData, newSize, data );

			//destroy the old array
			destroyArray( data, sz );
			freeArray( data, cap );

			//change to new array
			sz = newSize;
			cap = c;
			data = newData;
		}
	}

	//reserve @n elements
	inline void reserve(int n)
	{
		if ( n > cap )
		{
			setCapacity( n );
		}
	}

	//reserve @n extra elements
	inline void reserveExtra(int n)
	{
		reserve( sz + n );
	}

	//resize the array
	inline void resize(int s)
	{
		//reserve space
		reserve( s );

		if ( s > sz )
		{
			//increasing size; construct the additional elements
			constructArray( data + sz, s - sz );
		}
		else if ( s < sz )
		{
			//decreasing size; destroy the removed elements
			destroyArray( data + s, sz - s );
		}

		//set size
		sz = s;
	}

	//increase size by @n elements
	inline void increaseSize(int n = 1)
	{
		//calculate new size
		int newSize = sz + n;

		//ensure that there is adequate capacity
		increaseCapacity( newSize );

		//increase size
		resize( newSize );
	}

	//optimise the memory usage
	void optimiseMemoryUsageLean()
	{
		if ( sz  <  ( cap / 2 ) )
		{
			//less than half the capacity is used
			setCapacity( sz  +  ( sz / 2 ) );		//size * 1.5
		}
	}

	void optimiseMemoryUsageFor(int s)
	{
		//if the requested size is less than 1/n of the current capacity
		//(where n is ARRAY_LIGHTOPTIMISE__), compute a new size...
		if ( ( s * ARRAY_LIGHTOPTIMISE__ )  <  cap )
		{
			//the new size is s * 1.5
			int newSize = s  +  s / 2;

			//if the new size is sufficient for the existing contents
			if ( newSize >= sz )
			{
				//change capacity
				setCapacity( newSize );
			}
		}
	}



	//clear the array
	inline void clear()
	{
		//destroy all elements
		destroyArray( data, sz );
		sz = 0;
	}

	//insert @n copies of @element at position @p
	inline void insert(int p, int n, const T &element)
	{
		//if the starting position > size
		if ( p >= sz )
		{
			//elements dont need to be moved forward; add the elements to the end
			int newSize = p + n;

			//increase the capacity to hold the new elements
			increaseCapacity( newSize );

			//put blank elements up to @p
			constructArray( data + sz, p - sz );

			//fill the rest with @element
			constructArray( data + p, n, element );

			//increase size
			sz = newSize;
		}
		else
		{
			if ( n > 0 )
			{
				//size of the array
				int newSize = sz + n;

				//ensure adequate capacity
				increaseCapacity( newSize );


				//compute the index of the end of the inserted block
				int insertEnd = p + n;

				if ( insertEnd <= sz )
				{
					//all insertion takes place before the end of the current array

					//construct the @n new elements after the current array end, using
					//the last @n elements from the array
					constructArray( data + sz, n, data + sz - n );

					//move the existing elements after @p backward @n spaces
					for (int i = sz - n - 1; i >= p; i--)
					{
						data[i + n] = data[i];
					}

					//construct the elements in the inserted section
					for (int i = 0; i < n; i++)
					{
						data[p + i] = element;
					}
				}
				else
				{
					//some of the insertion takes place after the end of the current array
					//@n may be greater than @sz

					//compute the number of elements that have to be moved backward
					//this is the number of elements between @p and the end of the current array
					int numToMoveBackward = sz - p;

					//move these elements backward. destination is the end of the insertion, source is @p
					constructArray( data + insertEnd, numToMoveBackward, data + p );

					//construct the elements between the end of the current array, and the end of the insertion
					//with @element
					constructArray( data + sz, insertEnd - sz, element );

					//construct the elements in the inserted section that lies between @p and the end of the current array
					for (int i = p; i < sz; i++)
					{
						data[i] = element;
					}
				}


				//increase size
				sz += n;
			}
		}
	}

	//insert @element at position @p
	inline void insert(int p, const T &element)
	{
		insert( p, 1, element );
	}


	//insert @n copies of @element at position of iterator @pos
	inline void insert(iterator pos, int n, const T &element)
	{
		int position = pos - data;
		insert( position, n, element );
	}

	//insert @element at position of iterator @pos
	inline void insert(iterator pos, const T &element)
	{
		int position = pos - data;
		insert( position, 1, element );
	}


	//remove @n elements, starting at @p
	inline void remove(int p, int n = 1)
	{
		//if starting position >= sz, don't bother; nothing to do
		if ( p < sz )
		{
			if ( ( p + n )  >=  sz )
			{
				//if (p + n) >= sz, then only elements at the end are removed, so
				//elements don't need to be moved back, just remove those affected
				resize( p );
			}
			else
			{
				if ( n > 0 )
				{
					//move subsequent elements back
					for (int i = p; i  <  ( sz - n ); i++)
					{
						data[i] = data[ i + n ];
					}

					//destroy the last @n elements in the array
					destroyArray( data + sz - n, n );

					//reduce size
					sz -= n;
				}
			}
		}
	}

	//remove an element pointed to by an iterator
	inline void remove(iterator pos)
	{
		int position = pos - data;
		remove( position );
	}

	//find an element and remove it
	inline int findAndRemove(const T &element)
	{
		//find
		for (int i = 0; i < sz; i++)
		{
			if ( data[i] == element )
			{
				remove( i );
				return i;
			}
		}

		return -1;
	}

	//find and remove all instances of an element
	inline int findAndRemoveAll(const T &element)
	{
		int removedCount = 0;

		//find
		for (int i = sz - 1; i >= 0; i--)
		{
			if ( data[i] == element )
			{
				remove( i );
				removedCount++;
			}
		}

		return removedCount;
	}

	//wrapped remove; remove @n elements, starting at @p, wrap over end if
	//necessary
	//returns the number of places by which the array was shifted forward,
	//as a result of the erasure
	inline int wrappedRemove(int p, int n = 1)
	{
		//determine the maximum number of elements that can be removed
		//without needing to wrap
		int shift = 0;
		int wrapMax = sz - p;
		if ( n > wrapMax  &&  p != 0 )		//no point trying to wrap if p == 0
		{
			//need to wrap
			//remove all elements from p onwards; use resize()
			resize( p );
			//@wrapMax have been removed, @n - @wrapMax still need to be removed,
			//from the beginning
			p = 0;
			n -= wrapMax;
			shift = n;
		}

		//perform the removal
		remove( p, n );

		return shift;
	}

	//rotate contents forward (towards beginning)
	inline void rotateForward(int positions)
	{
		if ( positions != 0  &&  positions < sz )
		{
			Self tmp( *this, 0, positions );

			int remain = sz - positions;

			//shift elements forward
			for (int i = 0; i < remain; i++)
			{
				data[i] = data[positions + i];
			}

			//copy elements to end
			for (int i = 0; i < positions; i++)
			{
				data[remain + i] = tmp[i];
			}
		}
	}

	//rotate contents backward (towards end)
	inline void rotateBackward(int positions)
	{
		if ( positions != 0  &&  positions < sz )
		{
			int originalSize = sz;

			insert( 0, positions, T() );

			for (int i = 0; i < positions; i++)
			{
				data[i] = data[ i + originalSize ];
			}

			resize( originalSize );
		}
	}

	//swap two elements
	inline void swapElements(int index1, int index2)
	{
		//temporary space for element
		std::swap( data[index1], data[index2] );
	}

	//move an element within the array
	inline void move(int fromIndex, int toIndex)
	{
		if ( fromIndex != toIndex )
		{
			T movedElement = data[fromIndex];

			if ( toIndex > fromIndex )
			{
				//@toIndex is after @fromIndex:
				//move all elements between @fromIndex + 1 and @toIndex forward 1
				//(towards beginning)
				for (int i = fromIndex + 1; i <= toIndex; i++)
				{
					data[ i - 1 ] = data[i];
				}
			}
			else
			{
				//@toIndex is before @fromIndex:
				//move all elements between @toIndex and @fromIndex - 1 back 1
				//(towards end)
				for (int i = fromIndex - 1; i >= toIndex; i--)
				{
					data[ i + 1 ] = data[i];
				}

			}

			//put the moved element in position
			data[toIndex] = movedElement;
		}
	}

	//add elements from another array
	template <typename A2> inline void extend(const Array<T,A2> &from, int start, int n)
	{
		n = std::min( n, from.size() - start );
		reserveExtra( n );
		constructArray( data + sz, n, from.begin() + start );

		sz += n;
	}

	//add elements from another array
	template <typename A2> inline void extend(const Array<T,A2> &from)
	{
		extend( from, 0, from.size() );
	}

	//copy elements from another array, wrapping over the end of the
	//source array as many times as necessary
	template <typename A2> inline void wrappedCopy(const Array<T,A2> &from, int start, int n)
	{
		//reserve space
		reserveExtra( n );

		int remainingInFrom = from.size() - start;

		while ( n > 0 )
		{
			//compute number of elements to copy over
			int numToCopy = std::min( remainingInFrom, n );
			//copy
			append( from, start, numToCopy );

			//wrap over; start at 0
			start = 0;
			remainingInFrom = from.size();
			//take of @numToCopy elements
			n -= numToCopy;
		}
	}

	//duplicate elements from another array
	template <typename A2> inline void duplicate(const Array<T,A2> &from)
	{
		reserve( from.size() );

		int copy = std::min( sz, from.size() );

		for (int i = 0; i < copy; i++)
		{
			data[i] = from[i];
		}

		if ( copy < sz )
		{
			//remove extra elements
			destroyArray( data + copy, sz - copy );
		}
		else if ( copy < from.size() )
		{
			//copy additional elements
			constructArray( data + copy, from.size() - copy, from.begin() + copy );
		}

		sz = from.sz;
	}

	//swaps the contents of @with with the contents of @this
	inline void swapArray(Self &with)
	{
		std::swap( data, with.data );
		std::swap( sz, with.sz );
		std::swap( cap, with.cap );
	}

	//repeat an element earlier on in @this
	inline void repeat(int position)
	{
		T object = at( position );
		push_back( object );
	}

	//repeat elements earlier on in @this
	inline void repeat(int position, int n)
	{
		extend( *this, position, n );
	}


	//reverse order of a subset of the elements
	inline void reverse(int start, int n)
	{
		int halfSize = n / 2;
		int last = start + ( n - 1 );

		for (int i = 0; i < halfSize; i++)
		{
			swapElements( start + i, last - i );
		}
	}

	//reverse order of all elements
	inline void reverse()
	{
		reverse( 0, sz );
	}


	//add an element
	inline int push_back(const T &element)
	{
		//make the necessary space
		if ( sz >= cap )
		{
			incrementCapacity();
		}

		constructElement( &data[sz], element );

		return sz++;
	}

	//add an empty element
	inline T& push_back()
	{
		//make the necessary space
		if ( sz >= cap )
		{
			incrementCapacity();
		}

		constructElement( &data[sz] );

		return data[sz++];
	}

	//append n copies of element
	inline int fill(const T &element, int n)
	{
		int position = sz;

		insert( sz, n, element );

		return position;
	}

	//remove n elements
	inline void pop_back(int n = 1)
	{
		n = std::min( n, sz );
		destroyArray( data + sz - n, n );
		sz -= n;
	}


	//first element
	inline T &front()
	{
		return data[0];
	}

	inline const T &front() const
	{
		return data[0];
	}


	//last element
	inline T &back()
	{
		return data[sz - 1];
	}

	inline const T &back() const
	{
		return data[sz - 1];
	}

	//second last element
	inline T &secondLast()
	{
		return data[sz - 2];
	}

	inline const T &secondLast() const
	{
		return data[sz - 2];
	}


	//get size
	inline int size() const
	{
		return sz;
	}

	inline bool isEmpty() const
	{
		return sz == 0;
	}

	//get capacity
	inline int capacity() const
	{
		return cap;
	}

	//get memory usage
	inline int computeMemoryUsage() const
	{
		return cap * sizeof(T);
	}


	//get element at specific index
	inline T &operator[](int i)
	{
		return data[i];
	}

	inline const T &operator[](int i) const
	{
		return data[i];
	}

	//at(): used in place of [] operator when dealing with pointers
	inline T &at(int i)
	{
		return data[i];
	}

	inline const T &at(int i) const
	{
		return data[i];
	}

	//reverseAt(); like at() but count backwards from end
	inline T & reverseAt(int i)
	{
		return data[ sz - 1 - i ];
	}

	inline const T & reverseAt(int i) const
	{
		return data[ sz - 1 - i ];
	}


	//iterators pointing to beginning, last element, and end
	inline iterator begin()
	{
		return data;
	}

	inline iterator end()
	{
		return &data[sz];
	}

	inline const_iterator begin() const
	{
		return data;
	}

	inline const_iterator end() const
	{
		return &data[sz];
	}
};


#endif
