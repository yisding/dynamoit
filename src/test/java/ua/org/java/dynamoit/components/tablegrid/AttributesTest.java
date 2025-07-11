/*
 * This file is part of DynamoIt.
 *
 *     DynamoIt is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     DynamoIt is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with DynamoIt.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.org.java.dynamoit.components.tablegrid;

import ua.org.java.dynamoit.components.tablegrid.parser.FilterExpression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

public class AttributesTest {

    @Test
    public void testAttributeValueToFilter() {
        FilterExpression fe = Attributes.attributeValueToFilter("name", "John", Attributes.Type.STRING);
        assertEquals("#name = :name", fe.expression);
        assertEquals(Map.of("#name", "name"), fe.names);
        assertEquals(Map.of(":name", AttributeValue.builder().s("John").build()), fe.values);

        fe = Attributes.attributeValueToFilter("name", "^John", Attributes.Type.STRING);
        assertEquals("begins_with(#name, :name)", fe.expression);
        assertEquals(Map.of("#name", "name"), fe.names);
        assertEquals(Map.of(":name", AttributeValue.builder().s("John").build()), fe.values);

        fe = Attributes.attributeValueToFilter("name", "~John", Attributes.Type.STRING);
        assertEquals("contains(#name, :name)", fe.expression);
        assertEquals(Map.of("#name", "name"), fe.names);
        assertEquals(Map.of(":name", AttributeValue.builder().s("John").build()), fe.values);

        fe = Attributes.attributeValueToFilter("name", null, Attributes.Type.STRING);
        assertNull(fe);

        fe = Attributes.attributeValueToFilter("name", "", Attributes.Type.STRING);
        assertNull(fe);

        fe = Attributes.attributeValueToFilter("name", "*", Attributes.Type.STRING);
        assertEquals("#name = :name", fe.expression);
        assertEquals(Map.of("#name", "name"), fe.names);
        assertEquals(Map.of(":name", AttributeValue.builder().s("*").build()), fe.values);

        fe = Attributes.attributeValueToFilter("name", "**", Attributes.Type.STRING);
        assertEquals("#name = :name", fe.expression);
        assertEquals(Map.of("#name", "name"), fe.names);
        assertEquals(Map.of(":name", AttributeValue.builder().s("**").build()), fe.values);
    }
}

