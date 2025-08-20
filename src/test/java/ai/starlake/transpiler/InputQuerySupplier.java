/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI (hayssam.saleh@starlake.ai)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler;

import org.junit.jupiter.params.provider.Arguments;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class InputQuerySupplier implements Supplier<Arguments> {
  private final File inputFile;
  private final BufferedReader reader;
  private static final StringBuilder INPUT_BUILDER = new StringBuilder();
  private final AtomicInteger emittedElements = new AtomicInteger();

  public InputQuerySupplier(File inputFile) throws FileNotFoundException {
    this.inputFile = inputFile;
    this.reader = new BufferedReader(new FileReader(inputFile));
  }

  @Override
  public Arguments get() {
    String line;
    while (true) {
      try {
        if (!((line = reader.readLine()) != null)) {
          break;
        }
      } catch (IOException e) {
        closeReader();
        throw new RuntimeException(e);
      }
      if (line.trim().isEmpty()) {
        // Process the accumulated input and reset
        if (INPUT_BUILDER.length() > 0) {
          Arguments args = produceArgs(INPUT_BUILDER.toString());
          INPUT_BUILDER.setLength(0);
          return args;
        }
      } else {
        INPUT_BUILDER.append(line).append("\n");
      }
    }
    // Handle any remaining input
    if (INPUT_BUILDER.length() > 0) {
      Arguments args = produceArgs(INPUT_BUILDER.toString());
      INPUT_BUILDER.setLength(0);
      return args;
    }
    closeReader();
    return null; // Signal end of stream
  }

  private void closeReader() {
    try {
      this.reader.close();
    } catch (IOException e) {
      System.err.println("Error closing reader: " + e.getMessage());
      // ignore close exception
    }
  }

  private Arguments produceArgs(String input) {
    boolean supported = !input.startsWith("--unsupported");
    return Arguments.of(this.inputFile, this.emittedElements.incrementAndGet(), supported,
        INPUT_BUILDER.toString());
  }
}
