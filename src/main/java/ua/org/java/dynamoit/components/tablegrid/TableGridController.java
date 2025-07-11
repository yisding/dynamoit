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

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.StringUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Pair;
import org.reactfx.EventStream;
import ua.org.java.dynamoit.EventBus;
import ua.org.java.dynamoit.db.DynamoDBService;
import ua.org.java.dynamoit.db.KeySchemaType;
import ua.org.java.dynamoit.model.TableDef;
import ua.org.java.dynamoit.components.tablegrid.parser.FilterExpression;
import ua.org.java.dynamoit.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static ua.org.java.dynamoit.components.tablegrid.Attributes.*;
import static ua.org.java.dynamoit.utils.Utils.*;
import java.nio.file.Files;
import com.fasterxml.jackson.core.JsonProcessingException;
import ua.org.java.dynamoit.components.tablegrid.parser.ValueToFilterParser;
import ua.org.java.dynamoit.components.tablegrid.parser.EqualsParser;
import ua.org.java.dynamoit.components.tablegrid.parser.NotEqualsParser;
import ua.org.java.dynamoit.components.tablegrid.parser.BeginsWithParser;
import ua.org.java.dynamoit.components.tablegrid.parser.ContainsParser;
import ua.org.java.dynamoit.components.tablegrid.parser.NotContainsParser;
import ua.org.java.dynamoit.components.tablegrid.parser.ExistsParser;
import ua.org.java.dynamoit.components.tablegrid.parser.NotExistsParser;

public class TableGridController {

    private static final Logger LOG = Logger.getLogger(TableGridController.class.getName());

    private static final int PAGE_SIZE = 100;

    private final DynamoDbClient dynamoDbClient;
    private final TableGridContext context;
    private final TableGridModel tableModel;
    private final EventBus eventBus;
    private final Executor uiExecutor;
    private final HostServices hostServices;

    public TableGridController(TableGridContext context, TableGridModel tableModel, DynamoDBService dynamoDBService, EventBus eventBus, Executor uiExecutor, HostServices hostServices) {
        this.context = context;
        this.tableModel = tableModel;
        this.eventBus = eventBus;
        this.uiExecutor = uiExecutor;
        this.hostServices = hostServices;

        tableModel.getProfileModel().getAvailableTables().stream()
                .filter(tableDef -> tableDef.getName().equals(context.tableName()))
                .findFirst()
                .ifPresent(tableModel::setTableDef);

        tableModel.setTableName(context.tableName());
        tableModel.setProfile(context.tableName());

        dynamoDbClient = dynamoDBService.getOrCreateDynamoDbClient(context.profileDetails());
    }

    public void init() {
        eventBus.activity(
                supplyAsync(() -> {
                    if (tableModel.getOriginalTableDescription() == null) {
                        return supplyAsync(() -> dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(context.tableName()).build()))
                                .thenAcceptAsync(this::bindToModel, uiExecutor);
                    } else {
                        bindToModel(tableModel.getTableDef());
                        return CompletableFuture.completedFuture(Boolean.TRUE);
                    }
                })
                        .thenCompose(__ -> __)
                        .thenRun(this::applyContext)
                        .thenCompose(__ -> executeQueryOrSearch(null))
                        .thenAcceptAsync(this::bindToModel, uiExecutor)
        );
    }

    private void bindToModel(DescribeTableResponse describeTable) {
        TableDescription originalTableDescription = describeTable.table();
        tableModel.setOriginalTableDescription(originalTableDescription);

        Utils.getHashKey(describeTable).ifPresent(tableModel.getTableDef()::setHashAttribute);
        Utils.getRangeKey(describeTable).ifPresent(tableModel.getTableDef()::setRangeAttribute);

        Map<String, Attributes.Type> attributes = new TreeMap<>(KEYS_FIRST(hash(), range()));
        attributes.putAll(
                originalTableDescription.attributeDefinitions().stream()
                        .collect(Collectors.toMap(
                                AttributeDefinition::attributeName,
                                ad -> Attributes.Type.valueOf(ad.attributeType().name())
                        )));

        attributes.forEach((name, type) -> tableModel.getTableDef().getAttributeTypesMap().put(name, type));

        tableModel.getTableDef().setTotalCount(originalTableDescription.itemCount());
    }

    private void bindToModel(Pair<List<Map<String, AttributeValue>>, Map<String, AttributeValue>> pair) {
        Map<String, Attributes.Type> attributesTypes = new TreeMap<>(KEYS_FIRST(hash(), range()));
        attributesTypes.putAll(defineAttributesTypes(pair.getKey()));
        tableModel.getTableDef().getAttributeTypesMap().putAll(attributesTypes);
        tableModel.setLastEvaluatedKey(pair.getValue());
        tableModel.getRows().addAll(pair.getKey());
    }

    private void bindToModel(TableDef tableDef) {
        tableDef.getAttributeTypesMap().keySet().forEach(attr -> tableModel.getAttributeFilterMap().computeIfAbsent(attr, __ -> new SimpleStringProperty()));
    }

    private Map<String, Attributes.Type> defineAttributesTypes(List<Map<String, AttributeValue>> items) {
        Map<String, Attributes.Type> types = new TreeMap<>(KEYS_FIRST(hash(), range()));
        items.forEach(item -> item.forEach((key, av) -> {
            Attributes.Type t = fromAttributeValue(av);
            if (t != null) {
                types.put(key, t);
            }
        }));
        return types;
    }

    private Attributes.Type fromAttributeValue(AttributeValue av) {
        if (av.s() != null) return Attributes.Type.STRING;
        if (av.n() != null) return Attributes.Type.NUMBER;
        if (av.b() != null) return Attributes.Type.BINARY;
        if (av.bool() != null) return Attributes.Type.BOOLEAN;
        if (av.nul() != null) return Attributes.Type.NULL;
        if (av.l() != null) return Attributes.Type.LIST;
        if (av.m() != null) return Attributes.Type.MAP;
        if (av.ss() != null) return Attributes.Type.STRING_SET;
        if (av.ns() != null) return Attributes.Type.NUMBER_SET;
        if (av.bs() != null) return Attributes.Type.BINARY_SET;
        return null;
    }

    public void onReachScrollEnd() {
        if (tableModel.getLastEvaluatedKey() != null && !tableModel.getLastEvaluatedKey().isEmpty()) {
            eventBus.activity(
                    executeQueryOrSearch(tableModel.getLastEvaluatedKey())
                            .thenAcceptAsync(pair -> {
                                Map<String, Attributes.Type> attributesTypes = defineAttributesTypes(pair.getKey());
                                tableModel.getTableDef().getAttributeTypesMap().putAll(attributesTypes);
                                tableModel.getRows().addAll(pair.getKey());
                                tableModel.setLastEvaluatedKey(pair.getValue());
                            }, uiExecutor)
            );
        }
    }

    public CompletableFuture<Void> onRefreshData() {
        return eventBus.activity(
                executeQueryOrSearch(null)
                        .thenAcceptAsync(pair -> bindToModel(pair), uiExecutor)
        );
    }

    public EventStream<Boolean> validateItem(EventStream<String> textStream) {
        return validateItem(textStream, false);
    }

    public EventStream<Boolean> validateItem(EventStream<String> textStream, boolean jsonOnly) {
        return textStream.successionEnds(Duration.ofMillis(100))
                .map(text -> {
                    try {
                        Map<String, AttributeValue> item = jsonOnly ? Utils.fromRawJson(text) : Utils.fromSimpleJson(text);
                        if (jsonOnly) return true;
                        return item.containsKey(hash()) && (range() == null || item.containsKey(range()));
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    public void onCreateItem(String json, boolean isRaw) {
        eventBus.activity(
                createItem(json, isRaw).thenRun(this::onRefreshData)
        );
    }

    public void onUpdateItem(String json, boolean isRaw) {
        eventBus.activity(
                updateItem(json, isRaw).thenRun(this::onRefreshData)
        );
    }

    public void onDeleteItems(List<Map<String, AttributeValue>> items) {
        eventBus.activity(
                delete(items).thenRun(this::onRefreshData)
        );
    }

    public void onPatchItems(List<Map<String, AttributeValue>> items, String jsonPatch, boolean isRaw) {
        eventBus.activity(
                patchItems(items, jsonPatch, isRaw).thenRun(this::onRefreshData)
        );
    }

    public void onClearFilters() {
        tableModel.getAttributeFilterMap().values().forEach(simpleStringProperty -> simpleStringProperty.set(null));
        onRefreshData();
    }

    public void onSaveToFile(File file) {
        eventBus.activity(
                getAllItems().thenAccept(all -> {
                    try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                        JsonGenerator generator = new JsonFactory(new ObjectMapper()).createGenerator(writer);
                        generator.writeStartArray();
                        all.forEach(map -> {
                            try {
                                generator.writeRawValue(Utils.toSimpleJson(map));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        generator.writeEndArray();
                        generator.flush();
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    public void onLoadFromFile(File file) {
        eventBus.activity(
                runAsync(() -> {
                    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                        JsonNode root = new ObjectMapper().readTree(reader);
                        Observable.fromIterable(root::elements)
                                .map(jsonNode -> Utils.fromSimpleJson(jsonNode.toString()))
                                .buffer(25)
                                .map(list -> {
                                    Map<String, List<WriteRequest>> requestItems = new HashMap<>();
                                    List<WriteRequest> writes = list.stream()
                                            .map(item -> WriteRequest.builder().putRequest(PutRequest.builder().item(item).build()).build())
                                            .toList();
                                    requestItems.put(context.tableName(), writes);
                                    return BatchWriteItemRequest.builder().requestItems(requestItems).build();
                                })
                                .subscribe(dynamoDbClient::batchWriteItem);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }),
                "Can't load json data from the file",
                "Data in the file is not properly formatted or does not correspond to the db schema."
        ).whenComplete((v, throwable) -> onRefreshData());
    }

    private CompletableFuture<List<Map<String, AttributeValue>>> getAllItems() {
        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, AttributeValue>> all = new ArrayList<>();
            Map<String, AttributeValue> lastKey = null;
            do {
                try {
                    Pair<List<Map<String, AttributeValue>>, Map<String, AttributeValue>> pair = executeQueryOrSearch(lastKey).get();
                    all.addAll(pair.getKey());
                    lastKey = pair.getValue();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } while (lastKey != null && !lastKey.isEmpty());
            return all;
        });
    }

    protected CompletableFuture<Pair<List<Map<String, AttributeValue>>, Map<String, AttributeValue>>> executeQueryOrSearch(Map<String, AttributeValue> exclusiveStartKey) {
        if (notBlankFilterValue(hash())) {
            FilterExpression hashFilter = attributeValueToFilter(hash(), tableModel.getAttributeFilterMap().get(hash()).get(), tableModel.getTableDef().getAttributeTypesMap().get(hash()));
            if (hashFilter != null && "#attr = :val".equals(hashFilter.expression.trim())) {
                return queryTableItems(hash(), range(), tableModel.getAttributeFilterMap(), exclusiveStartKey);
            }
        }
        if (tableModel.getOriginalTableDescription().globalSecondaryIndexes() != null) {
            List<GlobalSecondaryIndexDescription> fullProjectionIndexes = tableModel.getOriginalTableDescription().globalSecondaryIndexes().stream()
                    .filter(index -> index.projection().projectionType().equals("ALL"))
                    .toList();
            Optional<GlobalSecondaryIndexDescription> globalIndexOptional = fullProjectionIndexes.stream()
                    .filter(index -> index.keySchema().stream()
                            .allMatch(key -> notBlankFilterValue(key.attributeName())))
                    .findFirst();
            globalIndexOptional = globalIndexOptional.or(() -> fullProjectionIndexes.stream()
                    .filter(index -> Utils.lookUpKeyName(index.keySchema(), KeySchemaType.HASH).map(this::notBlankFilterValue).orElse(false))
                    .findFirst());
            if (globalIndexOptional.isPresent()) {
                GlobalSecondaryIndexDescription indexDescription = globalIndexOptional.get();
                Optional<String> indexHash = Utils.lookUpKeyName(indexDescription.keySchema(), KeySchemaType.HASH);
                Optional<String> indexRange = Utils.lookUpKeyName(indexDescription.keySchema(), KeySchemaType.RANGE);
                if (indexHash.isPresent()) {
                    return queryIndexItems(indexHash.get(), indexRange.orElse(null), indexDescription.indexName(), tableModel.getAttributeFilterMap(), exclusiveStartKey);
                }
            }
        }
        return scanItems(tableModel.getAttributeFilterMap(), exclusiveStartKey);
    }

    private CompletableFuture<Pair<List<Map<String, AttributeValue>>, Map<String, AttributeValue>>> scanItems(Map<String, SimpleStringProperty> attributeFilterMap, Map<String, AttributeValue> exclusiveStartKey) {
        return supplyAsync(() -> {
            List<FilterExpression> filters = attributeFilterMap.entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getValue().get()) && entry.getValue().get().trim().length() > 0)
                    .map(entry -> attributeValueToFilter(entry.getKey(), entry.getValue().get(), tableModel.getTableDef().getAttributeTypesMap().get(entry.getKey())))
                    .filter(Objects::nonNull)
                    .toList();
            String filterExpression = null;
            Map<String, String> names = new HashMap<>();
            Map<String, AttributeValue> values = new HashMap<>();
            if (!filters.isEmpty()) {
                List<String> exps = new ArrayList<>();
                for (FilterExpression f : filters) {
                    exps.add("(" + f.expression + ")");
                    names.putAll(f.names);
                    values.putAll(f.values);
                }
                filterExpression = String.join(" AND ", exps);
            }
            ScanRequest request = ScanRequest.builder().tableName(context.tableName()).filterExpression(filterExpression).expressionAttributeNames(names).expressionAttributeValues(values).limit(PAGE_SIZE).exclusiveStartKey(exclusiveStartKey).build();
            ScanResponse response = dynamoDbClient.scan(request);
            return new Pair<>(response.items(), response.lastEvaluatedKey());
        });
    }

    private CompletableFuture<Pair<List<Map<String, AttributeValue>>, Map<String, AttributeValue>>> queryTableItems(String hashName, String rangeName, Map<String, SimpleStringProperty> attributeFilterMap, Map<String, AttributeValue> exclusiveStartKey) {
        return supplyAsync(() -> {
            String keyCondition = "#hk = :hkval";
            Map<String, String> names = new HashMap<>();
            Map<String, AttributeValue> values = new HashMap<>();
            names.put("#hk", hashName);
            Attributes.Type hType = tableModel.getTableDef().getAttributeTypesMap().get(hashName);
            values.put(":hkval", buildAv(attributeFilterMap.get(hashName).get(), hType));
            if (rangeName != null && notBlankFilterValue(rangeName)) {
                keyCondition += " and #rk = :rkval";
                names.put("#rk", rangeName);
                Attributes.Type rType = tableModel.getTableDef().getAttributeTypesMap().get(rangeName);
                values.put(":rkval", buildAv(attributeFilterMap.get(rangeName).get(), rType));
            }
            List<FilterExpression> filters = attributeFilterMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(hashName) && !entry.getKey().equals(rangeName))
                    .filter(entry -> notBlankFilterValue(entry.getKey()))
                    .map(entry -> attributeValueToFilter(entry.getKey(), entry.getValue().get(), tableModel.getTableDef().getAttributeTypesMap().get(entry.getKey())))
                    .filter(Objects::nonNull)
                    .toList();
            String filterExp = null;
            if (!filters.isEmpty()) {
                List<String> exps = new ArrayList<>();
                for (FilterExpression f : filters) {
                    exps.add("(" + f.expression + ")");
                    names.putAll(f.names);
                    values.putAll(f.values);
                }
                filterExp = String.join(" AND ", exps);
            }
            QueryRequest request = QueryRequest.builder().tableName(context.tableName()).keyConditionExpression(keyCondition).filterExpression(filterExp).expressionAttributeNames(names).expressionAttributeValues(values).limit(PAGE_SIZE).exclusiveStartKey(exclusiveStartKey).build();
            QueryResponse response = dynamoDbClient.query(request);
            return new Pair<>(response.items(), response.lastEvaluatedKey());
        });
    }

    private CompletableFuture<Pair<List<Map<String, AttributeValue>>, Map<String, AttributeValue>>> queryIndexItems(String hashName, String rangeName, String indexName, Map<String, SimpleStringProperty> attributeFilterMap, Map<String, AttributeValue> exclusiveStartKey) {
        return supplyAsync(() -> {
            String keyCondition = "#hk = :hkval";
            Map<String, String> names = new HashMap<>();
            Map<String, AttributeValue> values = new HashMap<>();
            names.put("#hk", hashName);
            Attributes.Type hType = tableModel.getTableDef().getAttributeTypesMap().get(hashName);
            values.put(":hkval", buildAv(attributeFilterMap.get(hashName).get(), hType));
            if (rangeName != null && notBlankFilterValue(rangeName)) {
                keyCondition += " and #rk = :rkval";
                names.put("#rk", rangeName);
                Attributes.Type rType = tableModel.getTableDef().getAttributeTypesMap().get(rangeName);
                values.put(":rkval", buildAv(attributeFilterMap.get(rangeName).get(), rType));
            }
            List<FilterExpression> filters = attributeFilterMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(hashName) && !entry.getKey().equals(rangeName))
                    .filter(entry -> notBlankFilterValue(entry.getKey()))
                    .map(entry -> attributeValueToFilter(entry.getKey(), entry.getValue().get(), tableModel.getTableDef().getAttributeTypesMap().get(entry.getKey())))
                    .filter(Objects::nonNull)
                    .toList();
            String filterExp = null;
            if (!filters.isEmpty()) {
                List<String> exps = new ArrayList<>();
                for (FilterExpression f : filters) {
                    exps.add("(" + f.expression + ")");
                    names.putAll(f.names);
                    values.putAll(f.values);
                }
                filterExp = String.join(" AND ", exps);
            }
            QueryRequest request = QueryRequest.builder().tableName(context.tableName()).indexName(indexName).keyConditionExpression(keyCondition).filterExpression(filterExp).expressionAttributeNames(names).expressionAttributeValues(values).limit(PAGE_SIZE).exclusiveStartKey(exclusiveStartKey).build();
            QueryResponse response = dynamoDbClient.query(request);
            return new Pair<>(response.items(), response.lastEvaluatedKey());
        });
    }

    private AttributeValue buildAv(String value, Attributes.Type type) {
        AttributeValue.Builder b = AttributeValue.builder();
        if (type == Attributes.Type.NUMBER) b.n(value);
        else if (type == Attributes.Type.BOOLEAN) b.bool(Boolean.parseBoolean(value));
        else b.s(value);
        return b.build();
    }

    private FilterExpression attributeValueToFilter(String attributeName, String value, Attributes.Type type) {
        ValueToFilterParser parser = new EqualsParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        parser = new NotEqualsParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        parser = new BeginsWithParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        parser = new ContainsParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        parser = new NotContainsParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        parser = new ExistsParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        parser = new NotExistsParser();
        if (parser.matches(value)) return parser.parse(attributeName, value, type);
        return null;
    }

    private CompletableFuture<Void> processItemAsync(String json, boolean isRaw, Consumer<Map<String, AttributeValue>> command) {
        try {
            Map<String, AttributeValue> item = isRaw ? Utils.fromRawJson(json) : Utils.fromSimpleJson(json);
            return runAsync(() -> command.accept(item));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Void> createItem(String json, boolean isRaw) {
        return processItemAsync(json, isRaw, item -> {
            PutItemRequest request = PutItemRequest.builder().tableName(context.tableName()).item(item).build();
            dynamoDbClient.putItem(request);
        });
    }

    private CompletableFuture<Void> updateItem(String json, boolean isRaw) {
        return processItemAsync(json, isRaw, item -> {
            PutItemRequest request = PutItemRequest.builder().tableName(context.tableName()).item(item).build();
            dynamoDbClient.putItem(request);
        });
    }

    private CompletableFuture<Void> patchItems(List<Map<String, AttributeValue>> items, String jsonPatch, boolean isRaw) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return processItemAsync(jsonPatch, isRaw, patch -> items.forEach(oldItem -> {
            StringBuilder updateExp = new StringBuilder("SET ");
            Map<String, String> names = new HashMap<>();
            Map<String, AttributeValue> values = new HashMap<>();
            int i = 0;
            for (Map.Entry<String, AttributeValue> entry : patch.entrySet()) {
                String attr = entry.getKey();
                String attrPH = "#a" + i;
                String valPH = ":v" + i;
                if (i > 0) updateExp.append(", ");
                updateExp.append(attrPH + " = " + valPH);
                names.put(attrPH, attr);
                values.put(valPH, entry.getValue());
                i++;
            }
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(hash(), oldItem.get(hash()));
            if (range() != null) key.put(range(), oldItem.get(range()));
            UpdateItemRequest request = UpdateItemRequest.builder().tableName(context.tableName()).key(key).updateExpression(updateExp.toString()).expressionAttributeNames(names).expressionAttributeValues(values).build();
            dynamoDbClient.updateItem(request);
        }));
    }

    private CompletableFuture<Void> delete(List<Map<String, AttributeValue>> items) {
        return runAsync(() -> Observable.fromIterable(items)
                .buffer(25)
                .map(list -> {
                    Map<String, List<WriteRequest>> requestItems = new HashMap<>();
                    List<WriteRequest> writes = list.stream()
                            .map(item -> {
                                Map<String, AttributeValue> key = new HashMap<>();
                                key.put(hash(), item.get(hash()));
                                if (range() != null) key.put(range(), item.get(range()));
                                return WriteRequest.builder().deleteRequest(DeleteRequest.builder().key(key).build()).build();
                            })
                            .toList();
                    requestItems.put(context.tableName(), writes);
                    return BatchWriteItemRequest.builder().requestItems(requestItems).build();
                })
                .subscribe(dynamoDbClient::batchWriteItem)
        );
    }

    private boolean notBlankFilterValue(String attr) {
        SimpleStringProperty property = tableModel.getAttributeFilterMap().get(attr);
        return property != null && !StringUtils.isEmpty(property.get());
    }

    private String hash() {
        return tableModel.getTableDef().getHashAttribute();
    }

    private String range() {
        return tableModel.getTableDef().getRangeAttribute();
    }

    public void openUrl(String url) {
        hostServices.showDocument(url);
    }

    private void applyContext() {
        if (context.propertyName() != null && context.propertyValue() != null) {
            tableModel.getAttributeFilterMap().computeIfAbsent(context.propertyName(), __ -> new SimpleStringProperty()).set(context.propertyValue());
        }
    }
}
