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

package io.parsingdata.metal.data.selection;

import static io.parsingdata.metal.data.ParseGraph.EMPTY;
import static io.parsingdata.metal.data.ParseGraph.NONE;
import static io.parsingdata.metal.data.selection.ByType.getReferences;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.parsingdata.metal.data.ParseReference;
import io.parsingdata.metal.data.Source;

public class ByTypeTest {

    public static final Source EMPTY_SOURCE = new Source() {
        @Override protected byte[] getData(BigInteger offset, BigInteger length) { throw new IllegalStateException(); }
        @Override protected boolean isAvailable(BigInteger offset, BigInteger length) { return false; }
    };

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void unresolvableRef() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("A ParseReference must point to an existing graph.");
        getReferences(EMPTY.add(new ParseReference(BigInteger.ZERO, EMPTY_SOURCE, NONE)));
    }

}
