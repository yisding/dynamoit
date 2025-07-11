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

import ua.org.java.dynamoit.components.tablegrid.parser.FilterExpression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Map;
import org.junit.Test;
import ua.org.java.dynamoit.components.tablegrid.Attributes;

import static org.junit.Assert.*;

public class NotEqualsParserTest {

    @Test
    public void test() {
        assertFalse(new NotEqualsParser().matches(""));
        assertFalse(new NotEqualsParser().matches(" "));
        assertFalse(new NotEqualsParser().matches("1"));
        assertFalse(new NotEqualsParser().matches("!="));  // no value
        assertTrue(new NotEqualsParser().matches("!=hello"));
    }

    @Test
    public void testValue(){
        NotEqualsParser parser = new NotEqualsParser();
        assertTrue(parser.matches("!=hello"));

        FilterExpression fe = parser.parse("attr", "!=hello", Attributes.Type.STRING);

        assertEquals("#attr <> :attr", fe.expression);
        assertEquals(Map.of("#attr", "attr"), fe.names);
        assertEquals(Map.of(":attr", AttributeValue.builder().s("hello").build()), fe.values);
    }

}
