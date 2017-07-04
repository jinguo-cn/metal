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

package io.parsingdata.metal.token;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Trampoline.complete;
import static io.parsingdata.metal.Trampoline.intermediate;
import static io.parsingdata.metal.Util.checkNotNull;
import static io.parsingdata.metal.Util.failure;
import static io.parsingdata.metal.Util.success;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import io.parsingdata.metal.Trampoline;
import io.parsingdata.metal.Util;
import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseValue;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;

public class Range extends Token {

    private final Token token;
    private final ValueExpression max;

    public Range(final String name, final Token token, final ValueExpression max, final Encoding encoding) {
        super(name, encoding);
        this.token = checkNotNull(token, "token");
        this.max = checkNotNull(max, "max");
    }

    @Override
    protected Optional<Environment> parseImpl(final String scope, final Environment environment, final Encoding encoding) throws IOException {
        final ImmutableList<Optional<Value>> maxs = max.eval(environment.order, encoding);
        if (maxs.size != 1 || !maxs.head.isPresent()) {
            return failure();
        }
        return iterate(scope, environment.addBranch(this), encoding, maxs.head.get().asNumeric().intValue(), con(1)).computeResult();
    }

    private Trampoline<Optional<Environment>> iterate(final String scope, final Environment environment, final Encoding encoding, final int max, final ValueExpression current) throws IOException {
        final ImmutableList<Optional<Value>> currents = current.eval(environment.order, encoding);
        if (currents.size != 1 || !currents.head.isPresent()) {
            return complete(Util::failure);
        }
        final Value currentValue = currents.head.get();
        if (currentValue.asNumeric().intValue() > max) {
            return complete(Util::failure);
        }
        final Environment newEnvironment = environment.add(new ParseValue(name, this, currentValue.slice, currentValue.encoding));
        return token.parse(scope, newEnvironment, encoding)
            .map(nextEnvironment -> complete(() -> success(nextEnvironment.closeBranch())))
            .orElseGet(() -> intermediate(() -> iterate(scope, environment, encoding, max, con(currentValue.asNumeric().intValue() + 1))));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + makeNameFragment() + token + "," + max + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj)
            && Objects.equals(token, ((Range)obj).token)
            && Objects.equals(max, ((Range)obj).max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, max);
    }

}
