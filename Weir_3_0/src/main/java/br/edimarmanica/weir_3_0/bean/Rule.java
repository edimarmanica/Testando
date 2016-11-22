/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.bean;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.distance.DataType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class Rule {

    private final int ruleID;
    private final String label;
    private final String ruleCypher;
    private Set<Value> values;
    private final Site site;
    private DataType type;

    public Rule(int ruleID, Site site, String label, String ruleCypher) {
        this.ruleID = ruleID;
        this.label = label;
        this.ruleCypher = ruleCypher;
        this.site = site;
    }

    public String getLabel() {
        return label;
    }

    public String getRuleCypher() {
        return ruleCypher;
    }

    public int getRuleID() {
        return ruleID;
    }

    public Set<Value> getValues() {
        return values;
    }

    public void setValues(Set<Value> values) {
        this.values = values;
    }

    public Site getSite() {
        return site;
    }

    public void addValue(String value, String pageID, String entityID) {
        if (values == null) {
            values = new HashSet<>();
        }

        values.add(new Value(value, pageID, entityID));
    }

    public void addValues(Set<Value> values) {
        if (values == null) {
            values = new HashSet<>();
        }

        values.addAll(values);
    }

    public void addValue(Value value) {
        if (values == null) {
            values = new HashSet<>();
        }

        values.add(value);
    }

    public Set<String> getPairsPageValue() {
        Set<String> valuesString = new HashSet<>();

        for (Value v : values) {
            if (v.getValue() != null) {
                valuesString.add(v.getPageID() + "_" + v.getValue());
            }
        }

        return valuesString;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.ruleID);
        hash = 79 * hash + Objects.hashCode(this.site.getPath());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rule other = (Rule) obj;
        if (this.ruleID != other.ruleID) {
            return false;
        }
        return this.site == other.site;
    }
}
