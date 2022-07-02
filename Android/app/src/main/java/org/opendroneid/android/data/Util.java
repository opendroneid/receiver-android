/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Util {

    public static class SetDifference<T> {
        final Set<T> added;
        final Set<T> removed;

        SetDifference(Set<? extends T> newSet, Set<? extends T> oldSet) {
            added = difference(newSet, oldSet);
            removed = difference(oldSet, newSet);
        }
    }

    /** OTHER - SET */
    private static <E> Set<E> difference(Set<? extends E> set, Set<? extends E> other) {
        HashSet<E> diff = new HashSet<>();
        for (E e : set) {
            if (!other.contains(e)) {
                diff.add(e);
            }
        }
        return diff;
    }

    /**
     * set - other, what is not in other
     */

    public static class DiffObserver<T> implements Observer<Set<T>> {
        Set<T> last = Collections.emptySet();

        @Override
        public void onChanged(@Nullable Set<T> newSet) {
            SetDifference<T> difference = new SetDifference<>(newSet, last);

            if (!difference.added.isEmpty()) {
                onAdded(difference.added);
            }
            if (!difference.removed.isEmpty()) {
                onRemoved(difference.removed);
            }
            last = newSet;
        }

        public void onAdded(Collection<T> added) { }
        public void onRemoved(Collection<T> removed) { }
    }
}
