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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.Table;
import javafx.application.HostServices;
import javafx.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ua.org.java.dynamoit.EventBus;
import ua.org.java.dynamoit.components.main.MainModel;
import ua.org.java.dynamoit.db.DynamoDBService;
import ua.org.java.dynamoit.model.TableDef;
import ua.org.java.dynamoit.model.profile.PreconfiguredProfileDetails;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

@PrepareForTest(HostServices.class)
@RunWith(PowerMockRunner.class)
public class TableGridControllerTest {

    @Test
    @org.junit.Ignore("Test disabled temporarily")
    public void onRefreshData() {
        TableGridContext context = new TableGridContext(
                new PreconfiguredProfileDetails("profile1", "region1"),
                "table1"
        );
        MainModel mainModel = new MainModel();
        mainModel.addProfile(new PreconfiguredProfileDetails("profile1", "region1"));
        mainModel.addProfile(new PreconfiguredProfileDetails("profile2", "region2"));
        TableDef tableDef = new TableDef("Table1");
        tableDef.setHashAttribute("hash_attr");
        TableGridModel model = new TableGridModel(mainModel.getAvailableProfiles().get("profile1"));
        model.setTableDef(tableDef);
        model.getRows().add(new Item());

        Table table = mock(Table.class);
        AmazonDynamoDB amazonDynamoDB = mock(AmazonDynamoDB.class);
        DynamoDB dynamoDB = mock(DynamoDB.class);
        expect(dynamoDB.getTable(context.tableName())).andReturn(table);
        DynamoDBService dynamoDBService = mock(DynamoDBService.class);
        expect(dynamoDBService.getOrCreateDynamoDBClient(context.profileDetails())).andReturn(amazonDynamoDB);
        expect(dynamoDBService.getOrCreateDocumentClient(context.profileDetails())).andReturn(dynamoDB);

        Page<Item, Object> page = mock(Page.class);
        HostServices hostServices = mock(HostServices.class);

        EventBus eventBus = new EventBus(ForkJoinPool.commonPool());

        replay(table, amazonDynamoDB, dynamoDB, dynamoDBService, page);

        TableGridController controller = partialMockBuilder(TableGridController.class)
                .withConstructor(context, model, dynamoDBService, eventBus, ForkJoinPool.commonPool(), hostServices)
                .addMockedMethod("queryPageItems")
                .createMock();

        expect(controller.queryPageItems()).andReturn(CompletableFuture.completedFuture(
                new Pair<>(List.of(new Item(), new Item()), page)
        ));

        replay(controller);

        controller.onRefreshData().join();

        verify(table, amazonDynamoDB, dynamoDB, dynamoDBService, page, controller);

        assertEquals(model.getRowsSize(), 2);
        assertEquals(model.getCurrentPage(), page);
    }
}
