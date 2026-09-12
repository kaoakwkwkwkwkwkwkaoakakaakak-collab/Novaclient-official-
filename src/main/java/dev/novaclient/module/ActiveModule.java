package dev.novaclient.module;

import java.util.ArrayList;
import java.util.List;

public class ActiveModule extends Module {

    public interface Effect {
        void apply(boolean enabled) throws RuntimeException;
    }

    public interface TickEffect {
        void tick() throws RuntimeException;
    }

    private final List<Effect> effects = new ArrayList<>();
    private final List<TickEffect> tickEffects = new ArrayList<>();
    private boolean faulty;
    private String faultMessage;

    public ActiveModule(String name, String description, Category category) {
        super(name, description, category);
    }

    public ActiveModule effect(Effect effect) {
        effects.add(effect);
        return this;
    }

    public ActiveModule onTick(TickEffect tickEffect) {
        tickEffects.add(tickEffect);
        return this;
    }

    @Override
    protected void onEnable() {
        runEffects(true);
    }

    @Override
    protected void onDisable() {
        runEffects(false);
    }

    private void runEffects(boolean enabled) {
        for (Effect effect : effects) {
            try {
                effect.apply(enabled);
            } catch (RuntimeException failure) {
                markFaulty(failure);
                return;
            }
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled() || faulty) {
            return;
        }
        for (TickEffect tickEffect : tickEffects) {
            try {
                tickEffect.tick();
            } catch (RuntimeException failure) {
                markFaulty(failure);
                return;
            }
        }
    }

    private void markFaulty(RuntimeException failure) {
        faulty = true;
        faultMessage = failure.getClass().getSimpleName() + ": " + failure.getMessage();
        setEnabled(false);
    }

    public boolean isFaulty() {
        return faulty;
    }

    public String faultMessage() {
        return faultMessage;
    }

    public void clearFault() {
        faulty = false;
        faultMessage = null;
    }

    public boolean hasEffects() {
        return !effects.isEmpty() || !tickEffects.isEmpty();
    }
}
