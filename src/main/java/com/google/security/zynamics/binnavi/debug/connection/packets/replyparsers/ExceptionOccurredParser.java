/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ExceptionOccurredReply;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Parser responsible for parsing replies sent by the debug client when exceptions occurred in the
 * target process.
 */
public final class ExceptionOccurredParser extends AbstractReplyParser<ExceptionOccurredReply> {
  /**
   * Creates a new Exception Occurred reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public ExceptionOccurredParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_EXCEPTION_OCCURED);
  }

  @Override
  protected ExceptionOccurredReply parseError(final int packetId) {
    // TODO(jannewger): There is no proper handling of errors on the side of the
    // client yet.
    throw new IllegalStateException("IE01086: Received invalid reply from the debug client");
  }

  @Override
  public ExceptionOccurredReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    Preconditions.checkArgument(argumentCount == 1,
        "IE00068: Unexpected number of argument while parsing exception occured packet");
    final byte[] data = parseData();
    Preconditions.checkNotNull(data, "IE00095: Data argument can not be null");
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));
      final Node node = document.getFirstChild();
      final long threadId =
          Long.valueOf(node.getAttributes().getNamedItem("threadId").getNodeValue());
      final RelocatedAddress address = new RelocatedAddress(new CAddress(
          new BigInteger(node.getAttributes().getNamedItem("address").getNodeValue())));
      final long exceptionCode =
          Long.valueOf(node.getAttributes().getNamedItem("exceptionCode").getNodeValue());
      String exceptionName = node.getAttributes().getNamedItem("exceptionName").getNodeValue();
      if (exceptionName.isEmpty()) {
        exceptionName = "Unknown exception";
      }

      return new ExceptionOccurredReply(packetId,
          0,
          threadId,
          exceptionCode,
          address,
          exceptionName);
    } catch (final Exception exception) {
      CUtilityFunctions.logException(exception);

      throw new IllegalStateException(
          "IE00097: Unexpected error while parsing exception occured packet");
    }
  }
}
