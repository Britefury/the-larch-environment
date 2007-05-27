//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef GSASSERT_H__
#define GSASSERT_H__

#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>



#define DEBUG


#ifdef DEBUG

inline void gs_assert_failed()
{
	abort();
}

inline void gs_assert(bool expression, const char *message, ...)
{
	if ( !expression )
	{
		va_list args;

		va_start( args, message );
		vprintf( message, args );
		va_end( args );

		gs_assert_failed();
	}
}

inline void gs_assert_not_reached(const char *message, ...)
{
	va_list args;

	va_start( args, message );
	vprintf( message, args );
	va_end( args );

	gs_assert_failed();
}

/*#define gs_assert_is_a(objectPtr, type, messageHeader)																							\
	gs_assert( objectPtr->getType()->isA( type::getTypeStatic() ), messageHeader ": '" #objectPtr "' is not an instance of \"" #type "\"; it is a \"%s\"\n",			\
			objectPtr->getType()->getName().c_str() )*/

#define gs_assert_is_instance_of(objectPtr, typeName, messageHeader)																				\
	gs_assert( objectPtr->isInstanceOf( typeName::getTypeStatic() ), messageHeader ": '" #objectPtr "' is not an instance of \"" #typeName "\"; it is a \"%s\"\n",		\
			objectPtr->getType()->getName().c_str() )

#define gs_assert_is_instance_of_type(objectPtr, type, messageHeader)																				\
	gs_assert( objectPtr->isInstanceOf( type ), messageHeader ": '" #objectPtr "' is not an instance of \"%s\"; it is a \"%s\"\n",									\
			type->getName().c_str(), objectPtr->getType()->getName().c_str() )

#define gs_assert_type_is_a(queryType, typeName, messageHeader)																					\
	gs_assert( queryType->isA( typeName::getTypeStatic() ), messageHeader ": '" #queryType "' is not not a \"" #typeName "\"; it is \"%s\"\n",								\
			queryType->getName().c_str() )																							\

#else

inline void gs_assert(bool expression, const char *message, ...)
{
}

inline void gs_assert_not_reached(const char *message, ...)
{
}

//#define gs_assert_is_a(objectPtr, type, messageHeader)
#define gs_assert_is_instance_of(objectPtr, type, messageHeader)
#define gs_assert_is_instance_of_type(objectPtr, type, messageHeader)
#define gs_assert_type_is_a(queryType, type, messageHeader)

#endif


#endif
