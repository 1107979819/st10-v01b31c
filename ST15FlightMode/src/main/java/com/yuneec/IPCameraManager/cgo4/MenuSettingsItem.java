package com.yuneec.IPCameraManager.cgo4;

public class MenuSettingsItem {
    public final String id;
    public final boolean isEnable;
    public String option;
    public String option2;

    public MenuSettingsItem(String itemId, boolean isEnable) {
        this.option = null;
        this.option2 = null;
        this.id = itemId;
        this.isEnable = isEnable;
    }

    public MenuSettingsItem(String itemId, boolean isEnable, String option) {
        this(itemId, isEnable);
        this.option = option;
    }

    public MenuSettingsItem(String itemId, boolean isEnable, String option, String option2) {
        this(itemId, isEnable, option);
        this.option2 = option2;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (this.isEnable ? 10000000 : 0) + this.id.hashCode();
        if (this.option != null) {
            int hashCode2 = this.option.hashCode();
            if (this.option2 != null) {
                i = this.option2.hashCode();
            }
            i += hashCode2;
        }
        return hashCode + i;
    }

    public boolean equals(Object compareValue) {
        if (!(compareValue instanceof MenuSettingsItem)) {
            return false;
        }
        if (!this.id.equals(((MenuSettingsItem) compareValue).id)) {
            return false;
        }
        if (this.isEnable != ((MenuSettingsItem) compareValue).isEnable) {
            return false;
        }
        return true;
    }
}
