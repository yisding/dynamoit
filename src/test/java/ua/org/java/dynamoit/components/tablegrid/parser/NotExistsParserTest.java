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
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

public class NotExistsParserTest {

    @Test
    public void test() {
        assertFalse(new NotExistsParser().matches(""));
        assertFalse(new NotExistsParser().matches(" "));
        assertFalse(new NotExistsParser().matches("$"));
        assertFalse(new NotExistsParser().matches("$hello"));
        assertFalse(new NotExistsParser().matches("hello$"));
        assertFalse(new NotExistsParser().matches("hello$hello"));

        NotExistsParser parser = new NotExistsParser();
        assertTrue(parser.matches("!$"));

        FilterExpression fe = parser.parse("attr", "!$", null);

        assertEquals("attribute_not_exists(#attr)", fe.expression);
        assertEquals(Map.of("#attr", "attr"), fe.names);
        assertTrue(fe.values.isEmpty());
    }

}
