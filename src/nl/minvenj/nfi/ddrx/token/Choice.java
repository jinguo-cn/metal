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

import nl.minvenj.nfi.ddrx.io.ByteStream;

public class Choice implements Token {
    
    private final Token _l;
    private final Token _r;
    
    public Choice(Token l, Token r) {
        _l = l;
        _r = r;
    }

    @Override
    public boolean eval(ByteStream input, Environment env) {
    	input.mark();
    	env.mark();
    	if (_l.eval(input, env)) {
    		input.clear();
    		env.clear();
    		return true;
    	} else {
    		input.reset();
    		env.reset();
    		input.mark();
    		env.mark();
    		if (_r.eval(input, env)) {
    			input.clear();
    			env.clear();
    			return true;
    		} else {
    			input.reset();
    			env.reset();
    			return false;
    		}
    	}
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + _l + "," + _r + ")";
    }
    
}
