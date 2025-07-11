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

package ua.org.java.dynamoit.utils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import ua.org.java.dynamoit.db.KeySchemaType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    public static final ObjectWriter PRETTY_PRINTER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    public static <T> Stream<T> asStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static boolean isHashKey(String attributeName, DescribeTableResponse describeTableResponse) {
        TableDescription tableDescription = describeTableResponse.table();
        return tableDescription.keySchema().stream().anyMatch(keySchemaElement -> keySchemaElement.attributeName().equals(attributeName) && keySchemaElement.keyType() == KeyType.HASH);
    }

    public static boolean isRangeKey(String attributeName, DescribeTableResponse describeTableResponse) {
        TableDescription tableDescription = describeTableResponse.table();
        return tableDescription.keySchema().stream().anyMatch(keySchemaElement -> keySchemaElement.attributeName().equals(attributeName) && keySchemaElement.keyType() == KeyType.RANGE);
    }

    public static Optional<String> getHashKey(DescribeTableResponse describeTableResponse) {
        TableDescription tableDescription = describeTableResponse.table();
        return lookUpKeyName(tableDescription.keySchema(), KeySchemaType.HASH);
    }

    public static Optional<String> getRangeKey(DescribeTableResponse describeTableResponse) {
        TableDescription tableDescription = describeTableResponse.table();
        return lookUpKeyName(tableDescription.keySchema(), KeySchemaType.RANGE);
    }

    public static Optional<String> lookUpKeyName(List<KeySchemaElement> keySchemaElements, KeySchemaType keySchemaType) {
        return keySchemaElements.stream().filter(keySchemaElement -> keySchemaElement.keyType().name().equals(keySchemaType.name())).map(KeySchemaElement::attributeName).findFirst();
    }

    public static <T> Comparator<T> KEYS_FIRST(String hashKeyName, String rangeKeyName, Function<T, String> convert) {
        return (o1, o2) -> {
            String v1 = convert.apply(o1);
            String v2 = convert.apply(o2);

            if (v1.equals(hashKeyName)) {
                return -1;
            }
            if (v2.equals(hashKeyName)) {
                return 1;
            }
            if (v1.equals(rangeKeyName)) {
                return -1;
            }
            if (v2.equals(rangeKeyName)) {
                return 1;
            }
            return v1.compareTo(v2);
        };
    }

    public static Comparator<String> KEYS_FIRST(String hashKeyName, String rangeKeyName) {
        return KEYS_FIRST(hashKeyName, rangeKeyName, s -> s);
    }

    public static String logAsJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
            System.out.println(ignored);
        }
        return String.valueOf(value);
    }

    public static String trimToBlank(String s) {
        if (s == null) {
            return "";
        }
        return s.trim();
    }

    public static String truncateWithDots(String value) {
        if (value.length() > 40) {
            return value.substring(0, 40) + "..";
        }
        return value;
    }

    public static List<Integer> skipSequences(List<Integer> dataList) {
        return groupBySeq(dataList).stream().map(list -> list.get(0)).collect(Collectors.toList());
    }

    public static List<List<Integer>> groupBySeq(List<Integer> dataList) {
        return dataList.stream().sorted().collect(ArrayList::new,
                (acc, val) -> {
                    if (acc.isEmpty()) {
                        acc.add(new ArrayList<>());
                    }
                    List<Integer> lastGroup = acc.get(acc.size() - 1);
                    if (lastGroup.isEmpty() || val - lastGroup.get(lastGroup.size() - 1) == 1) {
                        lastGroup.add(val);
                    } else {
                        ArrayList<Integer> newGroup = new ArrayList<>();
                        newGroup.add(val);
                        acc.add(newGroup);
                    }
                }, (lists, lists2) -> {
                });
    }

    public static boolean isKeyModifierDown(KeyEvent event) {
        return event.isAltDown() || event.isShiftDown() || event.isControlDown() || event.isMetaDown();
    }

    public static Map<String, AttributeValue> fromRawJson(String json) throws JsonProcessingException {
        JsonNode root = OBJECT_MAPPER.readTree(json);
        Map<String, AttributeValue> item = new HashMap<>();
        root.fields().forEachRemaining(entry -> item.put(entry.getKey(), rawJsonNodeToAttributeValue(entry.getValue())));
        return item;
    }

    public static String toRawJson(Map<String, AttributeValue> item) throws JsonProcessingException {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        item.forEach((k, v) -> root.set(k, attributeValueToRawJsonNode(v)));
        return PRETTY_PRINTER.writeValueAsString(root);
    }

    public static Map<String, AttributeValue> fromSimpleJson(String json) throws JsonProcessingException {
        JsonNode root = OBJECT_MAPPER.readTree(json);
        Map<String, AttributeValue> item = new HashMap<>();
        root.fields().forEachRemaining(entry -> item.put(entry.getKey(), jsonNodeToAttributeValue(entry.getValue())));
        return item;
    }

    public static String toSimpleJson(Map<String, AttributeValue> item) throws JsonProcessingException {
        Map<String, Object> simple = new HashMap<>();
        item.forEach((key, av) -> simple.put(key, attributeValueToObject(av)));
        return PRETTY_PRINTER.writeValueAsString(simple);
    }

    private static AttributeValue jsonNodeToAttributeValue(JsonNode node) {
        if (node.isTextual()) return AttributeValue.builder().s(node.asText()).build();
        if (node.isNumber()) return AttributeValue.builder().n(node.asText()).build();
        if (node.isBoolean()) return AttributeValue.builder().bool(node.asBoolean()).build();
        if (node.isNull()) return AttributeValue.builder().nul(true).build();
        if (node.isArray()) {
            List<AttributeValue> list = new ArrayList<>();
            for (JsonNode child : node) {
                list.add(jsonNodeToAttributeValue(child));
            }
            return AttributeValue.builder().l(list).build();
        }
        if (node.isObject()) {
            Map<String, AttributeValue> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> map.put(entry.getKey(), jsonNodeToAttributeValue(entry.getValue())));
            return AttributeValue.builder().m(map).build();
        }
        throw new IllegalArgumentException("Unsupported JSON type: " + node.toString());
    }

    public static Object attributeValueToObject(AttributeValue av) {
        if (av.s() != null) return av.s();
        if (av.n() != null) return av.n();
        if (av.bool() != null) return av.bool();
        if (av.nul() != null && av.nul()) return null;
        if (av.l() != null) return av.l().stream().map(Utils::attributeValueToObject).toList();
        if (av.m() != null) {
            Map<String, Object> map = new HashMap<>();
            av.m().forEach((k, v) -> map.put(k, attributeValueToObject(v)));
            return map;
        }
        if (av.ss() != null) return av.ss();
        if (av.ns() != null) return av.ns();
        if (av.bs() != null) return av.bs();
        throw new IllegalArgumentException("Unsupported AttributeValue type");
    }

    public static String convertJsonDocument(String json, boolean toRaw) {
        try {
            if (toRaw) {
                return toRawJson(fromSimpleJson(json));
            } else {
                return toSimpleJson(fromRawJson(json));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return json;
        }
    }

    public static void copyToClipboard(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    private static JsonNode attributeValueToRawJsonNode(AttributeValue av) {
        ObjectMapper mapper = OBJECT_MAPPER;
        if (av.s() != null) {
            return mapper.createObjectNode().put("s", av.s());
        } else if (av.n() != null) {
            return mapper.createObjectNode().put("n", av.n());
        } else if (av.bool() != null) {
            return mapper.createObjectNode().put("bool", av.bool());
        } else if (av.nul() != null && av.nul()) {
            return mapper.createObjectNode().put("nul", true);
        } else if (av.l() != null) {
            ArrayNode array = mapper.createArrayNode();
            av.l().forEach(lav -> array.add(attributeValueToRawJsonNode(lav)));
            return mapper.createObjectNode().set("l", array);
        } else if (av.m() != null) {
            ObjectNode obj = mapper.createObjectNode();
            av.m().forEach((k, v) -> obj.set(k, attributeValueToRawJsonNode(v)));
            return mapper.createObjectNode().set("m", obj);
        } else if (av.ss() != null) {
            ArrayNode array = mapper.createArrayNode();
            av.ss().forEach(array::add);
            return mapper.createObjectNode().set("ss", array);
        } else if (av.ns() != null) {
            ArrayNode array = mapper.createArrayNode();
            av.ns().forEach(array::add);
            return mapper.createObjectNode().set("ns", array);
        } // skip bs for now
        throw new IllegalArgumentException("Unsupported AttributeValue type");
    }

    private static AttributeValue rawJsonNodeToAttributeValue(JsonNode node) {
        if (!node.isObject() || node.size() != 1) {
            throw new IllegalArgumentException("Invalid raw AttributeValue JSON");
        }
        String type = node.fieldNames().next();
        JsonNode value = node.get(type);
        switch (type) {
            case "s":
                return AttributeValue.builder().s(value.asText()).build();
            case "n":
                return AttributeValue.builder().n(value.asText()).build();
            case "bool":
                return AttributeValue.builder().bool(value.asBoolean()).build();
            case "nul":
                return AttributeValue.builder().nul(true).build();
            case "l":
                List<AttributeValue> l = Utils.asStream(value).map(Utils::rawJsonNodeToAttributeValue).collect(Collectors.toList());
                return AttributeValue.builder().l(l).build();
            case "m":
                Map<String, AttributeValue> m = new HashMap<>();
                value.fields().forEachRemaining(e -> m.put(e.getKey(), rawJsonNodeToAttributeValue(e.getValue())));
                return AttributeValue.builder().m(m).build();
            case "ss":
                List<String> ss = Utils.asStream(value).map(JsonNode::asText).collect(Collectors.toList());
                return AttributeValue.builder().ss(ss).build();
            case "ns":
                List<String> ns = Utils.asStream(value).map(JsonNode::asText).collect(Collectors.toList());
                return AttributeValue.builder().ns(ns).build();
            // skip bs
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

}
