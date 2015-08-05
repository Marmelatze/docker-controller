package de.schub.marathon_scaler.Customer;

/**
 * Represents a customer stored in backend. Used for serialization and deserialization via GSON
 */
public class Customer
{
    private int id;

    private String name;

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }
}
