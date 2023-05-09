/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.common.observable.collection;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ObservableSet<S> extends ObservableCollection<S> implements Set<S> {
    public ObservableSet() {
        super();
    }

    public ObservableSet(Collection<S> values) {
        super(values);
    }

    @Override
    protected Collection<S> createCollection() {
        return new CopyOnWriteArraySet<>();
    }

    public Set<S> getSet() {
        return (Set<S>) collection;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof ObservableCollection))
            return false;

        ObservableCollection<?> c = (ObservableCollection<?>) o;
        if (c.size() != size())
            return false;

        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    public int hashCode() {
        int h = 0;
        for (S obj : this) {
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }
}
