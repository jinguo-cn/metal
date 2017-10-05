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

package io.parsingdata.metal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.CURRENT_OFFSET;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.rep;
import static io.parsingdata.metal.Shorthand.SELF;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.util.EncodingFactory.enc;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.encoding.Sign;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.token.Token;
import io.parsingdata.metal.util.InMemoryByteStream;

public class CurrentOffsetTest {

    private void checkCurrentOffset(final int size) throws IOException {
        final byte[] data = new byte[size];
        final Optional<Environment> result = def("a", con(size)).parse(new Environment(new InMemoryByteStream(data)), enc());
        assertTrue(result.isPresent());

        final ImmutableList<Optional<Value>> offset = CURRENT_OFFSET.eval(result.get().order, enc());

        assertNotNull(offset);
        assertEquals(1, offset.size);
        assertEquals(size, offset.head.get().asNumeric().longValue());
    }

    @Test
    public void currentOffset() throws IOException {
        checkCurrentOffset(42);
    }

    @Test
    public void currentOffsetLarger() throws IOException {
        // offset would flip signed bit if interpreted as signed integer:
        checkCurrentOffset(128);
    }

    @Test
    public void currentOffsetInCalculations() throws IOException {
        final byte[] stream = new byte[256];
        for (int i = 0; i < stream.length; i++) {
            stream[i] = (byte) i;
        }
        final Environment environment = new Environment(new InMemoryByteStream(stream));

        // value - offset + 1 should be 0:
        final Token offsetValidation = rep(def("byte", con(1), eqNum(sub(SELF, sub(CURRENT_OFFSET, con(1))), con(0))));

        final Optional<Environment> result = offsetValidation.parse(environment, new Encoding(Sign.UNSIGNED));
        assertTrue(result.isPresent());
        assertEquals(256, result.get().offset.intValue());
    }

}
