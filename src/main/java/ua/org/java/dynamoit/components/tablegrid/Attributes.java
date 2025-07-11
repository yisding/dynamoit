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

import javafx.util.Pair;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import ua.org.java.dynamoit.components.tablegrid.parser.*;
import ua.org.java.dynamoit.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Attributes {

    public enum Type {
        STRING, NUMBER, BOOLEAN, BINARY, NULL, LIST, MAP, STRING_SET, NUMBER_SET, BINARY_SET
    }

    private Attributes() {
    }

    public static Map<String, Type> defineAttributesTypes(List<Map<String, AttributeValue>> itemList) {
        return itemList.stream()
                .flatMap(item -> item.entrySet().stream()
                        .map(entry -> new javafx.util.Pair<>(entry.getKey(), fromAttributeValue(entry.getValue()))))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (type1, type2) -> type1));
    }

    private static Type fromAttributeValue(AttributeValue av) {
        if (av.s() != null) return Type.STRING;
        if (av.n() != null) return Type.NUMBER;
        if (av.b() != null) return Type.BINARY;
        if (av.bool() != null) return Type.BOOLEAN;
        if (av.nul() != null) return Type.NULL;
        if (av.l() != null) return Type.LIST;
        if (av.m() != null) return Type.MAP;
        if (av.ss() != null) return Type.STRING_SET;
        if (av.ns() != null) return Type.NUMBER_SET;
        if (av.bs() != null) return Type.BINARY_SET;
        return Type.STRING;
    }

    public static Type fromDynamoDBType(ScalarAttributeType dynamoDBType) {
        return Type.valueOf(dynamoDBType.name());
    }

    public static FilterExpression attributeValueToFilter(String attribute, String value, Type type) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.asList(
                new ContainsParser(),
                new BeginsWithParser(),
                new ExistsParser(),
                new NotEqualsParser(),
                new NotContainsParser(),
                new NotExistsParser(),
                new EqualsParser() // last parser
        ).stream()
                .filter(p -> p.matches(value))
                .findFirst()
                .map(p -> p.parse(attribute, value, type))
                .orElse(null);
    }

}
