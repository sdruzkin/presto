/*
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
package com.facebook.presto.type;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 50, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 50, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkNumberConversion
{
    @Param("12272022")
    public long seed;

    @Param("1000000")
    public int numValues;

    private long[] values;
    private long[] positiveValues;

    @Setup
    public void setUp()
    {
        Random rnd = new Random(seed);
        values = new long[numValues];
        positiveValues = new long[numValues];
        for (int i = 0; i < numValues; i++) {
            values[i] = rnd.nextLong();
            positiveValues[i] = Math.abs(rnd.nextLong());
        }
    }

    @Benchmark
    public int benchmarkCountDigitsJvm()
    {
        int sum = 0;
        for (int i = 0; i < numValues; i++) {
            sum += stringSizeJvm(positiveValues[i]);
        }
        return sum;
    }

    @Benchmark
    public int benchmarkCountDigitsTable()
    {
        int sum = 0;
        for (int i = 0; i < numValues; i++) {
            sum += stringSizeTable(positiveValues[i]);
        }
        return sum;
    }

    public static void main(String[] args)
            throws Throwable
    {
        Options options = new OptionsBuilder()
                .verbosity(VerboseMode.NORMAL)
                .include(".*" + BenchmarkNumberConversion.class.getSimpleName() + ".*")
                .build();
        new Runner(options).run();
    }

    static int stringSizeJvm(long x)
    {
        int d = 1;
        if (x >= 0) {
            d = 0;
            x = -x;
        }
        long p = -10;
        for (int i = 1; i < 19; i++) {
            if (x > p) {
                return i + d;
            }
            p = 10 * p;
        }
        return 19 + d;
    }

    static final long table[] = {
            9,
            99,
            999,
            9999,
            99999,
            999999,
            9999999,
            99999999,
            999999999,
            9999999999L,
            99999999999L,
            999999999999L,
            9999999999999L,
            99999999999999L,
            999999999999999L,
            9999999999999999L,
            99999999999999999L,
            999999999999999999L
    };

    static int stringSizeTable(long num)
    {
        int l2 = 63 - Long.numberOfLeadingZeros(num | 1);
        int ans = ((9 * l2) >>> 5);
        if (num > table[ans]) {ans += 1;}
        return ans + 1;
    }
}
