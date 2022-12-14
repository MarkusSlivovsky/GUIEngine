package org.vnna.core.example.data;

import org.vnna.core.example.data.game.ExampleObject;

import java.io.Serializable;

/**
 * Represtens a current Snapshot of the Simulations state.
 * Contents of this class are only modified by the GameEngine.
 * - All Members need to break down into Primitives
 * - All Members need to be serializable.
 * - All Members are public.
 * - Collections are allowed and should be initialized on declartion
 */
public class ExampleData implements Serializable {

    public ExampleObject exampleObject;


}
