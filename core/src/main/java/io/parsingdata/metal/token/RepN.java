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

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.OptionalValueList;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.ValueExpression;

import java.io.IOException;

import static io.parsingdata.metal.Util.checkNotNull;
import static io.parsingdata.metal.data.ParseResult.failure;
import static io.parsingdata.metal.data.ParseResult.success;

public class RepN extends Token {

    public final Token token;
    public final ValueExpression n;

    public RepN(final String name, final Token token, final ValueExpression n, final Encoding enc) {
        super(name, enc);
        this.token = checkNotNull(token, "token");
        this.n = checkNotNull(n, "n");
    }

    @Override
    protected ParseResult parseImpl(final String scope, final Environment env, final Encoding enc) throws IOException {
        final OptionalValueList counts = n.eval(env, enc);
        if (counts.size != 1 || !counts.head.isPresent()) {
            return failure(env);
        }
        final ParseResult res = iterate(scope, env.addBranch(this), enc, counts.head.get().asNumeric().longValue());
        if (res.succeeded) {
            return success(res.environment.closeBranch());
        }
        return failure(env);
    }

    private ParseResult iterate(final String scope, final Environment env, final Encoding enc, final long count) throws IOException {
        if (count <= 0) { return success(env); }
        final ParseResult res = token.parse(scope, env, enc);
        if (res.succeeded) { return iterate(scope, res.environment, enc, count - 1); }
        return failure(env);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + makeNameFragment() + token + "," + n + ")";
    }

}
