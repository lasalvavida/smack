/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright 2013 Florian Schmaus.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
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

package org.jivesoftware.smack.parsing;

/**
 * Parsing exception callback class that simply throws the encoutered parsing exception. This usually leads to an
 * connectionClosedOnError disconnect of the connection.
 * 
 * @author Florian Schmaus
 * 
 */
public class ThrowException extends ParsingExceptionCallback {
    public void messageParsingException(Exception e) throws Exception {
        throw e;
    }

    public void iqParsingException(Exception e) throws Exception {
        throw e;
    }

    public void presenceParsingException(Exception e) throws Exception {
        throw e;
    }
}
