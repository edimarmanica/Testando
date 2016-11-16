/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edimar
 */
public class DateDistance extends TypeAwareDistance {

    private static final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private static final String[] monthsAbrev = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

    /**
     * 0 if vR1 == vS1, 1 otherwise
     *
     * @param vR1
     * @param vS1
     * @return
     */
    @Override
    public double distanceSpecific(String vR1, String vS1) {
        //testando formatos diferentes -- pelo menos 1 combinação deve ser igual
        try {
            List<String> vR1formats = getAllFormats(vR1);
            List<String> vS1formats = getAllFormats(vS1);

            for (String fr : vR1formats) {
                for (String fs : vS1formats) {
                    if (fr.equals(fs)) {
                        return 0;
                    }
                }
            }
        } catch (NoiseException ex) {
            return 1;//regra pegou lixo. Por ex: Audio Cassete
        }

        return 1;

    }

    /**
     *
     * @param date
     * @return all formats possible to this date
     */
    private static List<String> getAllFormats(String date) throws NoiseException {
        // 01/01/2012 or 1/1/2012
        if (date.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            return getAllFormats01(date);
        }

        // 2012-01-01 or 2012-1-1
        if (date.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            return getAllFormats01(date);
        }

        // 01/2012 or 1/2012
        if (date.matches("\\d{1,2}/\\d{4}")) {
            return getAllFormats02(date);
        }

        // 2012-01 or 2012-1
        if (date.matches("\\d{1,4}-\\d{1,2}")) {
            return getAllFormats02(date);
        }

        // 2012
        if (date.matches("\\d{4}")) {
            return getAllFormats03(date);
        }

        //05 Oct 1995 or 5 Oct 1995
        if (date.matches("\\d{1,2} [a-zA-Z]+ \\d{4}")) {
            return getAllFormats04(date);
        }

        // "October 23, 1995" or "October 5, 1995"
        if (date.matches("[a-zA-Z]+ \\d{1,2}, \\d{4}")) {
            return getAllFormats05(date);
        }

        //"January 1996" 
        if (date.matches("[a-zA-Z]+ \\d{4}")) {
            return getAllFormats06(date);
        }

        throw new NoiseException(date, DataType.DATE);
    }

    /**
     * Formats: 2011-12-12 ou 01/02/2012
     *
     * @param date
     * @return
     */
    private static List<String> getAllFormats01(String date) {
        String partes[] = date.split("(-|/)");
        List<String> allFormats = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if (i != j && i != k && j != k) {
                        allFormats.add(Integer.valueOf(partes[i]).toString() + Integer.valueOf(partes[j]).toString() + Integer.valueOf(partes[k]).toString());
                    }

                }
            }

        }
        return allFormats;
    }

    /**
     * Formats: 2011-12 ou 02/2012
     *
     * @param date
     * @return
     */
    private static List<String> getAllFormats02(String date) {
        String partes[] = date.split("(-|/)");
        List<String> allFormats = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int day = 1; day <= 31; day++) {
                    if (i != j) {
                        allFormats.add(Integer.valueOf(partes[i]).toString() + Integer.valueOf(partes[j]).toString() + day);
                    }

                }
            }

        }
        return allFormats;
    }

    /**
     * Format: 2012
     *
     * @param date
     * @return
     */
    private static List<String> getAllFormats03(String date) {

        List<String> allFormats = new ArrayList<>();

        for (int month = 1; month < 13; month++) {
            for (int day = 1; day <= 31; day++) {
                allFormats.add(Integer.valueOf(date).toString() + month + "" + day);
                allFormats.add(month + Integer.valueOf(date).toString() + day);
                allFormats.add(month + "" + day + Integer.valueOf(date).toString());
            }
        }
        return allFormats;
    }

    /**
     * Formats: "05 Oct 1995" or "5 Oct 1995" or "05 October 1995"
     *
     * @param date
     * @return
     */
    private static List<String> getAllFormats04(String date) throws NoiseException {
        List<String> allFormats = new ArrayList<>();

        String[] partes = date.trim().split(" ");
        for (int i = 0; i < monthsAbrev.length; i++) {
            if (partes[1].equals(monthsAbrev[i])) {
                return getAllFormats01(Integer.valueOf(partes[2]) + "-" + (i + 1) + "-" + partes[0]);
            }
        }

        for (int i = 0; i < months.length; i++) {
            if (partes[1].equals(months[i])) {
                return getAllFormats01(Integer.valueOf(partes[2]) + "-" + (i + 1) + "-" + partes[0]);
            }
        }

        throw new NoiseException(date, DataType.DATE);
    }

    /**
     * Format: "October 23, 1995" or "October 5, 1995"
     *
     * @param date
     * @return
     */
    private static List<String> getAllFormats05(String date) throws NoiseException {
        List<String> allFormats = new ArrayList<>();

        String[] partes = date.trim().replaceAll(",", "").split(" ");
        for (int i = 0; i < months.length; i++) {
            if (partes[0].equals(months[i])) {
                return getAllFormats01(Integer.valueOf(partes[2]) + "-" + (i + 1) + "-" + partes[1]);
            }
        }

        throw new NoiseException(date, DataType.DATE);
    }

    /**
     * Format: "January 1996"
     *
     * @param date
     * @return
     */
    private static List<String> getAllFormats06(String date) throws NoiseException {
        List<String> allFormats = new ArrayList<>();

        String[] partes = date.split(" ");
        for (int i = 0; i < months.length; i++) {
            if (partes[0].equals(months[i])) {
                return getAllFormats02(Integer.valueOf(partes[1]) + "-" + (i + 1));
            }
        }

        throw new NoiseException(date, DataType.DATE);
    }
}
