/*
 * Copyright 2013-2016 Netherlands Forensic Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.parsingdata.metal.expression.value.reference;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import static io.parsingdata.metal.Trampoline.complete;
import static io.parsingdata.metal.Trampoline.intermediate;
import static io.parsingdata.metal.Util.checkNotNull;
import static io.parsingdata.metal.data.Selection.reverse;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import io.parsingdata.metal.Trampoline;
import io.parsingdata.metal.Util;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseState;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;

/**
 * A {@link ValueExpression} that returns an indexed list of {@link Value}s.
 * <p>
 * The Nth ValueExpression has two operands, <code>values</code> and
 * <code>indices</code> (both {@link ValueExpression}s). Both operands are
 * evaluated. Next, the resulting values of evaluating <code>indices</code> is
 * used as a list of integer indices into the results of evaluating
 * <code>values</code>. For every invalid index (such as
 * {@link Optional#empty()}, a negative value or an index that is out of
 * bounds) empty is returned.
 */
public class Nth implements ValueExpression {

    public final ValueExpression values;
    public final ValueExpression indices;

    public Nth(final ValueExpression values, final ValueExpression indices) {
        this.values = checkNotNull(values, "values");
        this.indices = checkNotNull(indices, "indices");
    }

    @Override
    public ImmutableList<Optional<Value>> eval(final ParseState parseState, final Encoding encoding) {
        return reverse(eval(values.eval(parseState, encoding), indices.eval(parseState, encoding), new ImmutableList<>()).computeResult());
    }

    private Trampoline<ImmutableList<Optional<Value>>> eval(final ImmutableList<Optional<Value>> values, final ImmutableList<Optional<Value>> indicesValues, final ImmutableList<Optional<Value>> result) {
        if (indicesValues.isEmpty()) {
            return complete(() -> result);
        }
        final BigInteger valueCount = BigInteger.valueOf(values.size);
        final Optional<Value> nextResult = indicesValues.head
            .filter(index -> index.asNumeric().compareTo(valueCount) < 0 && index.asNumeric().compareTo(ZERO) >= 0)
            .flatMap(index -> nth(values, valueCount.subtract(index.asNumeric()).subtract(ONE)).computeResult());
        return intermediate(() -> eval(values, indicesValues.tail, result.add(nextResult)));
    }

    private Trampoline<Optional<Value>> nth(final ImmutableList<Optional<Value>> values, final BigInteger indexValue) {
        if (indexValue.equals(ZERO)) {
            return complete(() -> values.head);
        }
        return intermediate(() -> nth(values.tail, indexValue.subtract(ONE)));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + values + "," + indices + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        return Util.notNullAndSameClass(this, obj)
            && Objects.equals(values, ((Nth)obj).values)
            && Objects.equals(indices, ((Nth)obj).indices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), values, indices);
    }

}
