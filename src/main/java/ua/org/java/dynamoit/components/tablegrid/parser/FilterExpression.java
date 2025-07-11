package ua.org.java.dynamoit.components.tablegrid.parser;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class FilterExpression {

    public String expression;
    public Map<String, String> names = new HashMap<>();
    public Map<String, AttributeValue> values = new HashMap<>();

    public FilterExpression(String expression, String attrPlaceholder, String attribute, String valPlaceholder, AttributeValue value) {
        this.expression = expression;
        this.names.put(attrPlaceholder, attribute);
        this.values.put(valPlaceholder, value);
    }

    public FilterExpression(String expression, String attrPlaceholder, String attribute) {
        this.expression = expression;
        this.names.put(attrPlaceholder, attribute);
    }
} 