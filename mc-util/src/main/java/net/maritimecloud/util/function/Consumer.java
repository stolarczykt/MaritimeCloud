/* Copyright (c) 2011 Danish Maritime Authority.
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
package net.maritimecloud.util.function;


/**
 * An operation upon an input object. The operation may modify that object or external state (other objects).
 * 
 * @author Kasper Nielsen
 */
public interface Consumer<T> {
    
    Consumer<Object> NOOP = new DummyConsumer();

    void accept(T t);

    // public Consumer<T> chain(final Consumer<? super T> other) {
    // requireNonNull(other);
    // return new Consumer<T>() {
    // public void accept(T t) {
    // Consumer.this.accept(t);
    // other.accept(t);
    // }
    // };
    // }
    //
    // public Consumer<T> filter(final Predicate<T> filter) {
    // requireNonNull(filter);
    // return new Consumer<T>() {
    // public void accept(T t) {
    // if (filter.test(t)) {
    // Consumer.this.accept(t);
    // }
    // }
    // };
    // }

}

class DummyConsumer implements Consumer<Object> {

    /** {@inheritDoc} */
    @Override
    public void accept(Object t) {}

}