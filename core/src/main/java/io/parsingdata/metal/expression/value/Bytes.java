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

package io.parsingdata.metal.expression.value;

import static io.parsingdata.metal.Util.checkNotNull;
import static io.parsingdata.metal.Util.createFromBytes;
import static io.parsingdata.metal.data.transformation.Reversal.reverse;

import java.util.Optional;

import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseGraph;
import io.parsingdata.metal.encoding.Encoding;

public class Bytes implements ValueExpression {

    public final ValueExpression operand;

    public Bytes(final ValueExpression operand) {
        this.operand = checkNotNull(operand, "operand");
    }

    @Override
    public ImmutableList<Optional<Value>> eval(ParseGraph graph, Encoding encoding) {
        final ImmutableList<Optional<Value>> input = operand.eval(graph, encoding);
        if (input.isEmpty()) { return input; }
        return reverse(toBytes(new ImmutableList<>(), input.head, input.tail, encoding));
    }

    private ImmutableList<Optional<Value>> toBytes(final ImmutableList<Optional<Value>> output, final Optional<Value> value, final ImmutableList<Optional<Value>> backlog, final Encoding encoding) {
        final ImmutableList<Optional<Value>> result = output.add(toBytes(value, encoding));
        return backlog.isEmpty() ? result : toBytes(result, backlog.head, backlog.tail, encoding);
    }

    private ImmutableList<Optional<Value>> toBytes(final Optional<Value> value, final Encoding encoding) {
        if (!value.isPresent()) { return new ImmutableList<>(); }
        return byteToValue(new ImmutableList<>(), value.get().getValue(), 0, encoding);
    }

    private ImmutableList<Optional<Value>> byteToValue(final ImmutableList<Optional<Value>> output, final byte[] value, final int i, final Encoding encoding) {
        if (i >= value.length) { return output; }
        return byteToValue(output.add(Optional.of(new Value(createFromBytes(new byte[] { value[i] }), encoding))), value, i + 1, encoding);
    }

}
