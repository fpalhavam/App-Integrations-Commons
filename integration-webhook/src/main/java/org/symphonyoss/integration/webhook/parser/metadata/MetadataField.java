/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.webhook.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.webhook.exception.MetadataParserException;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Field that represents a pure text without any markup.
 *
 * Each field has the attributes key, value and blank (optional).
 *
 * Example:
 * <pre>
 *   <field key="header" value="content.header" />
 * </pre>
 *
 * Created by rsanchez on 30/03/17.
 */
public class MetadataField {

  private static final String COMPONENT = "Common Webhook Dispatcher";

  private enum Type {
    BOOLEAN {
      @Override
      public Object getValue(JsonNode node) {
        return node.asBoolean(Boolean.FALSE);
      }
    },
    DEFAULT {
      @Override
      public Object getValue(JsonNode node) {
        return node.asText(StringUtils.EMPTY);
      }
    };

    public abstract Object getValue(JsonNode node);
  }

  private String key;

  private String value;

  private Type type = Type.DEFAULT;

  private boolean blank;

  @XmlAttribute
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @XmlAttribute
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @XmlAttribute
  public boolean isBlank() {
    return blank;
  }

  public void setBlank(boolean blank) {
    this.blank = blank;
  }

  @XmlAttribute
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Get the content from the input JSON according to the value attribute using dot notation.
   *
   * This value must be included as content to the {@link EntityObject} received as parameter.
   *
   * @param root Entity Object to store the content retrieved from the input JSON
   * @param node Input JSON node
   */
  public void process(EntityObject root, JsonNode node) {
    JsonNode resultNode = getResultNode(node, value);

    if (type == null) {
      throw new MetadataParserException(COMPONENT, "Invalid type in metadata.");
    }

    Object value = type.getValue(resultNode);

    if ((value != null && StringUtils.isNotEmpty(value.toString())) || isBlank()) {
      root.addContent(getKey(), value);
    }
  }

  /**
   * Navigates to the JSON node according to the key using dot notation.
   *
   * Example:
   *
   * JSON node
   * <pre>
   *   {
   *     "content": {
   *       "header": "hello",
   *       "body": "world"
   *     }
   *   }
   * </pre>
   *
   * To query the header field should be used the key 'content.header'
   *
   * @param node Input JSON node
   * @param jsonKey JSON key using dot notation
   * @return JSON node intended or {@link com.fasterxml.jackson.databind.node.MissingNode} if the node
   * wasn't found
   */
  private JsonNode getResultNode(JsonNode node, String jsonKey) {
    String[] nodeKeys = jsonKey.split("\\.");

    JsonNode resultNode = node;

    for (String key : nodeKeys) {
      resultNode = resultNode.path(key);
    }

    return resultNode;
  }

  @Override
  public String toString() {
    return "MetadataField{" +
        "key='" + key + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
