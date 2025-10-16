/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util

/**
 * @author tag
 * @version $Id$
 */
object WWUtil {

    /**
     * Determine whether an object reference is null or a reference to an empty string.
     *
     * @param s the reference to examine.
     *
     * @return true if the reference is null or is a zero-length [String].
     */
    @JvmStatic
    fun isEmpty(s: Any?): Boolean {
        return s == null || (s is String && s.isEmpty())
    }
}
