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
 * Base class to receive parsing exceptions
 * 
 * @author Florian Schmaus
 *
 */
public abstract class ParsingExceptionCallback {

    /**
     * Called when parsing an message stanza caused an exception.
     * 
     * @param e
     * the exception thrown while parsing the message stanza
     * @param message
     * the raw message stanza data that caused the exception
     * @throws Exception
     */
    public void messageParsingException(Exception e, UnparsedMessage message) throws Exception {
    }

    /**
     * Called when parsing an IQ stanza caused an exception.
     * 
     * @param e
     * the exception thrown while parsing the iq stanza
     * @param iq
     * the raw iq stanza data that caused the exception
     * @throws Exception
     */
    public void iqParsingException(Exception e, UnparsedIQ iq) throws Exception {
    }

    /**
     * Called when parsing a presence stanza caused an exception.
     * 
     * @param e
     * the exception thrown while parsing the presence stanza
     * @param presence
     * the raw presence stanza data that caused the exception
     * @throws Exception
     */
    public void presenceParsingException(Exception e, UnparsedPresence presence) throws Exception {
    }
}
