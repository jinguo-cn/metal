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

import static io.parsingdata.metal.Shorthand.CURRENT_ITERATION;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.rep;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.whl;
import static io.parsingdata.metal.util.EncodingFactory.enc;
import static io.parsingdata.metal.util.ParseStateFactory.stream;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;

import io.parsingdata.metal.token.Token;
import io.parsingdata.metal.util.ParameterizedParse;

public class CurrentIterationTest extends ParameterizedParse {

    public static final Token VALUE_EQ_ITERATION = def("value", con(1), eq(CURRENT_ITERATION));
    public static final Token VALUE_EQ_255 = def("value", con(1), eq(con(255)));

    @Parameterized.Parameters(name="{0} ({4})")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "[0, 1, 2, 3, 255] rep(CURRENT_ITERATION), def(255)", seq(rep(VALUE_EQ_ITERATION), VALUE_EQ_255), stream(0, 1, 2, 3, 255), enc(), true },
            { "[0, 1, 2, 3] repn=4(CURRENT_ITERATION)", repn(VALUE_EQ_ITERATION, con(4)), stream(0, 1, 2, 3), enc(), true },
            { "[255, 0, 1, 2, 3, 255] def(255), while<3(CURRENT_ITERATION), def (255)", seq(VALUE_EQ_255, whl(VALUE_EQ_ITERATION, not(eq(con(3)))), VALUE_EQ_255), stream(255, 0, 1, 2, 3, 255), enc(), true },
            { "[0, 0, 1, 2, 1, 0, 1, 2] repn=2(CURRENT_ITERATION, repn=3(CURRENT_ITERATION))", repn(seq(VALUE_EQ_ITERATION, repn(VALUE_EQ_ITERATION, con(3))), con(2)), stream(0, 0, 1, 2, 1, 0, 1, 2), enc(), true },
            { "[0, 0] seq(CURRENT_ITERATION, ...)", seq(VALUE_EQ_ITERATION, VALUE_EQ_ITERATION), stream(0, 0), enc(), true },
            { "[0] CURRENT_ITERATION", VALUE_EQ_ITERATION, stream(0), enc(), true }
        });
    }

}