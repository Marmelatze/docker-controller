package de.schub.marathon_scaler.Monitoring.Strategy;

import java.util.HashMap;

/**
 * Get a {ScalingStrategy} by its name
 */
public class StrategyFactory
{
    private final HashMap<String, ScalingStrategy> strategies;

    public StrategyFactory(HashMap<String, ScalingStrategy> strategies)
    {
        this.strategies = strategies;
    }

    public ScalingStrategy get(String name) throws UnkownStrategyException
    {
        if (!strategies.containsKey(name)) {
            throw new UnkownStrategyException(name);
        }

        return strategies.get(name);
    }

    public class UnkownStrategyException extends Throwable
    {
        public UnkownStrategyException(String name)
        {
            super("Strategie \"" + name + "\" does not exist");
        }
    }
}
