package com.lac.pucrio.luizpitta.iotrade.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Classe que contêm métodos úteis para calculo de atributos relacionados a interface
 *
 * @author Luiz Guilherme Pitta
 */
public class Utilities {

    /**
     * Método responsável por converter a unidade de DP para Pixel
     *
     * @return quantidade de pixel.
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
