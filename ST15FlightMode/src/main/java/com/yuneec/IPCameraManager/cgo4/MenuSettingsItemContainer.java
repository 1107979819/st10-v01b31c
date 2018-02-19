package com.yuneec.IPCameraManager.cgo4;

import java.util.ArrayList;
import java.util.List;

public class MenuSettingsItemContainer extends MenuSettingsItem {
    public final List<MenuSettingsItem> itemList;
    public final String value;
    public String value2;

    public MenuSettingsItemContainer(String itemId, boolean isEnable, String value) {
        super(itemId, isEnable);
        this.value = value;
        this.itemList = new ArrayList();
    }

    public MenuSettingsItemContainer(String itemId, boolean isEnable, String value, String value2) {
        this(itemId, isEnable, value);
        this.value2 = value2;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (this.value == null ? 0 : this.value.hashCode()) + (this.id.hashCode() + (this.isEnable ? 10000000 : 0));
        if (this.value2 != null) {
            i = this.value2.hashCode() + this.itemList.size();
        }
        return hashCode + i;
    }

    public boolean equals(Object compareValue) {
        if (!(compareValue instanceof MenuSettingsItemContainer)) {
            return false;
        }
        if (!this.id.equals(((MenuSettingsItemContainer) compareValue).id)) {
            return false;
        }
        if (this.isEnable != ((MenuSettingsItemContainer) compareValue).isEnable) {
            return false;
        }
        if (!this.value.equals(((MenuSettingsItemContainer) compareValue).value)) {
            return false;
        }
        List<MenuSettingsItem> compareItemList = ((MenuSettingsItemContainer) compareValue).itemList;
        if (this.itemList.size() != compareItemList.size()) {
            return false;
        }
        int index = 0;
        for (MenuSettingsItem subItem : this.itemList) {
            int index2 = index + 1;
            if (!subItem.equals(compareItemList.get(index))) {
                return false;
            }
            index = index2;
        }
        return true;
    }
}
