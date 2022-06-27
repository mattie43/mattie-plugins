package com.example.nseersvillage;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.queries.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.Plugin;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static java.awt.event.KeyEvent.*;

@Slf4j
public class ExtTools extends Plugin {
    @Inject
    private Client client;


    public int[] stringToIntArray(String string)
    {
        return Arrays.stream(string.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    @Nullable
    public NPC findNearestNPC(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findNearestWallObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new WallObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public DecorativeObject findNearestDecorObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GroundObject findNearestGroundObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GroundObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public java.util.List<GameObject> getGameObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public java.util.List<WallObject> getWallObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new WallObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public java.util.List<DecorativeObject> getDecorObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public java.util.List<GroundObject> getGroundObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new GroundObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    @Nullable
    public TileObject findNearestObject(int id)
    {
        GameObject gameObject = findNearestGameObject(id);

        if (gameObject != null)
        {
            return gameObject;
        }

        WallObject wallObject = findNearestWallObject(id);

        if (wallObject != null)
        {
            return wallObject;
        }
        DecorativeObject decorativeObject = findNearestDecorObject(id);

        if (decorativeObject != null)
        {
            return decorativeObject;
        }

        return findNearestGroundObject(id);
    }

    @Nullable
    public boolean findAndClickNearestObj(int[] object_ids)
    {
        if (object_ids.length < 1)
        {
            return false;
        }

        Player player = client.getLocalPlayer();
        if (client.getLocalPlayer() == null)
        {
            return false;
        }

        int smallest_dist = Integer.MAX_VALUE;
        TileObject object_to_click = null;
        for (int id : object_ids) {
            TileObject tmp = findNearestObject(id);
            if (tmp == null)
            {
                continue;
            }

            int dist = player.getLocalLocation().distanceTo(tmp.getLocalLocation());
            if (dist < smallest_dist)
            {
                object_to_click = tmp;
                smallest_dist = dist;
            }
        }

        if(object_to_click == null || smallest_dist > 4000 )
            return false;

        clickTileObject(object_to_click);

        return true;
    }


    public java.util.List<WidgetItem> getItems(int... itemIDs)
    {
        assert client.isClientThread();

        return new InventoryWidgetItemQuery()
                .idEquals(itemIDs)
                .result(client)
                .list;
    }

    public java.util.List<Widget> getEquippedItems(int[] itemIds)
    {
        assert client.isClientThread();

        Widget equipmentWidget = client.getWidget(WidgetInfo.EQUIPMENT);

        java.util.List<Integer> equippedIds = new ArrayList<>();

        for (int i : itemIds)
        {
            equippedIds.add(i);
        }

        java.util.List<Widget> equipped = new ArrayList<>();

        if (equipmentWidget.getStaticChildren() != null)
        {
            for (Widget widgets : equipmentWidget.getStaticChildren())
            {
                for (Widget items : widgets.getDynamicChildren())
                {
                    if (equippedIds.contains(items.getItemId()))
                    {
                        equipped.add(items);
                    }
                }
            }
        }

        return equipped;
    }
	/*
	Series of Functions that click specific things in the world.
	 */

    //Clicks the given widget
    public void clickWidget(Widget widget)
    {
        assert client.isClientThread();

        Rectangle widget_rect = widget.getBounds();
        if (widget_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 420));
                click(widget_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }

    //Clicks the closest NPC that matches the id's given
    public void clickNPC(int[] NPC_IDS)
    {
        assert client.isClientThread();

        NPC pickpocket_npc = findNearestNPC(NPC_IDS); //Change this to Ardy Knights
        if (pickpocket_npc == null)
            return;

        Shape npc_shape = pickpocket_npc.getConvexHull();
        if (npc_shape == null)
            return;

        Rectangle npc_rect = npc_shape.getBounds();
        if (npc_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(npc_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }

    //clicks the closest wall object that matches the IDS given
    public void clickWallObject(int[] WALL_IDS)
    {
        assert client.isClientThread();

        WallObject near_wall_obj = findNearestWallObject(WALL_IDS); //Change this to Ardy Knights
        if (near_wall_obj == null){
            log.info("Wall obj not found");
            return;
        }

        Shape npc_shape = near_wall_obj.getConvexHull();
        if (npc_shape == null)
            return;

        Rectangle npc_rect = npc_shape.getBounds();
        if (npc_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(npc_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }
    // Attempt to create clickGameObject
    public void clickGameObject(int GAME_OBJ_ID)
    {
        assert client.isClientThread();

        GameObject game_obj = findNearestGameObject(GAME_OBJ_ID);
        if (game_obj == null){
            log.info("Game obj not found");
            return;
        }

        Shape game_obj_shape = game_obj.getConvexHull();
        if (game_obj_shape == null)
            return;

        Rectangle game_obj_rect = game_obj_shape.getBounds();
        if (game_obj_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(game_obj_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }
    // attempt to click decor obj
    public void clickDecorObject(int DECOR_OBJ_ID)
    {
        assert client.isClientThread();

        DecorativeObject decor_obj = findNearestDecorObject(DECOR_OBJ_ID);
        if (decor_obj == null){
            log.info("Decor obj not found");
            return;
        }

        Shape decor_obj_shape = decor_obj.getConvexHull();
        if (decor_obj_shape == null)
            return;

        Rectangle game_obj_rect = decor_obj_shape.getBounds();
        if (game_obj_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(game_obj_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }
    // attempt to click ground obj
    public void clickGroundObject(int GROUND_OBJ_ID)
    {
        assert client.isClientThread();

        GroundObject ground_obj = findNearestGroundObject(GROUND_OBJ_ID);
        if (ground_obj == null){
            log.info("Decor obj not found");
            return;
        }

        Shape ground_obj_shape = ground_obj.getConvexHull();
        if (ground_obj_shape == null)
            return;

        Rectangle game_obj_rect = ground_obj_shape.getBounds();
        if (game_obj_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(game_obj_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }
    // attempt to click tile obj
    public void clickTileObject(TileObject TILE_OBJ)
    {
        assert client.isClientThread();

        Shape tile_obj_shape = TILE_OBJ.getClickbox();
        if (tile_obj_shape == null)
            return;

        Rectangle tile_obj_rect = tile_obj_shape.getBounds();
        if (tile_obj_rect == null)
            return;

        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(tile_obj_rect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }

    /**
     * This method must be called on a new
     * thread, if you try to call it on
     * {@link net.runelite.client.callback.ClientThread}
     * it will result in a crash/desynced thread.
     */
    public void typeString(String string)
    {
        assert !client.isClientThread();

        for (char c : string.toCharArray())
        {
            pressKey(c);
        }
    }

    public void pressKey(char key)
    {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    public void keyEvent(int id, char key)
    {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );

        client.getCanvas().dispatchEvent(e);
    }

    public void pressArrowKey(String direction, int millis)
    {
//      KEY_PRESSED -> 401 | KEY_RELEASED -> 402 | KEY_TYPED -> 400
        assert client.isClientThread();

        arrowKeyEvent(401, direction);
        (new Thread(() ->
        {
            try
            {
                Thread.sleep(millis);
                arrowKeyEvent(402, direction);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }

    public void arrowKeyEvent(int id, String direction)
    {
        int getDirection = KeyEvent.VK_UP;
        switch(direction){
            case "DOWN":
                getDirection = KeyEvent.VK_DOWN;
                break;
            case "LEFT":
                getDirection = KeyEvent.VK_LEFT;
                break;
            case "RIGHT":
                getDirection = KeyEvent.VK_RIGHT;
                break;
            default:
                break;
        }

        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, getDirection, CHAR_UNDEFINED
        );
        client.getCanvas().dispatchEvent(e);
    }

    /**
     * This method must be called on a new
     * thread, if you try to call it on
     * {@link net.runelite.client.callback.ClientThread}
     * it will result in a crash/desynced thread.
     */
    public void click(Rectangle rectangle)
    {
        assert !client.isClientThread();
        Point point = getClickPoint(rectangle);
        try
        {
            click(point);
        }
        catch (Exception ignored)
        {
            //Just return it. Should never get hit anyway tbh.
        }
    }

    public void click(Point p) throws InterruptedException
    {
        assert !client.isClientThread();

        if (client.isStretchedEnabled())
        {
            final Dimension stretched = client.getStretchedDimensions();
            final Dimension real = client.getRealDimensions();
            final double width = (stretched.width / real.getWidth());
            final double height = (stretched.height / real.getHeight());
            final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
            mouseEvent(503, point);
            Thread.sleep(getRandomIntBetweenRange(50, 200));
            mouseEvent(501, point);
            mouseEvent(502, point);
            mouseEvent(500, point);
            return;
        }
        mouseEvent(503, p);
        Thread.sleep(getRandomIntBetweenRange(50, 200));
        mouseEvent(501, p);
        mouseEvent(502, p);
        mouseEvent(500, p);
    }

    public Point getClickPoint(Rectangle rect)
    {
        final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
        final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

        return new Point(x, y);
    }

    public void clickTile(LocalPoint localPoint, int plane) {
        Point tilePoint = Perspective.localToCanvas(client, localPoint, plane);
        Rectangle tileRect = new Rectangle(tilePoint.getX(), tilePoint.getY(), 5, 5);
        (new Thread(() ->
        {
            try
            {
                Thread.sleep(getRandomIntBetweenRange(142, 523));
                click(tileRect);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        })).start();
    }

    public int getRandomIntBetweenRange(int min, int max)
    {
        return (int) ((Math.random() * ((max - min) + 1)) + min);
    }

    public void mouseEvent(int id,Point point)
    {
        MouseEvent e = new MouseEvent(
                client.getCanvas(), id,
                System.currentTimeMillis(),
                0, point.getX(), point.getY(),
                1, false, 1
        );

        client.getCanvas().dispatchEvent(e);
    }
}
