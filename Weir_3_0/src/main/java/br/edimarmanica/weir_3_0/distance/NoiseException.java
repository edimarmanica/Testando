/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

/**
 *
 * @author edimar
 */
public class NoiseException extends Exception {

    public NoiseException(String value, DataType type) {
        super("Value [" + value + "] is not a correct value for the type " + type.name());
    }
}
