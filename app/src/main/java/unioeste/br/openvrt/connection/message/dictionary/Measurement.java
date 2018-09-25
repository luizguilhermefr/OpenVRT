package unioeste.br.openvrt.connection.message.dictionary;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import unioeste.br.openvrt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public enum Measurement {
    KG_HA("KG_HA"), L_HA("L_HA");

    public final String code;

    Measurement(final String code) {
        this.code = code;
    }

    @NonNull
    public static HashMap<Measurement, String> translationMap(Context context) {
        HashMap<Measurement, String> map = new HashMap<>();
        Resources resources = context.getResources();
        map.put(KG_HA, resources.getString(R.string.kgs_per_hectare));
        map.put(L_HA, resources.getString(R.string.liters_per_hectare));
        return map;
    }

    @Nullable
    public static Measurement fromIndex(int index, Context context) {
        Set<Measurement> keySet = translationMap(context).keySet();
        ArrayList<Measurement> measurementsList = new ArrayList<>(keySet);
        if (index < 0 || index >= measurementsList.size()) {
            return null;
        }
        return measurementsList.get(index);
    }
}
