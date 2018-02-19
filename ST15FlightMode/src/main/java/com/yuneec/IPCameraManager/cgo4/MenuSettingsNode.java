package com.yuneec.IPCameraManager.cgo4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class MenuSettingsNode {
    public final HashMap<String, MenuSettingsItemContainer> containers = new HashMap();
    public final List<MenuSettingsItem> items = new ArrayList();
    public final String nodeName;

    public MenuSettingsNode(String nodeName) {
        this.nodeName = nodeName;
    }

    public int hashCode() {
        return (this.nodeName.hashCode() + this.items.size()) + this.containers.size();
    }

    public boolean equals(Object compareValue) {
        if (!(compareValue instanceof MenuSettingsNode)) {
            return false;
        }
        if (!this.nodeName.equals(((MenuSettingsNode) compareValue).nodeName)) {
            return false;
        }
        List<MenuSettingsItem> compareItems = ((MenuSettingsNode) compareValue).items;
        if (this.items.size() != compareItems.size()) {
            return false;
        }
        int index = 0;
        for (MenuSettingsItem item : this.items) {
            int index2 = index + 1;
            if (!item.equals(compareItems.get(index))) {
                return false;
            }
            index = index2;
        }
        HashMap<String, MenuSettingsItemContainer> campareContainerList = ((MenuSettingsNode) compareValue).containers;
        Iterator<?> it = this.containers.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MenuSettingsItemContainer> pair = (Entry) it.next();
            MenuSettingsItemContainer campareContainer = (MenuSettingsItemContainer) campareContainerList.get(pair.getKey());
            if (campareContainer == null || !campareContainer.equals((MenuSettingsItemContainer) pair.getValue())) {
                return false;
            }
            it.remove();
        }
        return true;
    }
}
