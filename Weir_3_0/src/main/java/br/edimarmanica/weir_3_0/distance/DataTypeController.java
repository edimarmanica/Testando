/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author edimar
 */
public class DataTypeController {

    public static String getPattern(DataType type) {
        /**
         * Manter a ordem uma vez que é uma hierarquia
         */
        switch (type) {
            case DATE:
                return "(\\d+(-|/)\\d+(-|/)\\d+)|(.*Jan .*|.*Feb .*|.*Mar .*|.*Apr .*|.*Aug .*|.*Sept .*|.*Oct .*|.*Nov .*|.*Dec .*|.*January.*|.*February.*|.*March.*|.*April.*|.*May.*|.*June.*|.*July.*|.*August.*|.*September.*|.*October.*|.*November.*|.*December.*)|(\\d{4}-\\d{2})";
            case ISBN:
                return "(\\d{3}(-)?)?\\d{10}";
            case PHONE:
                return "(\\()?0?x?x?(\\d{2})?\\s?\\d{4}-\\d{4}"; //(0xx11) 3396-4525
            case CURRENCY:
                return "(\\$|€|EUR|USD|R\\$)(\\s)?\\d+.((,|.)\\d+)*";
            case LENGHT:
                return "\\d+((,|\\.)\\d+)*\\s*(m|cm|km|ft|'|yd|in|'')";
            case WEIGHT:
                return "\\d+((,|\\.)\\d+)*\\s*(kg|g)";
            case NUMBER:
                return "\\d+((,|\\.)\\d+)*";
            case STRING:
                return ".*";
            default:
                return null;         //String
        }
    }

    public static DataType getDataType(String value) {
        String aux = normalize(value);
        for (DataType type : DataType.values()) {
            if (value.matches(getPattern(type))) {
                return type;
            }
        }

        return DataType.STRING;
    }

    public static String normalize(String value) {
        String aux = value.toLowerCase();
        aux = aux.trim();
        aux = aux.replaceAll("\\s+", " ");
        return aux;
    }

    /**
     *
     * @param r
     * @return DataType mais frequente (Primeiro acha os data type mais
     * específicos para cada valor, depois acha o data type que mais ocorre. No
     * WEIR original achava o datatype mais genérico entre esses, porém os
     * vários formatos de data no mesmo site (só mês e ano, ex.) dava problemas)
     */
    public static DataType getMostFrequentType(Rule r) {
        Map<DataType, Integer> dataTypes = new HashMap<>();
        for (Value v : r.getValues()) {
            if (v.getValue() == null){
                continue; //ignora
            }
            DataType type = getDataType(v.getValue());

            if (dataTypes.containsKey(type)) {
                dataTypes.put(type, dataTypes.get(type) + 1);
            } else {
                dataTypes.put(type, 1);
            }
        }

        int maxOccurrence = 0;
        DataType maxType = null;
        for (DataType type: dataTypes.keySet()) { 
            if (maxOccurrence < dataTypes.get(type)) {
                maxOccurrence = dataTypes.get(type);
                maxType = type;
            }
        }

        return maxType;
    }

    /**
     *
     * @param r1
     * @param r2
     * @return more specific type in common between the more specif type of each
     * rule
     */
    public static DataType getMostSpecificType(Rule r1, Rule r2) {
        /**
         * Hieraquia
         */
        DataType tR1 = r1.getType();
        DataType tR2 = r2.getType();

        if (tR1 == tR2) {
            return tR1;
        }

        if ((tR1 == DataType.CURRENCY || tR1 == DataType.LENGHT || tR1 == DataType.WEIGHT || tR1 == DataType.NUMBER)
                && (tR2 == DataType.CURRENCY || tR2 == DataType.LENGHT || tR2 == DataType.WEIGHT || tR2 == DataType.NUMBER)) {
            return DataType.NUMBER;
        }

        return DataType.STRING;
    }
}
