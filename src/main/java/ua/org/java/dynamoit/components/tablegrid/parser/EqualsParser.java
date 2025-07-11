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

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class EqualsParser extends BaseValueToFilterParser {

    private static final Pattern PATTERN = Pattern.compile("(.*)");

    @Override
    protected Pattern regPattern() {
        return PATTERN;
    }

    @Override
    protected FilterExpression createExpression(String attributeName, String term, Attributes.Type type) {
        AttributeValue.Builder b = AttributeValue.builder();
        try {
            if (type == Attributes.Type.NUMBER) {
                b.n(new BigDecimal(term).toString());
            } else if (type == Attributes.Type.BOOLEAN) {
                b.bool(Boolean.parseBoolean(term));
            } else {
                b.s(term);
            }
        } catch (Exception e) {
            e.printStackTrace();
            b.s(term);
        }
        AttributeValue av = b.build();
        String phName = "#" + attributeName;
        String phVal = ":" + attributeName;
        return new FilterExpression(phName + " = " + phVal, phName, attributeName, phVal, av);
    }
}
