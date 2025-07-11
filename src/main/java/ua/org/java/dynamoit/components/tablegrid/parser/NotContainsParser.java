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

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import ua.org.java.dynamoit.components.tablegrid.Attributes;

import java.util.regex.Pattern;

public class NotContainsParser extends BaseValueToFilterParser {

    private static final Pattern PATTERN = Pattern.compile("!~(.+)");

    @Override
    protected Pattern regPattern() {
        return PATTERN;
    }

    @Override
    protected FilterExpression createExpression(String attributeName, String term, Attributes.Type type) {
        AttributeValue av = AttributeValue.builder().s(term).build();
        String phName = "#" + attributeName;
        String phVal = ":" + attributeName;
        return new FilterExpression("not contains(" + phName + ", " + phVal + ")", phName, attributeName, phVal, av);
    }
}
