/*
 * Copyright 2008-2022 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright 2008-2022 Ping Identity Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008-2022 Ping Identity Corporation
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
package com.unboundid.ldap.sdk.unboundidds.controls;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.unboundid.asn1.ASN1Boolean;
import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.JSONControlDecodeHelper;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.NotNull;
import com.unboundid.util.Nullable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.json.JSONArray;
import com.unboundid.util.json.JSONBoolean;
import com.unboundid.util.json.JSONField;
import com.unboundid.util.json.JSONObject;
import com.unboundid.util.json.JSONString;
import com.unboundid.util.json.JSONValue;

import static com.unboundid.ldap.sdk.unboundidds.controls.ControlMessages.*;



/**
 * This class provides an implementation of an LDAP control that can be included
 * in a bind request to request that the Directory Server return the
 * authentication and authorization entries for the user that authenticated.
 * <BR>
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class, and other classes within the
 *   {@code com.unboundid.ldap.sdk.unboundidds} package structure, are only
 *   supported for use against Ping Identity, UnboundID, and
 *   Nokia/Alcatel-Lucent 8661 server products.  These classes provide support
 *   for proprietary functionality or for external specifications that are not
 *   considered stable or mature enough to be guaranteed to work in an
 *   interoperable way with other types of LDAP servers.
 * </BLOCKQUOTE>
 * <BR>
 * The value of this control may be absent, but if it is present then will be
 * encoded as follows:
 * <PRE>
 *   GetAuthorizationEntryRequest ::= SEQUENCE {
 *        includeAuthNEntry     [0] BOOLEAN DEFAULT TRUE,
 *        includeAuthZEntry     [1] BOOLEAN DEFAULT TRUE,
 *        attributes            [2] AttributeSelection OPTIONAL }
 * </PRE>
 * <BR><BR>
 * <H2>Example</H2>
 * The following example demonstrates the process for processing a bind
 * operation using the get authorization entry request control to return all
 * user attributes in both the authentication and authorization entries:
 * <PRE>
 * ReadOnlyEntry authNEntry = null;
 * ReadOnlyEntry authZEntry = null;
 *
 * BindRequest bindRequest = new SimpleBindRequest(
 *      "uid=john.doe,ou=People,dc=example,dc=com", "password",
 *      new GetAuthorizationEntryRequestControl());
 *
 * BindResult bindResult = connection.bind(bindRequest);
 * GetAuthorizationEntryResponseControl c =
 *      GetAuthorizationEntryResponseControl.get(bindResult);
 * if (c != null)
 * {
 *   authNEntry = c.getAuthNEntry();
 *   authZEntry = c.getAuthZEntry();
 * }
 * </PRE>
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class GetAuthorizationEntryRequestControl
       extends Control
{
  /**
   * The OID (1.3.6.1.4.1.30221.2.5.6) for the get authorization entry request
   * control.
   */
  @NotNull public static final String GET_AUTHORIZATION_ENTRY_REQUEST_OID =
       "1.3.6.1.4.1.30221.2.5.6";



  /**
   * The BER type for the {@code includeAuthNEntry} element.
   */
  private static final byte TYPE_INCLUDE_AUTHN_ENTRY = (byte) 0x80;



  /**
   * The BER type for the {@code includeAuthZEntry} element.
   */
  private static final byte TYPE_INCLUDE_AUTHZ_ENTRY = (byte) 0x81;



  /**
   * The BER type for the {@code attributes} element.
   */
  private static final byte TYPE_ATTRIBUTES = (byte) 0xA2;



  /**
   * The name of the field used to hold the names of the requested attributes in
   * the JSON representation of this control.
   */
  @NotNull private static final String JSON_FIELD_ATTRIBUTES = "attributes";



  /**
   * The name of the field used to indicate whether to include the
   * authentication entry in the JSON representation of this control.
   */
  @NotNull private static final String JSON_FIELD_INCLUDE_AUTHENTICATION_ENTRY =
       "include-authentication-entry";



  /**
   * The name of the field used to indicate whether to include the
   * authorization entry in the JSON representation of this control.
   */
  @NotNull private static final String JSON_FIELD_INCLUDE_AUTHORIZATION_ENTRY =
       "include-authorization-entry";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -5540345171260624216L;



  // Indicates whether to include the authentication entry in the response.
  private final boolean includeAuthNEntry;

  // Indicates whether to include the authorization entry in the response.
  private final boolean includeAuthZEntry;

  // The list of attributes to include in entries that are returned.
  @NotNull private final List<String> attributes;



  /**
   * Creates a new get authorization entry request control that will request all
   * user attributes in both the authentication and authorization entries.  It
   * will not be marked critical.
   */
  public GetAuthorizationEntryRequestControl()
  {
    this(false, true, true, (List<String>) null);
  }



  /**
   * Creates a new get authorization entry request control with the provided
   * information.
   *
   * @param  includeAuthNEntry  Indicates whether to include the authentication
   *                            entry in the response.
   * @param  includeAuthZEntry  Indicates whether to include the authorization
   *                            entry in the response.
   * @param  attributes         The attributes to include in the entries in the
   *                            response.  It may be empty or {@code null} to
   *                            request all user attributes.
   */
  public GetAuthorizationEntryRequestControl(final boolean includeAuthNEntry,
              final boolean includeAuthZEntry,
              @Nullable final String... attributes)
  {
    this(false, includeAuthNEntry, includeAuthZEntry,
         (attributes == null) ? null : Arrays.asList(attributes));
  }



  /**
   * Creates a new get authorization entry request control with the provided
   * information.
   *
   * @param  includeAuthNEntry  Indicates whether to include the authentication
   *                            entry in the response.
   * @param  includeAuthZEntry  Indicates whether to include the authorization
   *                            entry in the response.
   * @param  attributes         The attributes to include in the entries in the
   *                            response.  It may be empty or {@code null} to
   *                            request all user attributes.
   */
  public GetAuthorizationEntryRequestControl(final boolean includeAuthNEntry,
              final boolean includeAuthZEntry,
              @Nullable final List<String> attributes)
  {
    this(false, includeAuthNEntry, includeAuthZEntry, attributes);
  }



  /**
   * Creates a new get authorization entry request control with the provided
   * information.
   *
   * @param  isCritical         Indicates whether the control should be marked
   *                            critical.
   * @param  includeAuthNEntry  Indicates whether to include the authentication
   *                            entry in the response.
   * @param  includeAuthZEntry  Indicates whether to include the authorization
   *                            entry in the response.
   * @param  attributes         The attributes to include in the entries in the
   *                            response.  It may be empty or {@code null} to
   *                            request all user attributes.
   */
  public GetAuthorizationEntryRequestControl(final boolean isCritical,
              final boolean includeAuthNEntry,
              final boolean includeAuthZEntry,
              @Nullable final String... attributes)
  {
    this(isCritical, includeAuthNEntry, includeAuthZEntry,
         (attributes == null) ? null : Arrays.asList(attributes));
  }



  /**
   * Creates a new get authorization entry request control with the provided
   * information.
   *
   * @param  isCritical         Indicates whether the control should be marked
   *                            critical.
   * @param  includeAuthNEntry  Indicates whether to include the authentication
   *                            entry in the response.
   * @param  includeAuthZEntry  Indicates whether to include the authorization
   *                            entry in the response.
   * @param  attributes         The attributes to include in the entries in the
   *                            response.  It may be empty or {@code null} to
   *                            request all user attributes.
   */
  public GetAuthorizationEntryRequestControl(final boolean isCritical,
              final boolean includeAuthNEntry,
              final boolean includeAuthZEntry,
              @Nullable final List<String> attributes)
  {
    super(GET_AUTHORIZATION_ENTRY_REQUEST_OID, isCritical,
          encodeValue(includeAuthNEntry, includeAuthZEntry, attributes));

    this.includeAuthNEntry = includeAuthNEntry;
    this.includeAuthZEntry = includeAuthZEntry;

    if ((attributes == null) || attributes.isEmpty())
    {
      this.attributes = Collections.emptyList();
    }
    else
    {
      this.attributes =
           Collections.unmodifiableList(new ArrayList<>(attributes));
    }
  }



  /**
   * Creates a new get authorization entry request control which is decoded from
   * the provided generic control.
   *
   * @param  control  The generic control to decode as a get authorization entry
   *                  request control.
   *
   * @throws  LDAPException  If the provided control cannot be decoded as a get
   *                         authorization entry request control.
   */
  public GetAuthorizationEntryRequestControl(@NotNull final Control control)
         throws LDAPException
  {
    super(control);

    final ASN1OctetString value = control.getValue();
    if (value == null)
    {
      includeAuthNEntry = true;
      includeAuthZEntry = true;
      attributes        = Collections.emptyList();
      return;
    }

    try
    {
      final ArrayList<String> attrs = new ArrayList<>(20);
      boolean includeAuthN = true;
      boolean includeAuthZ = true;

      final ASN1Element element = ASN1Element.decode(value.getValue());
      for (final ASN1Element e :
           ASN1Sequence.decodeAsSequence(element).elements())
      {
        switch (e.getType())
        {
          case TYPE_INCLUDE_AUTHN_ENTRY:
            includeAuthN = ASN1Boolean.decodeAsBoolean(e).booleanValue();
            break;
          case TYPE_INCLUDE_AUTHZ_ENTRY:
            includeAuthZ = ASN1Boolean.decodeAsBoolean(e).booleanValue();
            break;
          case TYPE_ATTRIBUTES:
            for (final ASN1Element ae :
                 ASN1Sequence.decodeAsSequence(e).elements())
            {
              attrs.add(ASN1OctetString.decodeAsOctetString(ae).stringValue());
            }
            break;
          default:
            throw new LDAPException(ResultCode.DECODING_ERROR,
                 ERR_GET_AUTHORIZATION_ENTRY_REQUEST_INVALID_SEQUENCE_ELEMENT.
                      get(StaticUtils.toHex(e.getType())));
        }
      }

      includeAuthNEntry = includeAuthN;
      includeAuthZEntry = includeAuthZ;
      attributes        = attrs;
    }
    catch (final LDAPException le)
    {
      throw le;
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_GET_AUTHORIZATION_ENTRY_REQUEST_CANNOT_DECODE_VALUE.get(
                StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Encodes the provided information as appropriate for use as the value of
   * this control.
   *
   * @param  includeAuthNEntry  Indicates whether to include the authentication
   *                            entry in the response.
   * @param  includeAuthZEntry  Indicates whether to include the authorization
   *                            entry in the response.
   * @param  attributes         The attributes to include in the entries in the
   *                            response.  It may be empty or {@code null} to
   *                            request all user attributes.
   *
   * @return  An ASN.1 octet string appropriately encoded for use as the control
   *          value, or {@code null} if no value is needed.
   */
  @Nullable()
  private static ASN1OctetString encodeValue(final boolean includeAuthNEntry,
                      final boolean includeAuthZEntry,
                      @Nullable final List<String> attributes)
  {
    if (includeAuthNEntry && includeAuthZEntry &&
        ((attributes == null) || attributes.isEmpty()))
    {
      return null;
    }

    final ArrayList<ASN1Element> elements = new ArrayList<>(3);

    if (! includeAuthNEntry)
    {
      elements.add(new ASN1Boolean(TYPE_INCLUDE_AUTHN_ENTRY, false));
    }

    if (! includeAuthZEntry)
    {
      elements.add(new ASN1Boolean(TYPE_INCLUDE_AUTHZ_ENTRY, false));
    }

    if ((attributes != null) && (! attributes.isEmpty()))
    {
      final ArrayList<ASN1Element> attrElements =
           new ArrayList<>(attributes.size());
      for (final String s : attributes)
      {
        attrElements.add(new ASN1OctetString(s));
      }

      elements.add(new ASN1Sequence(TYPE_ATTRIBUTES, attrElements));
    }

    return new ASN1OctetString(new ASN1Sequence(elements).encode());
  }



  /**
   * Indicates whether the entry for the authenticated user should be included
   * in the response control.
   *
   * @return  {@code true} if the entry for the authenticated user should be
   *          included in the response control, or {@code false} if not.
   */
  public boolean includeAuthNEntry()
  {
    return includeAuthNEntry;
  }



  /**
   * Indicates whether the entry for the authorized user should be included
   * in the response control.
   *
   * @return  {@code true} if the entry for the authorized user should be
   *          included in the response control, or {@code false} if not.
   */
  public boolean includeAuthZEntry()
  {
    return includeAuthZEntry;
  }



  /**
   * Retrieves the attributes that will be requested for the authentication
   * and/or authorization entries.
   *
   * @return  The attributes that will be requested for the authentication
   *          and/or authorization entries, or an empty list if all user
   *          attributes should be included.
   */
  @NotNull()
  public List<String> getAttributes()
  {
    return attributes;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @NotNull()
  public String getControlName()
  {
    return INFO_CONTROL_NAME_GET_AUTHORIZATION_ENTRY_REQUEST.get();
  }



  /**
   * Retrieves a representation of this get authorization entry request control
   * as a JSON object.  The JSON object uses the following fields:
   * <UL>
   *   <LI>
   *     {@code oid} -- A mandatory string field whose value is the object
   *     identifier for this control.  For the get authorization entry request
   *     control, the OID is "1.3.6.1.4.1.30221.2.5.6".
   *   </LI>
   *   <LI>
   *     {@code control-name} -- An optional string field whose value is a
   *     human-readable name for this control.  This field is only intended for
   *     descriptive purposes, and when decoding a control, the {@code oid}
   *     field should be used to identify the type of control.
   *   </LI>
   *   <LI>
   *     {@code criticality} -- A mandatory Boolean field used to indicate
   *     whether this control is considered critical.
   *   </LI>
   *   <LI>
   *     {@code value-base64} -- An optional string field whose value is a
   *     base64-encoded representation of the raw value for this get
   *     authorization entry request control.  Exactly one of the
   *     {@code value-base64} and {@code value-json} fields must be present.
   *   </LI>
   *   <LI>
   *     {@code value-json} -- An optional JSON object field whose value is a
   *     user-friendly representation of the value for this get authorization
   *     entry request control.  Exactly one of the {@code value-base64} and
   *     {@code value-json} fields must be present, and if the
   *     {@code value-json} field is used, then it will use the following
   *     fields:
   *     <UL>
   *       <LI>
   *         {@code include-authentication-entry} -- A mandatory Boolean field
   *         that indicates whether to include the entry for the authentication
   *         identity in the response control.
   *       </LI>
   *       <LI>
   *         {@code include-authorization-entry} -- A mandatory Boolean field
   *         that indicates whether to include the entry for the authorization
   *         identity in the response control.
   *       </LI>
   *       <LI>
   *         {@code attributes} -- An optional array field whose values are
   *         strings that represent the names of the attributes that should be
   *         included in the entries returned in the repsonse control.
   *       </LI>
   *     </UL>
   *   </LI>
   * </UL>
   *
   * @return  A JSON object that contains a representation of this control.
   */
  @Override()
  @NotNull()
  public JSONObject toJSONControl()
  {
    final Map<String,JSONValue> valueFields = new LinkedHashMap<>();
    valueFields.put(JSON_FIELD_INCLUDE_AUTHENTICATION_ENTRY,
         new JSONBoolean(includeAuthNEntry));
    valueFields.put(JSON_FIELD_INCLUDE_AUTHORIZATION_ENTRY,
         new JSONBoolean(includeAuthZEntry));

    if (! attributes.isEmpty())
    {
      final List<JSONValue> attributeValues =
           new ArrayList<>(attributes.size());
      for (final String attribute : attributes)
      {
        attributeValues.add(new JSONString(attribute));
      }

      valueFields.put(JSON_FIELD_ATTRIBUTES, new JSONArray(attributeValues));
    }


    return new JSONObject(
         new JSONField(JSONControlDecodeHelper.JSON_FIELD_OID,
              GET_AUTHORIZATION_ENTRY_REQUEST_OID),
         new JSONField(JSONControlDecodeHelper.JSON_FIELD_CONTROL_NAME,
              INFO_CONTROL_NAME_GET_AUTHORIZATION_ENTRY_REQUEST.get()),
         new JSONField(JSONControlDecodeHelper.JSON_FIELD_CRITICALITY,
              isCritical()),
         new JSONField(JSONControlDecodeHelper.JSON_FIELD_VALUE_JSON,
              new JSONObject(valueFields)));
  }



  /**
   * Attempts to decode the provided object as a JSON representation of a get
   * authorization entry request control.
   *
   * @param  controlObject  The JSON object to be decoded.  It must not be
   *                        {@code null}.
   * @param  strict         Indicates whether to use strict mode when decoding
   *                        the provided JSON object.  If this is {@code true},
   *                        then this method will throw an exception if the
   *                        provided JSON object contains any unrecognized
   *                        fields.  If this is {@code false}, then unrecognized
   *                        fields will be ignored.
   *
   * @return  The get authorization entry request control that was decoded from
   *          the provided JSON object.
   *
   * @throws  LDAPException  If the provided JSON object cannot be parsed as a
   *                         valid get authorization entry request control.
   */
  @NotNull()
  public static GetAuthorizationEntryRequestControl decodeJSONControl(
              @NotNull final JSONObject controlObject,
              final boolean strict)
         throws LDAPException
  {
    final JSONControlDecodeHelper jsonControl = new JSONControlDecodeHelper(
         controlObject, strict, true, true);

    final ASN1OctetString rawValue = jsonControl.getRawValue();
    if (rawValue != null)
    {
      return new GetAuthorizationEntryRequestControl(new Control(
           jsonControl.getOID(), jsonControl.getCriticality(), rawValue));
    }


    final JSONObject valueObject = jsonControl.getValueObject();

    final Boolean includeAuthNEntry =
         valueObject.getFieldAsBoolean(JSON_FIELD_INCLUDE_AUTHENTICATION_ENTRY);
    if (includeAuthNEntry == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_GET_AUTHORIZATION_ENTRY_REQUEST_JSON_MISSING_FIELD.get(
                controlObject.toSingleLineString(),
                JSON_FIELD_INCLUDE_AUTHENTICATION_ENTRY));
    }

    final Boolean includeAuthZEntry =
         valueObject.getFieldAsBoolean(JSON_FIELD_INCLUDE_AUTHORIZATION_ENTRY);
    if (includeAuthZEntry == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_GET_AUTHORIZATION_ENTRY_REQUEST_JSON_MISSING_FIELD.get(
                controlObject.toSingleLineString(),
                JSON_FIELD_INCLUDE_AUTHORIZATION_ENTRY));
    }


    final List<String> attributes;
    final List<JSONValue> attributeValues =
         valueObject.getFieldAsArray(JSON_FIELD_ATTRIBUTES);
    if (attributeValues == null)
    {
      attributes = null;
    }
    else
    {
      attributes = new ArrayList<>(attributeValues.size());
      for (final JSONValue v : attributeValues)
      {
        if (v instanceof JSONString)
        {
          attributes.add(((JSONString) v).stringValue());
        }
        else
        {
          throw new LDAPException(ResultCode.DECODING_ERROR,
               ERR_GET_AUTHORIZATION_ENTRY_REQUEST_JSON_ATTR_NOT_STRING.get(
                    controlObject.toSingleLineString(),
                    JSON_FIELD_ATTRIBUTES));
        }
      }
    }


    if (strict)
    {
      final List<String> unrecognizedFields =
           JSONControlDecodeHelper.getControlObjectUnexpectedFields(
                valueObject, JSON_FIELD_INCLUDE_AUTHENTICATION_ENTRY,
                JSON_FIELD_INCLUDE_AUTHORIZATION_ENTRY, JSON_FIELD_ATTRIBUTES);
      if (! unrecognizedFields.isEmpty())
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_GET_AUTHORIZATION_ENTRY_REQUEST_JSON_CONTROL_UNRECOGNIZED_FIELD
                  .get(controlObject.toSingleLineString(),
                       unrecognizedFields.get(0)));
      }
    }


    return new GetAuthorizationEntryRequestControl(jsonControl.getCriticality(),
         includeAuthNEntry, includeAuthZEntry, attributes);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(@NotNull final StringBuilder buffer)
  {
    buffer.append("GetAuthorizationEntryRequestControl(isCritical=");
    buffer.append(isCritical());
    buffer.append(", includeAuthNEntry=");
    buffer.append(includeAuthNEntry);
    buffer.append(", includeAuthZEntry=");
    buffer.append(includeAuthZEntry);
    buffer.append(", attributes={");

    final Iterator<String> iterator = attributes.iterator();
    while (iterator.hasNext())
    {
      buffer.append(iterator.next());
      if (iterator.hasNext())
      {
        buffer.append(", ");
      }
    }

    buffer.append("})");
  }
}
