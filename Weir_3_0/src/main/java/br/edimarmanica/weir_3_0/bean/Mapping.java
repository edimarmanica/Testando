/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.bean;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class Mapping {

    private Set<Rule> rules;
    private boolean complete = false;

    public Mapping() {
    }

    public Mapping(Rule rule) {
        addRule(rule);
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public void addRule(Rule rule) {
        if (rules == null) {
            rules = new HashSet<>();
        }

        rules.add(rule);
    }

    public void addRules(Set<Rule> rules) {
        if (this.rules == null) {
            this.rules = new HashSet<>();
        }

        this.rules.addAll(rules);
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

}
