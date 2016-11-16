/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.load;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class LoadEntityID {

    /**
     *
     * @param site
     * @return Map<URl,EntityID>
     */
    public static Map<String, String> loadEntityID(Site site) {
        Map<String, String> ids = new HashMap<>();

        try (Reader in = new FileReader(Paths.PATH_BASE + site.getEntityPath())) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {
                    ids.put(record.get("url").replaceAll(".*/"+site.getFolderName()+"/", ""), record.get("entityID"));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadEntityID.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadEntityID.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ids;
    }
}
