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

package io.parsingdata.metal.expression.value.bitwise;

import java.util.BitSet;
import java.util.Optional;

import io.parsingdata.metal.data.ParseState;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ConstantFactory;
import io.parsingdata.metal.expression.value.OneToManyValueExpression;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;

/**
 * A {@link OneToManyValueExpression} that implements the bitwise NOT operator.
 */
public class Not extends OneToManyValueExpression {

    public Not(final ValueExpression operand) {
        super(operand);
    }

    @Override
    public Optional<Value> eval(final Value value, final ParseState parseState, final Encoding encoding) {
        final BitSet bits = value.asBitSet();
        bits.flip(0, value.getValue().length * 8);
        return Optional.of(ConstantFactory.createFromBitSet(bits, value.getValue().length, encoding));
    }

}
