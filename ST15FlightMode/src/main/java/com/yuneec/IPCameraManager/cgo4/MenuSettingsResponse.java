package com.yuneec.IPCameraManager.cgo4;

import java.util.List;

public class MenuSettingsResponse extends ResponseResult {
    private final int MAX_LIST_SIZE = 100;
    public final List<MenuSettingsNode> menuNodes;

    public MenuSettingsResponse(boolean isOk, List<MenuSettingsNode> menuNodes) {
        super(isOk);
        this.menuNodes = menuNodes;
    }

    public int hashCode() {
        if (this.isOk) {
            return (100 % this.menuNodes.size()) + 100;
        }
        return 100 % this.menuNodes.size();
    }

    public boolean equals(Object compareResponse) {
        return false;
    }

    public MenuSettingsItemContainer findSettingItemWithKey(String key) {
        if (key == null || this.menuNodes == null) {
            return null;
        }
        for (MenuSettingsNode menuNode : this.menuNodes) {
            MenuSettingsItemContainer container = (MenuSettingsItemContainer) menuNode.containers.get(key);
            if (container != null) {
                return container;
            }
        }
        return null;
    }
}
