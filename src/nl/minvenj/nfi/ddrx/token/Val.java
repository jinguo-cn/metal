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

package nl.minvenj.nfi.ddrx.token;

import nl.minvenj.nfi.ddrx.data.Environment;
import nl.minvenj.nfi.ddrx.expression.Expression;
import nl.minvenj.nfi.ddrx.expression.value.NumericValue;
import nl.minvenj.nfi.ddrx.expression.value.Value;
import nl.minvenj.nfi.ddrx.expression.value.ValueExpression;

public class Val<T extends Value> implements Token {

    private final String _name;
    private final ValueExpression<NumericValue> _size;
    private final Expression _pred;
    private final Class<T> _type;
    
    public Val(String name, ValueExpression<NumericValue> size, Expression pred, Class<T> type) {
        _name = name;
        _size = size;
        _pred = pred;
        _type = type;
    }
    
    @Override
    public boolean parse(Environment env) {
        int size = _size.eval(env).toBigInteger().intValue();
        byte[] data = new byte[size];
        env.mark();
        try {
            if (env.read(data) != size) {
                env.reset();
                return false;
            }
            T value = _type.getConstructor(new Class<?>[] { String.class, byte[].class }).newInstance(new Object[] {_name, data });
            env.put(value);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
        if (_pred.eval(env)) {
            env.clear();
            return true;
        } else {
            env.reset();
            return false;
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(\"" + _name + "\"," + _size + "," + _pred + ",)";
    }

}