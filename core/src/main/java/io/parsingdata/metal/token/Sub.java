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

import static io.parsingdata.metal.Trampoline.complete;
import static io.parsingdata.metal.Trampoline.intermediate;
import static io.parsingdata.metal.Util.checkNotNull;
import static io.parsingdata.metal.Util.failure;
import static io.parsingdata.metal.Util.success;
import static io.parsingdata.metal.data.Selection.hasRootAtOffset;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import io.parsingdata.metal.Trampoline;
import io.parsingdata.metal.Util;
import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseReference;
import io.parsingdata.metal.data.ParseState;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;

/**
 * A {@link Token} that specifies a token to be parsed at specific locations
 * in the input.
 * <p>
 * A Sub consists of a <code>token</code> (a {@link Token}) and an
 * <code>offsets</code> (a {@link ValueExpression}). First
 * <code>offsets</code> is evaluated. Then each resulting value is used as a
 * location in the input to parse <code>token</code> at. Sub succeeds if all
 * parses of <code>token</code> at all locations succeed. Sub fails if
 * <code>offsets</code> evaluates to a list of locations that is either empty
 * or contains an invalid value.
 *
 * @see ValueExpression
 */
public class Sub extends Token {

    public final Token token;
    public final ValueExpression offsets;

    public Sub(final String name, final Token token, final ValueExpression offsets, final Encoding encoding) {
        super(name, encoding);
        this.token = checkNotNull(token, "token");
        this.offsets = checkNotNull(offsets, "offsets");
    }

    @Override
    protected Optional<ParseState> parseImpl(final Environment environment) {
        final ImmutableList<Optional<Value>> evaluatedOffsets = offsets.eval(environment.parseState, environment.encoding);
        if (evaluatedOffsets.isEmpty()) {
            return failure();
        }
        return iterate(environment.addBranch(this), evaluatedOffsets)
            .computeResult()
            .flatMap(nextParseState -> nextParseState.seek(environment.parseState.offset));
    }

    private Trampoline<Optional<ParseState>> iterate(final Environment environment, final ImmutableList<Optional<Value>> offsetValues) {
        if (offsetValues.isEmpty()) {
            return complete(() -> success(environment.parseState.closeBranch(this)));
        }
        return offsetValues.head
            .flatMap(offsetValue -> parse(environment, offsetValue.asNumeric()))
            .map(nextParseState -> intermediate(() -> iterate(environment.withParseState(nextParseState), offsetValues.tail)))
            .orElseGet(() -> complete(Util::failure));
    }

    private Optional<ParseState> parse(final Environment environment, final BigInteger offsetValue) {
        if (hasRootAtOffset(environment.parseState.order, token.getCanonical(environment.parseState), offsetValue, environment.parseState.source)) {
            return success(environment.parseState.add(new ParseReference(offsetValue, environment.parseState.source, token.getCanonical(environment.parseState))));
        }
        return environment.parseState
            .seek(offsetValue)
            .map(newParseState -> token.parse(environment.withParseState(newParseState)))
            .orElseGet(Util::failure);
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + makeNameFragment() + token + "," + offsets + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj)
            && Objects.equals(token, ((Sub)obj).token)
            && Objects.equals(offsets, ((Sub)obj).offsets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, offsets);
    }

}
