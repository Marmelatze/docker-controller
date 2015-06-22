package de.schub.marathon_scaler.Customer;

import com.google.gson.Gson;
import mesosphere.marathon.client.model.v2.Group;

public class GroupTemplate
{
    private final Gson gson;
    private final String json;

    public GroupTemplate(Gson gson, String json)
    {
        this.gson = gson;
        this.json = json;
    }

    public Group create(Customer customer)
    {
        String json2 = json.replace("{customer_id}", Integer.toString(customer.getId()));
        Group group = gson.fromJson(json2, Group.class);
        group.setId("/customer/" + customer.getId());

        return group;
    }
}
