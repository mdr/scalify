/* 
 * $Id: JavaMatchResult.java 124053 2005-01-04 01:24:35Z dfs $
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


package org.apache.oro.text.java;


import java.util.regex.*;

import org.apache.oro.text.regex.*;

/**
 *
 * @version @version@
 * @since 2.1
 */
final class JavaMatchResult extends Perl5MatchResult {

  JavaMatchResult(Matcher matcher){
    super(matcher.groupCount());
    _match_ = matcher.group();
    _matchBeginOffset_ = matcher.start();
    for(int i = 0; i < _beginGroupOffset_.length; ++i) {
      _beginGroupOffset_[i] = matcher.start(i) - _matchBeginOffset_;
      _endGroupOffset_[i]   = matcher.end(i) - _matchBeginOffset_;
    }
  }

}
