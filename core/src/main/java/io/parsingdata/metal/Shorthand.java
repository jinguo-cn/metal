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

import static io.parsingdata.metal.data.Slice.createFromBytes;
import static io.parsingdata.metal.encoding.Encoding.DEFAULT_ENCODING;
import static io.parsingdata.metal.token.Token.EMPTY_NAME;
import static io.parsingdata.metal.token.Token.NO_NAME;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.True;
import io.parsingdata.metal.expression.comparison.ComparisonExpression;
import io.parsingdata.metal.expression.comparison.Eq;
import io.parsingdata.metal.expression.comparison.EqNum;
import io.parsingdata.metal.expression.comparison.EqStr;
import io.parsingdata.metal.expression.comparison.GtEqNum;
import io.parsingdata.metal.expression.comparison.GtNum;
import io.parsingdata.metal.expression.comparison.LtEqNum;
import io.parsingdata.metal.expression.comparison.LtNum;
import io.parsingdata.metal.expression.logical.And;
import io.parsingdata.metal.expression.logical.BinaryLogicalExpression;
import io.parsingdata.metal.expression.logical.Not;
import io.parsingdata.metal.expression.logical.Or;
import io.parsingdata.metal.expression.logical.UnaryLogicalExpression;
import io.parsingdata.metal.expression.value.BinaryValueExpression;
import io.parsingdata.metal.expression.value.Bytes;
import io.parsingdata.metal.expression.value.Cat;
import io.parsingdata.metal.expression.value.Const;
import io.parsingdata.metal.expression.value.ConstantFactory;
import io.parsingdata.metal.expression.value.Elvis;
import io.parsingdata.metal.expression.value.Expand;
import io.parsingdata.metal.expression.value.FoldCat;
import io.parsingdata.metal.expression.value.FoldLeft;
import io.parsingdata.metal.expression.value.FoldRight;
import io.parsingdata.metal.expression.value.Reverse;
import io.parsingdata.metal.expression.value.UnaryValueExpression;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.expression.value.arithmetic.Add;
import io.parsingdata.metal.expression.value.arithmetic.Div;
import io.parsingdata.metal.expression.value.arithmetic.Mod;
import io.parsingdata.metal.expression.value.arithmetic.Mul;
import io.parsingdata.metal.expression.value.arithmetic.Neg;
import io.parsingdata.metal.expression.value.arithmetic.Sub;
import io.parsingdata.metal.expression.value.bitwise.ShiftLeft;
import io.parsingdata.metal.expression.value.bitwise.ShiftRight;
import io.parsingdata.metal.expression.value.reference.Count;
import io.parsingdata.metal.expression.value.reference.CurrentIteration;
import io.parsingdata.metal.expression.value.reference.CurrentOffset;
import io.parsingdata.metal.expression.value.reference.First;
import io.parsingdata.metal.expression.value.reference.Last;
import io.parsingdata.metal.expression.value.reference.Len;
import io.parsingdata.metal.expression.value.reference.Nth;
import io.parsingdata.metal.expression.value.reference.Offset;
import io.parsingdata.metal.expression.value.reference.Ref.DefinitionRef;
import io.parsingdata.metal.expression.value.reference.Ref.NameRef;
import io.parsingdata.metal.expression.value.reference.Self;
import io.parsingdata.metal.token.Cho;
import io.parsingdata.metal.token.Def;
import io.parsingdata.metal.token.Post;
import io.parsingdata.metal.token.Pre;
import io.parsingdata.metal.token.Rep;
import io.parsingdata.metal.token.RepN;
import io.parsingdata.metal.token.Seq;
import io.parsingdata.metal.token.Tie;
import io.parsingdata.metal.token.Token;
import io.parsingdata.metal.token.TokenRef;
import io.parsingdata.metal.token.Until;
import io.parsingdata.metal.token.While;

public final class Shorthand {

    public static final Token EMPTY = def(EMPTY_NAME, 0L);
    public static final ValueExpression SELF = new Self();
    public static final ValueExpression CURRENT_OFFSET = new CurrentOffset();
    public static final ValueExpression CURRENT_ITERATION = new CurrentIteration(con(0));
    public static final Expression TRUE = new True();

    private Shorthand() {}

    public static Token def(final String name, final ValueExpression size, final Expression predicate, final Encoding encoding) { return post(def(name, size, encoding), predicate); }
    public static Token def(final String name, final ValueExpression size, final Expression predicate) { return def(name, size, predicate, null); }
    public static Token def(final String name, final ValueExpression size, final Encoding encoding) { return new Def(name, size, encoding); }
    public static Token def(final String name, final ValueExpression size) { return def(name, size, (Encoding)null); }
    public static Token def(final String name, final long size, final Expression predicate, final Encoding encoding) { return def(name, con(size), predicate, encoding); }
    public static Token def(final String name, final long size, final Expression predicate) { return def(name, size, predicate, null); }
    public static Token def(final String name, final long size, final Encoding encoding) { return def(name, con(size), encoding); }
    public static Token def(final String name, final long size) { return def(name, size, (Encoding)null); }
    public static Token nod(final ValueExpression size) { return def(EMPTY_NAME, size); }
    public static Token nod(final long size) { return nod(con(size)); }
    public static Token cho(final String name, final Encoding encoding, final Token token1, final Token token2, final Token... tokens) { return new Cho(name, encoding, token1, token2, tokens); }
    public static Token cho(final String name, final Token token1, final Token token2, final Token... tokens) { return cho(name, null, token1, token2, tokens); }
    public static Token cho(final Encoding encoding, final Token token1, final Token token2, final Token... tokens) { return cho(NO_NAME, encoding, token1, token2, tokens); }
    public static Token cho(final Token token1, final Token token2, final Token... tokens) { return cho((Encoding)null, token1, token2, tokens); }
    public static Token rep(final String name, final Token token, final Encoding encoding) { return new Rep(name, token, encoding); }
    public static Token rep(final String name, final Token token) { return rep(name, token, null); }
    public static Token rep(final Token token, final Encoding encoding) { return rep(NO_NAME, token, encoding); }
    public static Token rep(final Token token) { return rep(token, null); }
    public static Token repn(final String name, final Token token, final ValueExpression n, final Encoding encoding) { return new RepN(name, token, n, encoding); }
    public static Token repn(final String name, final Token token, final ValueExpression n) { return repn(name, token, n, null); }
    public static Token repn(final Token token, final ValueExpression n, final Encoding encoding) { return repn(NO_NAME, token, n, encoding); }
    public static Token repn(final Token token, final ValueExpression n) { return repn(token, n, null); }
    public static Token seq(final String name, final Encoding encoding, final Token token1, final Token token2, final Token... tokens) { return new Seq(name, encoding, token1, token2, tokens); }
    public static Token seq(final String name, final Token token1, final Token token2, final Token... tokens) { return seq(name, null, token1, token2, tokens); }
    public static Token seq(final Encoding encoding, final Token token1, final Token token2, final Token... tokens) { return seq(NO_NAME, encoding, token1, token2, tokens); }
    public static Token seq(final Token token1, final Token token2, final Token... tokens) { return seq((Encoding)null, token1, token2, tokens); }
    public static Token sub(final String name, final Token token, final ValueExpression address, final Encoding encoding) { return new io.parsingdata.metal.token.Sub(name, token, address, encoding); }
    public static Token sub(final String name, final Token token, final ValueExpression address) { return sub(name, token, address, null); }
    public static Token sub(final Token token, final ValueExpression address, final Encoding encoding) { return sub(NO_NAME, token, address, encoding); }
    public static Token sub(final Token token, final ValueExpression address) { return sub(token, address, null); }
    public static Token pre(final String name, final Token token, final Expression predicate, final Encoding encoding) { return new Pre(name, token, predicate, encoding); }
    public static Token pre(final String name, final Token token, final Expression predicate) { return pre(name, token, predicate, null); }
    public static Token pre(final Token token, final Expression predicate, final Encoding encoding) { return pre(NO_NAME, token, predicate, encoding); }
    public static Token pre(final Token token, final Expression predicate) { return pre(token, predicate, null); }
    public static Token post(final String name, final Token token, final Expression predicate, final Encoding encoding) { return new Post(name, token, predicate, encoding); }
    public static Token post(final String name, final Token token, final Expression predicate) { return post(name, token, predicate, null); }
    public static Token post(final Token token, final Expression predicate, final Encoding encoding) { return post(NO_NAME, token, predicate, encoding); }
    public static Token post(final Token token, final Expression predicate) { return post(token, predicate, null); }
    public static Token whl(final String name, final Token token, final Expression predicate, final Encoding encoding) { return new While(name, token, predicate, encoding); }
    public static Token whl(final String name, final Token token, final Expression predicate) { return whl(name, token, predicate, null); }
    public static Token whl(final Token token, final Expression predicate, final Encoding encoding) { return whl(NO_NAME, token, predicate, encoding); }
    public static Token whl(final Token token, final Expression predicate) { return whl(NO_NAME, token, predicate); }
    public static Token opt(final String name, final Token token, final Encoding encoding) { return cho(name, encoding, token, EMPTY); }
    public static Token opt(final String name, final Token token) { return opt(name, token, null); }
    public static Token opt(final Token token, final Encoding encoding) { return opt(NO_NAME, token, encoding); }
    public static Token opt(final Token token) { return opt(token, null); }
    public static Token token(final String tokenName) { return new TokenRef(NO_NAME, tokenName, null); }
    public static Token tie(final String name, final Token token, final ValueExpression dataExpression, final Encoding encoding) { return new Tie(name, token, dataExpression, encoding); }
    public static Token tie(final String name, final Token token, final ValueExpression dataExpression) { return tie(name, token, dataExpression, null); }
    public static Token tie(final Token token, final ValueExpression dataExpression, final Encoding encoding) { return tie(NO_NAME, token, dataExpression, encoding); }
    public static Token tie(final Token token, final ValueExpression dataExpression) { return tie(token, dataExpression, null); }
    public static Token until(final String name, final ValueExpression initialSize, final ValueExpression stepSize, final ValueExpression maxSize, final Token terminator, final Encoding encoding) { return new Until(name, initialSize, stepSize, maxSize, terminator, encoding); }
    public static Token until(final String name, final ValueExpression initialSize, final ValueExpression stepSize, final ValueExpression maxSize, final Token terminator) { return until(name, initialSize, stepSize, maxSize, terminator, null); }
    public static Token until(final String name, final ValueExpression initialSize, final ValueExpression stepSize, final Token terminator, final Encoding encoding) { return until(name, initialSize, stepSize, null, terminator, encoding); }
    public static Token until(final String name, final ValueExpression initialSize, final ValueExpression stepSize, final Token terminator) { return until(name, initialSize, stepSize, null, terminator, null); }
    public static Token until(final String name, final ValueExpression initialSize, final Token terminator, final Encoding encoding) { return until(name, initialSize, null, terminator, encoding); }
    public static Token until(final String name, final ValueExpression initialSize, final Token terminator) { return until(name, initialSize, null, terminator, null); }
    public static Token until(final String name, final Token terminator, final Encoding encoding) { return until(name, null, terminator, encoding); }
    public static Token until(final String name, final Token terminator) { return until(name, terminator, null); }
    public static Token when(final String name, final Token token, final Expression predicate, final Encoding encoding) { return cho(name, encoding, pre(def(EMPTY_NAME, 0), not(predicate)), token); }
    public static Token when(final String name, final Token token, final Expression predicate) { return when(name, token, predicate, null); }
    public static Token when(final Token token, final Expression predicate, final Encoding encoding) { return when(EMPTY_NAME, token, predicate, encoding); }
    public static Token when(final Token token, final Expression predicate) { return when(token, predicate, null); }

    public static BinaryValueExpression add(final ValueExpression left, final ValueExpression right) { return new Add(left, right); }
    public static BinaryValueExpression div(final ValueExpression left, final ValueExpression right) { return new Div(left, right); }
    public static BinaryValueExpression mul(final ValueExpression left, final ValueExpression right) { return new Mul(left, right); }
    public static BinaryValueExpression sub(final ValueExpression left, final ValueExpression right) { return new Sub(left, right); }
    public static BinaryValueExpression mod(final ValueExpression left, final ValueExpression right) { return new Mod(left, right); }
    public static UnaryValueExpression neg(final ValueExpression operand) { return new Neg(operand); }
    public static BinaryValueExpression and(final ValueExpression left, final ValueExpression right) { return new io.parsingdata.metal.expression.value.bitwise.And(left, right); }
    public static BinaryValueExpression or(final ValueExpression left, final ValueExpression right) { return new io.parsingdata.metal.expression.value.bitwise.Or(left, right); }
    public static UnaryValueExpression not(final ValueExpression operand) { return new io.parsingdata.metal.expression.value.bitwise.Not(operand); }
    public static BinaryValueExpression shl(final ValueExpression left, final ValueExpression right) { return new ShiftLeft(left, right); }
    public static BinaryValueExpression shr(final ValueExpression left, final ValueExpression right) { return new ShiftRight(left, right); }
    public static ValueExpression con(final long value) { return con(value, DEFAULT_ENCODING); }
    public static ValueExpression con(final long value, final Encoding encoding) { return con(ConstantFactory.createFromNumeric(value, encoding)); }
    public static ValueExpression con(final String value) { return con(value, DEFAULT_ENCODING); }
    public static ValueExpression con(final String value, final Encoding encoding) { return con(ConstantFactory.createFromString(value, encoding)); }
    public static ValueExpression con(final Value value) { return new Const(value); }
    public static ValueExpression con(final Encoding encoding, final int... values) { return new Const(new Value(createFromBytes(toByteArray(values)), encoding)); }
    public static ValueExpression con(final int... values) { return con(DEFAULT_ENCODING, values); }
    public static ValueExpression con(final byte[] value) { return con(value, DEFAULT_ENCODING); }
    public static ValueExpression con(final byte[] value, final Encoding encoding) { return con(ConstantFactory.createFromBytes(value, encoding)); }
    public static ValueExpression len(final ValueExpression operand) { return new Len(operand); }
    public static NameRef ref(final String name) { return ref(name, null); }
    public static NameRef ref(final String name, final ValueExpression limit) { return new NameRef(name, limit); }
    public static DefinitionRef ref(final Token definition) { return ref(definition, null); }
    public static DefinitionRef ref(final Token definition, final ValueExpression limit) { return new DefinitionRef(definition, limit); }
    public static ValueExpression first(final ValueExpression operand) { return new First(operand); }
    public static ValueExpression last(final ValueExpression operand) { return new Last(operand); }
    public static ValueExpression last(final NameRef operand) { return new Last(new NameRef(operand.reference, con(1))); }
    public static ValueExpression last(final DefinitionRef operand) { return new Last(new DefinitionRef(operand.reference, con(1))); }
    public static ValueExpression nth(final ValueExpression values, final ValueExpression indices) { return new Nth(values, indices); }
    public static ValueExpression offset(final ValueExpression operand) { return new Offset(operand); }
    public static ValueExpression iteration(final int level) { return iteration(con(level)); }
    public static ValueExpression iteration(final ValueExpression level) { return new CurrentIteration(level); }
    public static ValueExpression cat(final ValueExpression left, final ValueExpression right) { return new Cat(left, right); }
    public static ValueExpression cat(final ValueExpression operand) { return new FoldCat(operand); }
    public static ValueExpression elvis(final ValueExpression left, final ValueExpression right) { return new Elvis(left, right); }
    public static ValueExpression count(final ValueExpression operand) { return new Count(operand); }
    public static ValueExpression foldLeft(final ValueExpression values, final BinaryOperator<ValueExpression> reducer) { return new FoldLeft(values, reducer, null); }
    public static ValueExpression foldLeft(final ValueExpression values, final BinaryOperator<ValueExpression> reducer, final ValueExpression initial) { return new FoldLeft(values, reducer, initial); }
    public static ValueExpression foldRight(final ValueExpression values, final BinaryOperator<ValueExpression> reducer) { return new FoldRight(values, reducer, null); }
    public static ValueExpression foldRight(final ValueExpression values, final BinaryOperator<ValueExpression> reducer, final ValueExpression initial) { return new FoldRight(values, reducer, initial); }
    public static ValueExpression fold(final ValueExpression values, final BinaryOperator<ValueExpression> reducer) { return foldRight(values, reducer); }
    public static ValueExpression fold(final ValueExpression values, final BinaryOperator<ValueExpression> reducer, final ValueExpression initial) { return foldRight(values, reducer, initial); }
    public static ValueExpression rev(final ValueExpression values) { return new Reverse(values); }
    public static ValueExpression exp(final ValueExpression base, final ValueExpression count) { return new Expand(base, count); }
    public static BinaryValueExpression mapLeft(final BiFunction<ValueExpression, ValueExpression, BinaryValueExpression> func, final ValueExpression left, final ValueExpression rightExpand) { return func.apply(left, exp(rightExpand, count(left))); }
    public static BinaryValueExpression mapRight(final BiFunction<ValueExpression, ValueExpression, BinaryValueExpression> func, final ValueExpression leftExpand, final ValueExpression right) { return func.apply(exp(leftExpand, count(right)), right); }
    public static ValueExpression bytes(final ValueExpression operand) { return new Bytes(operand); }

    public static BinaryLogicalExpression and(final Expression left, final Expression right) { return new And(left, right); }
    public static BinaryLogicalExpression or(final Expression left, final Expression right) { return new Or(left, right); }
    public static UnaryLogicalExpression not(final Expression operand) { return new Not(operand); }

    public static ComparisonExpression eq(final ValueExpression predicate) { return new Eq(null, predicate); }
    public static ComparisonExpression eq(final ValueExpression value, final ValueExpression predicate) { return new Eq(value, predicate); }
    public static ComparisonExpression eqStr(final ValueExpression predicate) { return new EqStr(null, predicate); }
    public static ComparisonExpression eqStr(final ValueExpression value, final ValueExpression predicate) { return new EqStr(value, predicate); }
    public static ComparisonExpression eqNum(final ValueExpression predicate) { return new EqNum(null, predicate); }
    public static ComparisonExpression eqNum(final ValueExpression value, final ValueExpression predicate) { return new EqNum(value, predicate); }
    public static ComparisonExpression gtEqNum(final ValueExpression predicate) { return new GtEqNum(null, predicate); }
    public static ComparisonExpression gtEqNum(final ValueExpression value, final ValueExpression predicate) { return new GtEqNum(value, predicate); }
    public static ComparisonExpression gtNum(final ValueExpression predicate) { return new GtNum(null, predicate); }
    public static ComparisonExpression gtNum(final ValueExpression value, final ValueExpression predicate) { return new GtNum(value, predicate); }
    public static ComparisonExpression ltEqNum(final ValueExpression predicate) { return new LtEqNum(null, predicate); }
    public static ComparisonExpression ltEqNum(final ValueExpression value, final ValueExpression predicate) { return new LtEqNum(value, predicate); }
    public static ComparisonExpression ltNum(final ValueExpression predicate) { return new LtNum(null, predicate); }
    public static ComparisonExpression ltNum(final ValueExpression value, final ValueExpression predicate) { return new LtNum(value, predicate); }

    public static byte[] toByteArray(final int... bytes) {
        final byte[] outBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            outBytes[i] = (byte) bytes[i];
        }
        return outBytes;
    }

}
