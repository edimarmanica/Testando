/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.load.LoadRules;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public abstract class Filter {

    public static final String[] HEADER = {"RULE"};
    protected Site site;
    protected String path;
    protected String lastFilter;

    protected abstract void execute();
    
    /**
     * 
     * @return the name of the filter. It is also the name of the file that store the filtered rules
     */
    protected abstract String getFilterName();

    /**
     * @return regras atuais, ou seja, as regras que restaram após o filtro anterior
     */
    protected Set<Rule> loadCurrentRules() {
        return loadCurrentRules(site);
    }
    
    /**
     * @return regras atuais, ou seja, as regras que restaram após o filtro anterior
     */
    protected Set<Rule> loadCurrentRules(Site site) {
        Set<Integer> currentRules = new HashSet<>();
        try (Reader in = new FileReader(path+"/"+site.getPath()+"/"+lastFilter+".csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value
                    currentRules.add(Integer.parseInt(record.get(HEADER[0])));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }
        LoadRules load = new LoadRules(site);
        return load.getRules(currentRules);
    }

    /**
     * 
     * @param keptRules regras mantidas após o filtro atual
     */
    protected void persiste(Set<Integer> keptRules) {
        File file = new File(path+"/"+site.getPath()+"/"+getFilterName()+".csv");
        CSVFormat format = CSVFormat.EXCEL.withHeader(HEADER);

        try (Writer out = new FileWriter(file)) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                for (Integer rule : keptRules) {
                    csvFilePrinter.printRecord(rule);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NullValuesFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
