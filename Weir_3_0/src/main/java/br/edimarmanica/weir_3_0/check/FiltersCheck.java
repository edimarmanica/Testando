/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import static br.edimarmanica.weir_3_0.filter.Filter.HEADER;
import br.edimarmanica.weir_3_0.filter.FirstFilter;
import br.edimarmanica.weir_3_0.filter.IdenticalValuesFilter;
import br.edimarmanica.weir_3_0.filter.NullValuesFilter;
import br.edimarmanica.weir_3_0.filter.weakfilter.WeakRulesFilter;
import br.edimarmanica.weir_3_0.load.LoadRules;
import br.edimarmanica.weir_3_0.util.Conjuntos;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public class FiltersCheck {

    private final Site site;
    private final String path;
    private final String[] FILTERS = {FirstFilter.NAME, NullValuesFilter.NAME, IdenticalValuesFilter.NAME, WeakRulesFilter.NAME};
    private Conjuntos<String> util = new Conjuntos<>();

    public FiltersCheck(Site site, String path) {
        this.site = site;
        this.path = path;
    }

    private void execute() {
        System.out.println("RULE;FILTER;OVERLAPPED_RULE");

        //ler as best rules (regra com maior F1 para cada atributo - estão em result.csv)
        Set<Integer> rules = getBestRules();
        for (Integer rule : rules) {
            //verifica em qual filtro ela foi removida (le os csvs de todos os filtros, na ordem)
            String filter = removedInFilter(rule);
            System.out.print(rule + ";" + filter + ";");

            if (filter != null && filter.equals(WeakRulesFilter.NAME)) {
                //se for no Weak, verifica qual das regras que ficaram que é sobreposta
                Integer overlappedRule = overlappedFinalRule(rule);
                System.out.print(overlappedRule);
            }
            System.out.println("");
        }
    }

    protected Set<Integer> loadRulesFilter(String filter) {
        Set<Integer> rules = new HashSet<>();
        try (Reader in = new FileReader(path + "/" + site.getPath() + "/" + filter + ".csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value
                    rules.add(Integer.parseInt(record.get(HEADER[0])));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rules;
    }

    //verifica em qual filtro ela foi removida (le os csvs de todos os filtros, na ordem)
    private String removedInFilter(int rule) {
        for (String filter : FILTERS) {
            if (!loadRulesFilter(filter).contains(rule)) {
                return filter;
            }
        }
        return null; //não foi eliminada por nenhum filtro
    }

    /**
     * encontra a regra final (que permaceu após todos os filtros) que é
     * sobreposta com a regra rule. A regra final é a regra que eliminou a rule
     *
     * @param rule
     * @return
     */
    private Integer overlappedFinalRule(int rule) {
        Rule r = loadRule(rule);
        for (Integer finalRule : loadRulesFilter(WeakRulesFilter.NAME)) {
            Rule rf = loadRule(finalRule);

            if (util.hasIntersection(r.getPairsPageValue(), rf.getPairsPageValue())) {
                return rf.getRuleID();
            }
        }
        return null;
    }

    private Rule loadRule(int ruleID) {
        Set<Integer> rules = new HashSet<>();
        rules.add(ruleID);
        LoadRules load = new LoadRules(site);
        List<Rule> rulesList = new ArrayList<>(load.getRules(rules));
        if (rulesList.size() != 1) {
            System.out.println("Erro com a regra: " + ruleID + " do site " + site);
        }
        return rulesList.get(0);
    }

    /**
     *
     * @return regras com maior F1 para cada atributo -- que foi obtido pelo
     * Evaluate do Intrasite e está em results.csv
     */
    private Set<Integer> getBestRules() {
        Set<Integer> rules = new HashSet<>();
        try (Reader in = new FileReader(path + "/" + site.getPath() + "/result.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value
                    rules.add(Integer.parseInt(record.get("RULE").replaceAll("rule_", "").replaceAll(".csv", "")));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rules;
    }

    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.orion.driver.Site.F1;
        String path = Paths.PATH_INTRASITE;
        FiltersCheck check = new FiltersCheck(site, path);
        check.execute();
    }
}
