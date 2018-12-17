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

import static io.parsingdata.metal.Util.checkNotNull;
import static io.parsingdata.metal.Util.failure;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseState;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;

/**
 * A {@link Token} that specifies a bounded repetition of a token.
 * <p>
 * A RepN consists of a <code>token</code> (a {@link Token}) and an
 * <code>n</code> (a {@link ValueExpression}). First <code>n</code> is
 * evaluated. Parsing fails if it does not evaluate to a single value. The
 * token is then parsed for an amount of times equal to the evaluated value of
 * <code>n</code>. RepN succeeds if this succeeds.
 *
 * @see Rep
 * @see ValueExpression
 */
public class RepN extends IterableToken {

    public final ValueExpression n;

    public RepN(final String name, final Token token, final ValueExpression n, final Encoding encoding) {
        super(name, token, encoding);
        this.n = checkNotNull(n, "n");
    }

    @Override
    protected Optional<ParseState> parseImpl(final Environment environment) {
        final ImmutableList<Optional<Value>> evaluatedN = n.eval(environment.parseState, environment.encoding);
        if (evaluatedN.size != 1 || !evaluatedN.head.isPresent()) {
            return failure();
        }
        final BigInteger nValue = evaluatedN.head.get().asNumeric();
        return parse(environment, env -> env.parseState.iterations.head.right.compareTo(nValue) >= 0, env -> failure());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + makeNameFragment() + token + "," + n + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj)
            && Objects.equals(n, ((RepN)obj).n);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), n);
    }

}
