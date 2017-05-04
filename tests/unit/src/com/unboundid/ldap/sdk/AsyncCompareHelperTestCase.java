/*
 * Copyright 2009-2017 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2009-2017 Ping Identity Corporation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk;



import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;



/**
 * This class provides a set of test cases for the {@code AsyncCompareHelper}
 * class.
 * <BR><BR>
 * Access to a Directory Server instance is required for complete processing.
 */
public class AsyncCompareHelperTestCase
       extends LDAPSDKTestCase
{
  // The connection that will be used for the tests.
  private LDAPConnection connection;

  // The async listener that will be used.
  private TestAsyncListener asyncListener;



  /**
   * Performs the necessary setup for the tests.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @BeforeClass()
  public void setUp()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    connection    = getAdminConnection();
    asyncListener = new TestAsyncListener();
  }



  /**
   * Performs the necessary cleanup after the tests are finished.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @AfterClass()
  public void cleanUp()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    connection.close();
  }



  /**
   * Tests with a valid compare result message.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testResponseReceivedValidCompareResult()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    asyncListener.clear();

    AsyncCompareHelper helper =
         new AsyncCompareHelper(connection, 1, asyncListener, null);

    CompareResult compareResult = new CompareResult(1, ResultCode.COMPARE_TRUE,
          null, null, null, null);

    helper.responseReceived(compareResult);

    LDAPResult result = asyncListener.getLastResult();
    assertNotNull(result);
    assertTrue(result instanceof CompareResult);

    CompareResult cr = (CompareResult) result;
    assertNotNull(cr);
    assertTrue(cr.compareMatched());
  }



  /**
   * Tests with a connection closed response that contains a message.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testConnectionClosedResponseReceivedWithMessage()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    asyncListener.clear();

    AsyncCompareHelper helper =
         new AsyncCompareHelper(connection, 1, asyncListener, null);

    helper.responseReceived(new ConnectionClosedResponse(
         ResultCode.SERVER_DOWN, "foo"));

    LDAPResult result = asyncListener.getLastResult();
    assertNotNull(result);
    assertTrue(result instanceof CompareResult);
    assertResultCodeEquals(result, ResultCode.SERVER_DOWN);
  }



  /**
   * Tests with a connection closed response that does not contain a message.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testConnectionClosedResponseReceivedWithoutMessage()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    asyncListener.clear();

    AsyncCompareHelper helper =
         new AsyncCompareHelper(connection, 1, asyncListener, null);

    helper.responseReceived(new ConnectionClosedResponse(
         ResultCode.SERVER_DOWN, null));

    LDAPResult result = asyncListener.getLastResult();
    assertNotNull(result);
    assertTrue(result instanceof CompareResult);
    assertResultCodeEquals(result, ResultCode.SERVER_DOWN);
  }



  /**
   * Tests with an intermediate response message for a helper with no listener.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testIntermediateResponseNoListener()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    AsyncCompareHelper helper =
         new AsyncCompareHelper(connection, 1, asyncListener, null);

    IntermediateResponse r = new IntermediateResponse("1.2.3.4", null);
    helper.intermediateResponseReturned(r);
  }



  /**
   * Tests with an intermediate response message for a helper with a listener.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testIntermediateResponseWithListener()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    TestIntermediateResponseListener l = new TestIntermediateResponseListener();

    AsyncCompareHelper helper =
         new AsyncCompareHelper(connection, 1, asyncListener, l);

    IntermediateResponse r = new IntermediateResponse("1.2.3.4", null);
    helper.intermediateResponseReturned(r);

    assertEquals(l.getCount(), 1);
  }
}