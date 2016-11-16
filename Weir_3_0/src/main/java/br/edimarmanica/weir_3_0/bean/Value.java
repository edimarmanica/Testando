/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.bean;

import java.util.Objects;

/**
 *
 * @author edimar
 */
public class Value {

    private String value;
    private String pageID;
    private String entityID;

    public Value(String value, String pageID, String entityID) {
        this.value = value;
        this.pageID = pageID;
        this.entityID = entityID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPageID() {
        return pageID;
    }

    public void setPageID(String pageID) {
        this.pageID = pageID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.value);
        hash = 29 * hash + Objects.hashCode(this.pageID);
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
        final Value other = (Value) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!this.pageID.equals(other.pageID)) {
            return false;
        }
        return true;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

}
