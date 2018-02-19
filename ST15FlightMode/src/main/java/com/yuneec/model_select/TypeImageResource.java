package com.yuneec.model_select;

import android.content.Context;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;

public class TypeImageResource {
    private static int[] multicopter_image_ST10 = new int[]{R.drawable.mode_select_type_image_1, R.drawable.mode_select_type_image_2, R.drawable.mode_select_type_image_3};
    private static int[] multicopter_image_ST12 = new int[]{R.drawable.mode_select_type_image_1, R.drawable.mode_select_type_image_2, R.drawable.mode_select_type_image_3, R.drawable.mode_select_type_image_4, R.drawable.mode_select_type_image_5, R.drawable.mode_select_type_image_6};
    private static int[] multicopter_image_name_ST10 = new int[]{R.string.str_model_type_1, R.string.str_model_type_2, R.string.str_model_type_3};
    private static int[] multicopter_image_name_ST12 = new int[]{R.string.str_model_type_1, R.string.str_model_type_2, R.string.str_model_type_3, R.string.str_model_type_4, R.string.str_model_type_5, R.string.str_model_type_6};

    public static int typeTransformToImageId(Context context, int type) {
        int i;
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            for (i = 0; i < multicopter_image_ST12.length; i++) {
                if (type == (i + DataProviderHelper.MODEL_TYPE_MULITCOPTER_BASE) + 1) {
                    return multicopter_image_ST12[i];
                }
            }
            return 0;
        } else if (!Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            return 0;
        } else {
            for (i = 0; i < multicopter_image_ST10.length; i++) {
                if (type == (i + DataProviderHelper.MODEL_TYPE_MULITCOPTER_BASE) + 1) {
                    return multicopter_image_ST10[i];
                }
            }
            return 0;
        }
    }

    public static int getTypeCount(int type) {
        int[] array = null;
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            array = multicopter_image_ST12;
        } else if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            array = multicopter_image_ST10;
        }
        return array.length;
    }

    public static String getTypeName(Context context, int type) {
        int strId = 0;
        int i;
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            for (i = 0; i < multicopter_image_ST12.length; i++) {
                if (type == (i + DataProviderHelper.MODEL_TYPE_MULITCOPTER_BASE) + 1) {
                    strId = multicopter_image_name_ST12[i];
                    break;
                }
            }
        } else if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            for (i = 0; i < multicopter_image_ST10.length; i++) {
                if (type == (i + DataProviderHelper.MODEL_TYPE_MULITCOPTER_BASE) + 1) {
                    strId = multicopter_image_name_ST10[i];
                    break;
                }
            }
        }
        if (strId != 0) {
            return context.getResources().getString(strId);
        }
        return null;
    }
}
