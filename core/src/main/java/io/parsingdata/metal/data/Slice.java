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

import static io.parsingdata.metal.Util.checkNotNull;

public class Slice {

    public final Source source;
    public final long offset;
    private final byte[] data; // Private because array contents is mutable.
    public final int size;

    public Slice(final Source source, final long offset, final byte[] data) {
        this.source = checkNotNull(source, "source");
        this.offset = offset;
        this.data = checkNotNull(data, "data");
        this.size = data.length;
    }

    public byte[] getData() {
        return data.clone();
    }

    @Override
    public String toString() {
        return source + "@" + offset + ":" + (offset+size);
    }
}
