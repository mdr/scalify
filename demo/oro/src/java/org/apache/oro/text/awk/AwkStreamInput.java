/* 
 * $Id: AwkStreamInput.java 124053 2005-01-04 01:24:35Z dfs $
 *
 * Copyright 2000-2005 The Apache Software Foundation
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


package org.apache.oro.text.awk;

import java.io.*;
import org.apache.oro.text.regex.*;

/**
 * The AwkStreamInput class is used to look for pattern matches in an
 * input stream (actually a java.io.Reader instance) in conjunction with
 * the AwkMatcher class.  It is called
 * AwkStreamInput instead of AwkInputStream to stress that it is a form
 * of streamed input for the AwkMatcher class to use rather than a subclass of
 * InputStream.
 * AwkStreamInput performs special internal buffering to accelerate
 * pattern searches through a stream.  You can determine the size of this
 * buffer and how it grows by using the appropriate constructor.
 * <p>
 * If you want to perform line by line
 * matches on an input stream, you should use a DataInput or BufferedReader
 * instance in conjunction
 * with one of the PatternMatcher methods taking a String, char[], or
 * PatternMatcherInput as an argument.  The DataInput and BufferedReader
 * readLine() methods will likely be implemented as native methods and
 * therefore more efficient than supporting line by line searching within
 * AwkStreamInput.
 * <p>
 * In the future the programmer will be able to set this class to save
 * all the input it sees so that it can be accessed later.  This will avoid
 * having to read a stream more than once for whatever reason.
 *
 * @version @version@
 * @since 1.0
 * @see AwkMatcher
 */
public final class AwkStreamInput {
  static final int _DEFAULT_BUFFER_INCREMENT = 2048;
  private Reader __searchStream;
  private int __bufferIncrementUnit;
  boolean _endOfStreamReached;
  // The offset into the stream corresponding to buffer[0]
  int _bufferSize, _bufferOffset, _currentOffset;
  char[] _buffer;

  /**
   * We use this default contructor only within the package to create a dummy
   * AwkStreamInput instance.
   */
  AwkStreamInput() {
    _currentOffset = 0;
  }


  /**
   * Creates an AwkStreamInput instance bound to a Reader with a
   * specified initial buffer size and default buffer increment.
   * <p>
   * @param input  The InputStream to associate with the AwkStreamInput
   *        instance.
   * @param bufferIncrement  The initial buffer size and the default buffer
   *      increment to use when the input buffer has to be increased in
   *      size.
   */
  public AwkStreamInput(Reader input, int bufferIncrement) {
    __searchStream = input;
    __bufferIncrementUnit = bufferIncrement;
    _buffer = new char[bufferIncrement];
    _bufferOffset = _bufferSize  =  _currentOffset = 0;
    _endOfStreamReached = false;
  }


  /**
   * Creates an AwkStreamInput instance bound to a Reader with an
   * initial buffer size and default buffer increment of 2048 bytes.
   * <p>
   * @param input  The InputStream to associate with the AwkStreamInput
   *        instance.
   */
  public AwkStreamInput(Reader input) {
    this(input, _DEFAULT_BUFFER_INCREMENT);
  }

  // Only called when buffer overflows
  int _reallocate(int initialOffset) throws IOException {
    int offset, bytesRead;
    char[] tmpBuffer;

    if(_endOfStreamReached)
      return _bufferSize;

    offset    = _bufferSize - initialOffset;
    tmpBuffer = new char[offset + __bufferIncrementUnit];

    bytesRead =
      __searchStream.read(tmpBuffer, offset, __bufferIncrementUnit);

    if(bytesRead <= 0){
      _endOfStreamReached = true;
      /* bytesRead should never equal zero, but if it does, we don't
	 want to continue to try and read, running the risk of entering
	 an infinite loop.  Throw an IOException instead, because this
	 really IS an exception. */
      if(bytesRead == 0)
	throw new IOException("read from input stream returned 0 bytes.");
      return _bufferSize;
    } else {
      _bufferOffset += initialOffset;
      _bufferSize = offset + bytesRead;

      System.arraycopy(_buffer, initialOffset, tmpBuffer, 0, offset);
      _buffer = tmpBuffer;
    }

    return offset;
  }

  boolean read() throws IOException {
    _bufferOffset+=_bufferSize;
    _bufferSize = __searchStream.read(_buffer);
    _endOfStreamReached = (_bufferSize == -1);
    return (!_endOfStreamReached);
  }

  public boolean endOfStream() { return _endOfStreamReached; }

}
