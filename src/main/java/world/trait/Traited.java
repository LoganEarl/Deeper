package main.java.world.trait;

import java.util.Set;

public interface Traited {
    Set<Trait> getInherentTraits();
    Set<Trait> getTransitiveTraits();
}
