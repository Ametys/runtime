/*
 *  Copyright 2016 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ametys.core.util;

import java.util.function.Function;

/**
 * Helper for lambda expressions.
 */
public final class LambdaUtils
{
    private LambdaUtils()
    {
        // empty constructor
    }
    
    /**
     * Function allowed to throw checked {@link Exception}. <br>
     * It allows to build one-line lambda for methods throwing checked exceptions.
     * @param <T> the type of the input to the function.
     * @param <R> the type of the result of the function.
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R>
    {
        /**
         * Applies this function to the given argument.
         * @param t the function argument
         * @return the function result
         * @throws Exception if something wrong occurs.
         */
        R apply(T t) throws Exception;
    }
    
    /**
     * Wraps a {@link Function} by catching its {@link Exception} and rethrowing them as {@link RuntimeException}.
     * @param function the {@link Function} to wrap.
     * @return the wrapped {@link Function}.
     */
    public static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> function)
    {
        return value -> {
            try
            {
                return function.apply(value);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        };
    }
}
