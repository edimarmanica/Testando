/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.load;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import br.edimarmanica.weir_3_0.distance.DataTypeController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class LoadRules {

    private final Site site;
    private final Map<Integer, String> labels = new HashMap<>(); //<ruleID,label>
    private final Map<Integer, String> rulesCypher = new HashMap<>(); //<ruleID,ruleCypher>
    private Map<String, String> ids = new HashMap<>(); //<URL,EntityID>

    public LoadRules(Site site) {
        this.site = site;
    }

    /**
     * @param currentRules contém as regras que não foram eliminadas em filtros.
     * Se for informado null, retorna todas as regras
     * @return
     *
     */
    public Set<Rule> getRules(Set<Integer> currentRules) {
        Set<Rule> rules = new HashSet<>();

        ids = LoadEntityID.loadEntityID(site);
        loadRulesInfo();

        File dir = new File(Paths.PATH_INTRASITE + "/" + site.getPath() + "/extracted_values");
        for (File rule : dir.listFiles()) {//para cada rule
            if (!rule.getName().endsWith(".csv")) {
                continue;
            }
            int ruleID = Integer.valueOf(rule.getName().replaceAll("rule_", "").replaceAll("\\.csv", ""));
            if (currentRules != null && !currentRules.contains(ruleID)) { //essta regra já foi removida
                continue;
            }
            Map<String, String> idsCopy = new HashMap<>(ids);
            Rule newRule = new Rule(ruleID, site, labels.get(ruleID), rulesCypher.get(ruleID));

            try (Reader in = new FileReader(rule.getAbsolutePath())) {
                try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                    for (CSVRecord record : parser) { //para cada value
                        String url = record.get("URL").replaceAll(".*/" + site.getFolderName() + "/", "");
                        //o replace abaixo foi adicionado pq os filtros eliminam se os valores são iguais para diferentes regras do mesmo site, mas as vezes os valores de uma regra tinham um símbolo como >>
                        newRule.addValue(record.get("EXTRACTED VALUE"), url, idsCopy.get(url));
                        idsCopy.remove(url);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
            }

            /**
             * As páginas que a regra não extrai valor tem que ficar com valor
             * nulo pq na função de distância isso aumentará a distância e
             * ajudará a eliminar regras fracas
             */
            Set<String> discoveredPages = getDiscoveredPages();
            for (String url : idsCopy.keySet()) {
                //verificar se a página foi descoberta
                if (discoveredPages.contains(url)) {
                    newRule.addValue(new Value(null, url, idsCopy.get(url)));
                }
            }

            newRule.setType(DataTypeController.getMostFrequentType(newRule));
            rules.add(newRule);
        }

        return rules;
    }

    private void loadRulesInfo() {
        try (Reader in = new FileReader(Paths.PATH_INTRASITE + "/" + site.getPath() + "/rule_info.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {
                    labels.put(Integer.valueOf(record.get("ID")), record.get("LABEL"));
                    rulesCypher.put(Integer.valueOf(record.get("ID")), record.get("RULE"));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<String> getDiscoveredPages() {
        Set<String> discoveredPages = new HashSet<>();
        File dir = new File(Paths.PATH_BASE + "/" + site.getPath());
        for (String page : dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html");
            }
        })) {
            discoveredPages.add(page);
        }
        return discoveredPages;
    }

    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.orion.driver.Site.FORMULAE;

        LoadRules load = new LoadRules(site);
        Set<Rule> rules = load.getRules(null);
        for (Rule r : rules) {
            System.out.println("R: " + r.getRuleID());
            for (Value v : r.getValues()) {
                System.out.println("\t" + v.getEntityID() + "-" + v.getPageID() + "-[" + v.getValue() + "]");
            }
        }
    }
}
