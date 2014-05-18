/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 * Email: subchen@gmail.com
 * URL: http://subchen.github.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.dao.id;

public class SequenceId {
    public static final int NOT_FOUND = 0;
    private static final int CACHE_SIZE = 50;
    private final SequenceIdProvider provider;
    private final String name;
    private final int beginValue;
    private int value;

    protected SequenceId(SequenceIdProvider provider, String name, int beginValue) {
        this.provider = provider;
        this.name = name;
        this.beginValue = beginValue;
        this.value = -1;

        if (beginValue <= 0) {
            throw new IllegalArgumentException("begin value must be great than zero.");
        }
    }

    public String getName() {
        return name;
    }

    public synchronized int nextVal() {
        if (value < 0) {
            value = provider.load(name);
            if (value <= NOT_FOUND) {
                value = beginValue - 1;
            }
            provider.store(name, value + CACHE_SIZE);
        }

        value++;

        if (value % CACHE_SIZE == 0) {
            provider.store(name, value + CACHE_SIZE);
        }

        return value;
    }
}
