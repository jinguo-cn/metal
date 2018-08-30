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

import java.util.Optional;

import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseState;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.encoding.Sign;
import io.parsingdata.metal.expression.value.ConstantFactory;
import io.parsingdata.metal.expression.value.OneToOneValueExpression;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;

/**
 * A {@link ValueExpression} that represents the amount of {@link Value}s
 * returned by evaluating its <code>operand</code>.
 */
public class Count extends OneToOneValueExpression {

    public Count(final ValueExpression operand) {
        super(operand);
    }

    @Override
    public Optional<Value> eval(final ImmutableList<Optional<Value>> list, final ParseState parseState, final Encoding encoding) {
        return Optional.of(fromNumeric(list.size));
    }

    private static Value fromNumeric(final long length) {
        return ConstantFactory.createFromNumeric(length, new Encoding(Sign.SIGNED));
    }

}
