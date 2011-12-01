/*
 * PointScreen.java
 * 
 * Copyright � 1998-2011 Research In Motion Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.gpsdemo;

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

import com.rim.samples.device.gpsdemo.GPSDemo.WayPoint;

/**
 * A screen to render the saved WayPoints.
 */
class PointScreen extends MainScreen implements ListFieldCallback {
    private final Vector _points;
    private final ListField _listField;

    PointScreen(final Vector points) {

        final LabelField title = new LabelField("Previous waypoints");
        setTitle(title);

        _points = points;
        _listField = new ListField();
        _listField.setCallback(this);
        add(_listField);
        reloadWayPointList();
    }

    private void reloadWayPointList() {
        // Refreshes wayPoint list on screen.
        _listField.setSize(_points.size());
    }

    private void displayWayPoint() {
        final int index = _listField.getSelectedIndex();
        final ViewScreen screen =
                new ViewScreen((WayPoint) _points.elementAt(index), index);
        UiApplication.getUiApplication().pushModalScreen(screen);
    }

    /**
     * Overrides method in super class.
     * 
     * @see net.rim.device.api.ui.Screen#makeMenu(Menu, int)
     */
    protected void makeMenu(final Menu menu, final int instance) {
        if (!_listField.isEmpty()) {
            menu.add(_viewPointAction);
            menu.add(_deletePointAction);
        }
        super.makeMenu(menu, instance);
    }

    /**
     * Overrides method in super class.
     * 
     * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
     */
    protected boolean keyChar(final char key, final int status, final int time) {
        // Intercept the ENTER key.
        if (key == Characters.ENTER) {
            displayWayPoint();
            return true;
        }
        return super.keyChar(key, status, time);
    }

    /**
     * Handles a trackball click and provides identical behavior to an ENTER
     * keypress event.
     * 
     * @see net.rim.device.api.ui.Screen#invokeAction(int)
     */
    protected boolean invokeAction(final int action) {
        switch (action) {
        case ACTION_INVOKE: // Trackball click.
            displayWayPoint();
            return true; // We've consumed the event.
        }
        return super.invokeAction(action);
    }

    // ListFieldCallback methods
    // -----------------------------------------------------------------------------------
    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#drawListRow(ListField
     *      , Graphics , int , int , int)
     */
    public void drawListRow(final ListField listField, final Graphics graphics,
            final int index, final int y, final int width) {
        if (listField == _listField && index < _points.size()) {
            final String name = "Waypoint " + index;
            graphics.drawText(name, 0, y, 0, width);
        }
    }

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#get(ListField ,
     *      int)
     */
    public Object get(final ListField listField, final int index) {
        if (listField == _listField) {
            // If index is out of bounds an exception will be thrown, but that's
            // the behaviour we want in that case.
            return _points.elementAt(index);
        }

        return null;
    }

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#getPreferredWidth(ListField)
     */
    public int getPreferredWidth(final ListField listField) {
        // Use all the width of the current LCD.
        return Display.getWidth();
    }

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#indexOfList(ListField
     *      , String , int)
     */
    public int indexOfList(final ListField listField, final String prefix,
            final int start) {
        return -1; // Not implemented.
    }

    // Menu items
    // ---------------------------------------------------------------
    MenuItem _viewPointAction = new MenuItem("View", 100000, 10) {
        public void run() {
            displayWayPoint();
        }
    };

    MenuItem _deletePointAction = new MenuItem("Delete", 100000, 11) {
        public void run() {

            final int result =
                    Dialog.ask(Dialog.DELETE, "Delete Waypoint "
                            + _listField.getSelectedIndex() + "?");
            if (result == Dialog.YES) {
                GPSDemo.removeWayPoint((WayPoint) _points.elementAt(_listField
                        .getSelectedIndex()));
                reloadWayPointList();
            }
        }
    };

    /**
     * A screen to render a particular Waypoint.
     */
    private static class ViewScreen extends MainScreen {
        ViewScreen(final WayPoint point, final int count) {
            final LabelField title = new LabelField("Waypoint" + count);
            setTitle(title);

            Date date = new Date(point._startTime);
            final String startTime = date.toString();
            date = new Date(point._endTime);
            final String endTime = date.toString();

            final float avgSpeed =
                    point._distance / (point._endTime - point._startTime);

            add(new RichTextField("Start: " + startTime, Field.NON_FOCUSABLE));
            add(new RichTextField("End: " + endTime, Field.NON_FOCUSABLE));
            add(new RichTextField("Horizontal Distance (m): "
                    + Float.toString(point._distance), Field.NON_FOCUSABLE));
            add(new RichTextField("Vertical Distance (m): "
                    + Float.toString(point._verticalDistance),
                    Field.NON_FOCUSABLE));
            add(new RichTextField("Average Speed(m/s): "
                    + Float.toString(avgSpeed), Field.NON_FOCUSABLE));
        }
    }
}
