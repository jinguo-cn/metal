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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.AutoEqualityTest.DUMMY_STREAM;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.div;
import static io.parsingdata.metal.Shorthand.exp;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.data.Slice.createFromBytes;
import static io.parsingdata.metal.util.EncodingFactory.enc;
import static io.parsingdata.metal.util.TokenDefinitions.any;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseGraph;
import io.parsingdata.metal.data.ParseValue;

public class ExpandTest {

    public static final int SIZE = 5;
    public static final int VALUE_1 = 42;
    public static final int VALUE_2 = 84;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    public static final ParseValue PARSEVALUE_1 = createParseValue("a", VALUE_1);
    public static final ParseValue PARSEVALUE_2 = createParseValue("a", VALUE_2);

    @Test
    public void expandEmpty() {
        final ImmutableList<Optional<Value>> result = exp(ref("a"), con(5)).eval(ParseGraph.EMPTY, enc());
        assertTrue(result.isEmpty());
    }

    @Test
    public void expandEmptyTimes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Count must evaluate to a single non-empty value.");
        exp(con(1), div(con(1), con(0))).eval(ParseGraph.EMPTY, enc());
    }

    @Test
    public void expandListTimes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Count must evaluate to a single non-empty value.");
        exp(con(1), ref("a")).eval(new Environment(DUMMY_STREAM).add(PARSEVALUE_1).add(PARSEVALUE_2).order, enc());
    }

    @Test
    public void expandZeroTimes() {
        final ImmutableList<Optional<Value>> result = exp(con(1), con(0)).eval(ParseGraph.EMPTY, enc());
        assertTrue(result.isEmpty());
    }

    @Test
    public void expandValue() {
        ImmutableList<Optional<Value>> result = exp(con(VALUE_1), con(SIZE)).eval(ParseGraph.EMPTY, enc());
        assertEquals(SIZE, result.size);
        for (int i = 0; i < SIZE; i++) {
            assertTrue(result.head.isPresent());
            assertEquals(VALUE_1, result.head.get().asNumeric().intValueExact());
            result = result.tail;
        }
    }

    @Test
    public void expandList() {
        ImmutableList<Optional<Value>> result = exp(ref("a"), con(SIZE)).eval(new Environment(DUMMY_STREAM).add(PARSEVALUE_2).add(PARSEVALUE_1).order, enc());
        assertEquals(2 * SIZE, result.size);
        for (int i = 0; i < SIZE; i++) {
            assertTrue(result.head.isPresent());
            assertEquals(VALUE_1, result.head.get().asNumeric().intValueExact());
            result = result.tail;
            assertTrue(result.head.isPresent());
            assertEquals(VALUE_2, result.head.get().asNumeric().intValueExact());
            result = result.tail;
        }
    }

    public static ParseValue createParseValue(String name, int value) {
        return new ParseValue(name, any(name), createFromBytes(new byte[] { (byte)value }), enc());
    }

}
