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

package io.parsingdata.metal.data;

import io.parsingdata.metal.data.callback.TokenCallbackList;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.token.Token;

import static io.parsingdata.metal.Util.checkNotNull;

public class Environment {

    public final ParseGraph order;
    public final ByteStream input;
    public final long offset;
    public final TokenCallbackList callbacks;

    public Environment(final ParseGraph order, final ByteStream input, final long offset, final TokenCallbackList callbacks) {
        this.order = checkNotNull(order, "order");
        this.input = checkNotNull(input, "input");
        this.offset = offset;
        this.callbacks = checkNotNull(callbacks, "callbacks");
    }

    public Environment(final ParseGraph order, final ByteStream input, final long offset) {
        this(order, input, offset, TokenCallbackList.EMPTY);
    }

    public Environment(final ByteStream input, final long offset, final TokenCallbackList callbacks) {
        this(ParseGraph.EMPTY, input, offset, callbacks);
    }

    public Environment(final ByteStream input, final long offset) {
        this(ParseGraph.EMPTY, input, offset, TokenCallbackList.EMPTY);
    }

    public Environment(final ByteStream input, final TokenCallbackList callbacks) {
        this(ParseGraph.EMPTY, input, 0L, callbacks);
    }

    public Environment(final ByteStream input) {
        this(ParseGraph.EMPTY, input, 0L, TokenCallbackList.EMPTY);
    }

    public void handleCallbacks(final Token token, final ParseResult result, final Encoding encoding) {
        handleCallbacks(callbacks, token, result, encoding);
    }

    private void handleCallbacks(final TokenCallbackList callbacks, final Token token, final ParseResult result, final Encoding encoding) {
        if (callbacks.isEmpty()) {
            return;
        }
        if (callbacks.head.token == token) {
            callbacks.head.callback.handle(token, result, encoding);
        }
        handleCallbacks(callbacks.tail, token, result, encoding);
    }

    public Environment addBranch(final Token token) {
        return new Environment(order.addBranch(token), input, offset, callbacks);
    }

    public Environment closeBranch() {
        return new Environment(order.closeBranch(), input, offset, callbacks);
    }

    public Environment add(final ParseValue parseValue) {
        return new Environment(order.add(parseValue), input, offset, callbacks);
    }

    public Environment seek(final long newOffset) {
        return new Environment(order, input, newOffset, callbacks);
    }

    public Environment add(final ParseRef parseRef) {
        return new Environment(order.add(parseRef), input, offset, callbacks);
    }

    @Override
    public String toString() {
        return "stream: " + input + "; offset: " + offset + "; order: " + order + "; callbacks: " + callbacks;
    }

}
