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

package ua.org.java.dynamoit.components.tablegrid.parser;

import ua.org.java.dynamoit.components.tablegrid.parser.BeginsWithParser;
import ua.org.java.dynamoit.components.tablegrid.parser.FilterExpression;
import ua.org.java.dynamoit.components.tablegrid.Attributes.Type;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class BeginsWithParserTest {

    @Test
    public void testEmpty() {
        assertFalse(new BeginsWithParser().matches(""));
        assertFalse(new BeginsWithParser().matches(" "));
        assertFalse(new BeginsWithParser().matches("^"));
        assertTrue(new BeginsWithParser().matches("^hello"));
    }

    @Test
    public void testValue() {
        BeginsWithParser parser = new BeginsWithParser();
        assertTrue(parser.matches("^hello"));

        FilterExpression fe = parser.parse("attr", "^hello", null);

        assertEquals("begins_with(#attr, :attr)", fe.expression);
        assertEquals(Map.of("#attr", "attr"), fe.names);
        assertEquals(Map.of(":attr", AttributeValue.builder().s("hello").build()), fe.values);
    }

}
