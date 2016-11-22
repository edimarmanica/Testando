/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.integration;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Mapping;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.ScoredPair;
import br.edimarmanica.weir_3_0.distance.TypeAwareDistance;
import static br.edimarmanica.weir_3_0.filter.Filter.HEADER;
import br.edimarmanica.weir_3_0.filter.weakfilter.WeakRulesFilter;
import br.edimarmanica.weir_3_0.load.LoadRules;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
public class Weir {

    private final Domain domain;
    private final String path;
    private final String lastFilter;
    private Set<Mapping> mappings;
    private final List<List<Rule>> rules = new ArrayList<>();

    public Weir(Domain domain, String path, String lastFilter) {
        this.domain = domain;
        this.lastFilter = lastFilter;
        this.path = path;
    }

    /**
     * carrega as regras de todos os sites
     */
    private void loadRules() {
        for (Site site : domain.getSites()) {
            rules.add(loadRules(site));
        }
    }

    /**
     * Carrega as regras que sobraram após o último filtro (lastFilter)
     *
     * @param site
     * @return
     */
    private List<Rule> loadRules(Site site) {
        Set<Integer> currentRules = new HashSet<>();
        try (Reader in = new FileReader(path + "/" + site.getPath() + "/" + lastFilter + ".csv")) {
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
        return new ArrayList<>(load.getRules(currentRules));
    }

    /**
     * Calcula os escores entre as regras do GP_UPDATE com as dos outros sites
     *
     * @param rulesSite
     * @param rulesComparedSite
     * @return
     */
    private List<ScoredPair> scorePairs() {
        List<ScoredPair> scores = new ArrayList<>();

        //calcula o escore entre as regras do mesmo site
        for (int i = 0; i < rules.size(); i++) {
            if (General.DEBUG) {
                System.out.println("\tSites restantes: " + (rules.size() - i));
            }

            for (int j = i; j < rules.size(); j++) {
                scores.addAll(scorePairs(rules.get(i), rules.get(j), i == j));
            }
        }
        Collections.sort(scores); //non-decresing order
        return scores;
    }

    /**
     * calcula o escore entre as regras de dois sites. Se os sites forem o mesmo
     * então calcula r1Xr2 e não calcula r2Xr1
     *
     * @param site1
     * @param site2
     * @param sameSite
     * @return
     */
    private List<ScoredPair> scorePairs(List<Rule> site1, List<Rule> site2, boolean sameSite) {
        List<ScoredPair> scores = new ArrayList<>();
        for (int i = 0; i < site1.size(); i++) {
            int j;
            if (sameSite) {
                j = i + 1;
            } else {
                j = 0;
            }
            for (; j < site2.size(); j++) {
                double score = TypeAwareDistance.typeDistance(site1.get(i), site2.get(j));
                if (score == -1) { //Número insuficiente de instâncias compartilhadas.
                    continue;
                }
                if (score == 1) {//regras completas não terão distância 1
                    continue;
                }
                scores.add(new ScoredPair(site1.get(i), site2.get(j), score));
            }
        }
        return scores;
    }

    public void execute() {
        if (General.DEBUG) {
            System.out.println("Loading rules ... ");
        }
        loadRules();

        if (General.DEBUG) {
            System.out.println("Scoring pairs ... ");
        }
        List<ScoredPair> scores = scorePairs();

        if (General.DEBUG) {
            System.out.println("Mapping ... ");
        }
        integration(scores);

        if (General.DEBUG) {
            System.out.println("Persisting ... ");
        }
        persistResults();
    }

    private void integration(List<ScoredPair> scores) {
        printLog("site_r1; r1; site_r2; r2; distance; action", false);

        /**
         * starts with singleton mappings (apenas para as regras de GP_UPDATE)
         * Cada regra é um mapeamento singleton
         */
        mappings = new HashSet<>();
        for (List<Rule> list : rules) {
            for (Rule rule : list) {
                mappings.add(new Mapping(rule));
            }
        }

        //processa os pares em ordem não decrescente
        int faltam = scores.size();
        for (ScoredPair sp : scores) {
            if (General.DEBUG) {
                System.out.println("\tPares restantes: " + faltam);
                faltam--;
            }

            //se as duas regras já estão no mesmo mapping ignora
            if (inSameMapping(sp.getR1(), sp.getR2(), mappings)) {
                if (General.DEBUG) {
                    printLog(sp.getR1().getSite() + ";" + sp.getR1().getRuleID() + ";" + sp.getR2().getSite() + ";" + sp.getR2().getRuleID() + ";" + sp.getDistance() + ";same mapping", true);
                }
                continue;
            }

            //se uma das regras pertece a um mapeamento completo ou se a regra oposta está em um mapeamento que já possui uma regra do site da regra 
            if (isComplete(sp.getR1(), mappings) || isComplete(sp.getR2(), mappings) || isLC(sp.getR1(), sp.getR2(), mappings)) {
                getMapping(sp.getR1(), mappings).setComplete(true);
                getMapping(sp.getR2(), mappings).setComplete(true);
                if (General.DEBUG) {
                    printLog(sp.getR1().getSite() + ";" + sp.getR1().getRuleID() + ";" + sp.getR2().getSite() + ";" + sp.getR2().getRuleID() + ";" + sp.getDistance() + ";Mapping complete", true);
                }
            } else {
                if (General.DEBUG) {
                    printLog(sp.getR1().getSite() + ";" + sp.getR1().getRuleID() + ";" + sp.getR2().getSite() + ";" + sp.getR2().getRuleID() + ";" + sp.getDistance() + ";Merging", true);
                }
                Mapping mR1 = getMapping(sp.getR1(), mappings);
                Mapping mS1 = getMapping(sp.getR2(), mappings);
                Mapping mUnion = new Mapping();
                mUnion.addRules(mR1.getRules());
                mUnion.addRules(mS1.getRules());

                mappings.remove(mR1);
                mappings.remove(mS1);
                mappings.add(mUnion);
            }
        }
    }

    /**
     *
     * @param r
     * @return the Mapping containing the rule r
     */
    private static Mapping getMapping(Rule r, Set<Mapping> mappings) {
        for (Mapping m : mappings) {
            if (m.getRules().contains(r)) {
                return m;
            }
        }
        throw new UnsupportedOperationException("Não deve entrar aqui! - Weir.java - getMapping");
    }

    /**
     *
     * @param r
     * @param s
     * @return true if the rules are already in the same mapping
     */
    private static boolean inSameMapping(Rule r, Rule s, Set<Mapping> mappings) {
        boolean flag = false;
        for (Mapping m : mappings) {

            if (m.getRules().contains(r) && m.getRules().contains(s)) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    /**
     *
     * @param r
     * @param wrappers
     * @return true if the mapping that contains the rule r is complete
     */
    private static boolean isComplete(Rule r, Set<Mapping> mappings) {
        return getMapping(r, mappings).isComplete();
    }

    /**
     *
     * @param r1
     * @param r2
     * @param mappings true se a intersecção entre (os sites do mapping que
     * contém o r1) e (os sites do mapping que contém o s1) não é vazia -- passo
     * 6 do algoritmo
     * @return
     */
    private static boolean isLC(Rule r1, Rule r2, Set<Mapping> mappings) {
        Mapping mR1 = getMapping(r1, mappings);
        Mapping mR2 = getMapping(r2, mappings);

        Set<Site> sitesR1 = new HashSet<>();
        for (Rule aux_r1 : mR1.getRules()) {
            sitesR1.add(aux_r1.getSite());
        }

        for (Rule aux_r2 : mR2.getRules()) {
            if (sitesR1.contains(aux_r2.getSite())) {
                return true;
            }
        }

        return false;
    }

    private void persistResults() {
        String[] header = {"MAP_ID", "SITE", "RULE"};

        try (Writer out = new FileWriter(path + "/" + domain.getPath() + "/mappings.csv")) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.EXCEL.withHeader(header))) {
                int i = 0;
                for (Mapping map : mappings) {
                    if (map.getRules().size() < 5) {
                        continue;
                    }

                    for (Rule rule : map.getRules()) {
                        List<String> dataRecord = new ArrayList<>();
                        dataRecord.add(i + "");
                        dataRecord.add(rule.getSite().getFolderName());
                        dataRecord.add(rule.getRuleID() + "");
                        csvFilePrinter.printRecord(dataRecord);
                    }
                    i++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Weir.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void printLog(String line, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/" + domain.getPath() + "/weir_log.csv", append))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException ex) {
            Logger.getLogger(WeakRulesFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Domain domain = br.edimarmanica.dataset.orion.Domain.DRIVER;
        String path = Paths.PATH_INTRASITE;
        String lastFilter = WeakRulesFilter.NAME;
        Weir weir = new Weir(domain, path, lastFilter);
        weir.execute();
    }
}
